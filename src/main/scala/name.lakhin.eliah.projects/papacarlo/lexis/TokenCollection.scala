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

import name.lakhin.eliah.projects.papacarlo.utils.{Difference, Signal, Bounds}
import scala.util.control.Breaks._

final class TokenCollection(lineCutTokens: Set[String]) {
  private[papacarlo] var descriptions = IndexedSeq(Token.lineBreak)
  private[papacarlo] var references = IndexedSeq(new TokenReference(this, 0))
  private[papacarlo] var head = new TokenReference(this, 0)
  private[papacarlo] var last = new TokenReference(this, 0)

  val onBeforeRewrite = new Signal[Bounds]
  val onAfterRewrite = new Signal[Bounds]

  def cursor(tokenIndex: Int) = {

    var result = (1, 1)
    var currentIndex = 0

    breakable {
      for (token <- descriptions) {
        currentIndex += 1

        if (currentIndex > tokenIndex) break()

        if (token.kind == Token.LineBreakKind) result = (result._1 + 1, 1)
        else result = (result._1, result._2 + token.value.length)
      }
    }

    result
  }

  def range(range: Bounds) = {
    var result = ""

    if (range.defined) {
      val begin = cursor(range.from)
      result += "(" + begin._1 + ":" + begin._2 + ")"

      if (range.length > 0) {
        val end = cursor(range.until)
        result += " - (" + end._1 + ":" + end._2 + ")"
      }
    }

    result
  }

  def write(bounds: Bounds, replacement: Seq[Token]) {
    val oldTokens = bounds.slice(descriptions)

    var equal = false

    if (oldTokens.length == replacement.length) {
      val tokenPairs = oldTokens.zip(replacement)

      if (tokenPairs.forall(pair => pair._1.sameAs(pair._2))) {
        val references = bounds.slice(this.references)

        if (references.forall(_.onUpdate.nonEmpty)) {
          equal = true

          descriptions = bounds.replace(descriptions, replacement)

          for (((oldToken, newToken), reference) <- tokenPairs.zip(references))
            if (oldToken.value != newToken.value)
              reference.onUpdate.trigger(reference)
        }
      }
    }

    if (!equal) {
      val diff = computeDifference(oldTokens, replacement)
      val range = bounds.enlarge(-diff._1, -diff._2)

      onBeforeRewrite.trigger(range)

      val newTokens = replacement.slice(diff._1, replacement.size - diff._2)
      val newRange = Bounds(range.from, range.from + newTokens.size)

      for (removeTarget <- range.slice(references)) removeTarget.remove()

      val rightHandOffset = newTokens.size - range.length
      for (rightHand <- references.slice(range.until, references.size))
        rightHand.index += rightHandOffset
      last.index += rightHandOffset

      descriptions = range.replace(descriptions, newTokens)
      references = range.replace(
        references,
        (for (index <- newRange.iterator)
          yield new TokenReference(this, index)).toSeq
      )

      onAfterRewrite.trigger(newRange)
    }
  }

  def highlight(segment: Bounds, limit: Option[Int] = None): String =
    highlightSegments(List((segment, "<<<", ">>>")), limit = limit)

  def highlightSegments(segments: List[(Bounds, String, String)],
                        context: Boolean = false,
                        limit: Option[Int] = None) = {
    var currentContext = ""
    val tokens = descriptions.zipWithIndex.map {
      case (token, index) =>
        var view = token.value

        if (context) {
          val contextView = token.context.view
          if (currentContext != contextView ||
            segments.exists(segment => segment._1.from == index ||
              segment._1.until - 1 == index)) {

            currentContext = contextView
            view += "~" + currentContext + "~"
          }
        }

        for ((range, start, end) <- segments) {
          if (range.length > 0) {
            if (range.from == index) {
              view = start + view
            }

            if (range.until - 1 == index) {
              view = view + end
            }
          }
          else if (range.from == index) {
            view = start + end + view
          }
        }

        view
    }

    limit match {
      case Some(limit: Int) =>
        val lines = tokens.mkString.split("\n", -1)

        val starts = segments.map(_._2)
        val ends = segments.map(_._3)

        val firstLine =
          (lines.indexWhere(line => starts.exists(line.contains)) -
            limit) max 0

        val lastLine =
          (lines.lastIndexWhere(line => ends.exists(line.contains)) +
            limit + 1) min lines.size

        var result = lines.slice(firstLine, lastLine).mkString("\n")

        if (firstLine > 0)
          result = "..." + firstLine + "\n" + result

        if (lastLine < lines.size)
          result = result + "\n" + (lastLine + 1) + "..."

        result

      case None => tokens.mkString
    }
  }

  private def computeDifference(first: Seq[Token], second: Seq[Token]) = {
    val comparator =
      (pair: Tuple2[Token, Token]) => pair._1.value == pair._2.value

    if (first.exists(token => lineCutTokens(token.kind))
        || second.exists(token => lineCutTokens(token.kind)))
        (
          Difference.head[Token](first, second, comparator),
          0
        )
      else
        Difference.double[Token](first, second, comparator)
  }
}
