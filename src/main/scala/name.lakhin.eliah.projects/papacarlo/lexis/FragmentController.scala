/*
   Copyright 2013 Ilya Lakhin (Илья Александрович Лахин)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package name.lakhin.eliah.projects
package papacarlo.lexis

import name.lakhin.eliah.projects.papacarlo.utils.{Registry, Bounds, Signal}
import scala.util.control.Breaks._

final class FragmentController(contextualizer: Contextualizer,
                               tokens: TokenCollection) {
  private val registry = new Registry[Fragment]
  private var invalidationContext = Option.empty[Context]
  private var invalidationRange = Bounds.undefined
  private var invalidTokens = Map.empty[Int, SeamType]
  private var valid = true

  val onCreate = registry.onAdd
  val onInvalidate = new Signal[(Fragment, Bounds)]
  val onRemove = registry.onRemove

  val rootFragment = createFragment(tokens.head, tokens.last)

  tokens.onBeforeRewrite.bind {
    case (oldRange: Bounds) =>
      updateInvalidationContext(oldRange)
      invalidationRange = invalidationRange.takeout(oldRange)
  }

  tokens.onAfterRewrite.bind {
    case (newRange: Bounds) =>
      invalidationRange = invalidationRange.inject(newRange)
      val newTokens = invalidationRange.slice(tokens.descriptions)
      contextualizer.contextualize(contextAfter(invalidationRange.from - 1),
                                   newTokens)
      invalidTokens = Map.empty
      valid = true

      var index = newRange.from
      for (token <- newTokens) {
        if (token.seam == UnexpectedSeam) {
          invalidTokens += Tuple2(index, LeaveContext)
          valid = false
        }
        index += 1
      }

      val left = contextAfter(invalidationRange.until - 1)
      val right = contextBefore(invalidationRange.until)

      if (left != right) {
        val intersection = left.intersect(right)
        invalidationContext = invalidationContext
          .map(_.intersect(intersection))
          .orElse(Some(intersection))

        checkLeftBalance(invalidationRange.until - 1, intersection)
        checkRightBalance(invalidationRange.until, intersection)
      }

      updateInvalidationContext(invalidationRange)
      invalidate()

      if (valid) {
        invalidationRange = Bounds.undefined
        invalidationContext = None
      } else
        for ((index, seam) <- invalidTokens)
          invalidationRange = invalidationRange.union(index)
  }

  private def invalidate(): Unit = {
    val range = computeActualRange
    updateSkipLevel(range)

    var fragmentOrigins = List.empty[TokenReference]
    var fragmentsToInvalidate = List.empty[Fragment]
    val invalidationContext = this.invalidationContext.getOrElse(Context.Base)
    val tokenCount = tokens.descriptions.length

    for (index <- range.iterator) {
      if (index == 0) fragmentOrigins ::= tokens.head

      val token = tokens.descriptions.lift(index).getOrElse(Token.lineBreak)

      if (!invalidTokens.contains(index))
        token.seam match {
          case EnterContext =>
            fragmentOrigins ::= tokens.references
              .lift(index)
              .getOrElse(tokens.head)

          case LeaveContext =>
            val begin = fragmentOrigins match {
              case head :: tail =>
                fragmentOrigins = tail
                head

              case _ => tokens.head
            }
            val end = tokens.references.lift(index).getOrElse(tokens.last)
            val existFragment = begin.fragment.filter(fragment => {
              val same = fragment.end.index == end.index
              if (!same) fragment.remove()
              same
            })
            if (begin.fragment.isEmpty
                || token.context == invalidationContext)
              existFragment match {
                case Some(fragment) => fragmentsToInvalidate ::= fragment
                case None =>
                  if (contextualizer.isCachableContext(token.context))
                    begin.fragment = Some(createFragment(begin, end))
              }

          case _ =>
        } else
        for (fragmentToRemove <- tokens.references
               .lift(index)
               .flatMap(_.fragment))
          fragmentToRemove.remove()

      if (index == tokenCount - 1 && fragmentOrigins.nonEmpty) {
        val rootBegin = fragmentOrigins.head

        if (rootBegin.index == 0 || token.context == invalidationContext)
          fragmentsToInvalidate ::= rootFragment
        fragmentOrigins = fragmentOrigins.tail
      }
    }

    for (fragment <- fragmentsToInvalidate.reverse)
      fragment.onInvalidate.trigger(fragment)

    if (fragmentsToInvalidate.isEmpty &&
        this.invalidationContext.exists(_.parent.nonEmpty)) {

      this.invalidationContext = this.invalidationContext.flatMap(_.parent)
      invalidate()
    }
  }

  private def createFragment(start: TokenReference, end: TokenReference) = {
    val fragment = registry.add(id => Fragment(id, start, end))

    fragment.onInvalidate.bind(fragment =>
      onInvalidate.trigger(Tuple2(fragment, invalidationRange)))

    fragment.onRemove.bind(fragment => registry.remove(fragment.id))

    fragment
  }

  private def updateSkipLevel(range: Bounds): Unit = {
    for (token <- range.slice(tokens.descriptions);
         skipLevel <- contextualizer.getContextSkipLevel(token.context))
      token.applySkipLevel(skipLevel)
  }

  private def computeActualRange = {
    val invalidationContext = this.invalidationContext.getOrElse(Context.Base)
    val tokenCount = tokens.descriptions.length

    invalidationRange.map(
      from =>
        (0 until (from + 1)).reverse
          .takeWhile(index => {
            val token = tokens.descriptions
              .lift(index)
              .getOrElse(Token.lineBreak)

            invalidTokens.contains(index) || token.seam != EnterContext ||
            token.context != invalidationContext
          })
          .lastOption
          .map(_ - 1)
          .filter(_ >= 0)
          .getOrElse(0),
      until =>
        (((until - 1) max 0) until tokenCount)
          .takeWhile(index => {
            val token = tokens.descriptions
              .lift(index)
              .getOrElse(Token.lineBreak)

            invalidTokens.contains(index) || token.seam != LeaveContext ||
            token.context != invalidationContext
          })
          .lastOption
          .map(_ + 1)
          .filter(_ < tokenCount)
          .getOrElse(tokenCount - 1) + 1
    )
  }

  private def updateInvalidationContext(range: Bounds): Unit = {
    var outOfContext = true
    var index = range.from

    for (token <- range.slice(tokens.descriptions)) {
      val invalid = invalidTokens.contains(index)

      if (outOfContext || (!invalid && token.seam == EnterContext)) {
        invalidationContext = invalidationContext
          .map(_.intersect(token.context))
          .orElse(Some(token.context))
        outOfContext = false
      } else if (!invalid && token.seam == LeaveContext) outOfContext = true

      index += 1
    }

    if (outOfContext)
      invalidationContext = invalidationContext
        .map(
          _.intersect(
            tokens.descriptions
              .lift(range.until)
              .map(_.context)
              .getOrElse(Context.Base)))
  }

  private def contextAfter(index: Int) = contextNear(index, LeaveContext)

  private def contextBefore(index: Int) = contextNear(index, EnterContext)

  private def contextNear(index: Int, seam: SeamType) =
    tokens.descriptions
      .lift(index)
      .map(token => {
        val context = token.context
        context.parent.filter(parent => token.seam == seam).getOrElse(context)
      })
      .getOrElse(Context.Base)

  private def checkLeftBalance(start: Int, prototype: Context): Unit = {
    checkBalance((0 until (start + 1)).reverse,
                 LeaveContext,
                 EnterContext,
                 prototype)
  }

  private def checkRightBalance(start: Int, prototype: Context): Unit = {
    checkBalance(start until tokens.descriptions.length,
                 EnterContext,
                 LeaveContext,
                 prototype)
  }

  private def checkBalance(indexes: Range,
                           increment: SeamType,
                           decrement: SeamType,
                           prototype: Context): Unit = {
    var balance = 0

    breakable {
      for (index <- indexes) {
        val token = tokens.descriptions.lift(index).getOrElse(Token.lineBreak)

        if (token.context == prototype) break()

        if (token.seam == increment) balance += 1
        else if (token.seam == decrement) {
          if (balance > 0) balance -= 1
          else {
            invalidTokens += index -> decrement
            valid = false
          }
        }
      }
    }
  }
}
