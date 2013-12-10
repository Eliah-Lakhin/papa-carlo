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

final class Contextualizer {
  final class TokenPairDefinition(val open: String,
                                  val close: String,
                                  val context: Int) {
    private[lexis] var top = false
    private[lexis] var priorityLevel: Int = 1
    private[lexis] var skipLevel: SkipLevel = OriginalSkipping
    private[lexis] var caching = false

    def forceSkip = {
      skipLevel = ForceSkip

      this
    }

    def forceUse = {
      skipLevel = ForceUse

      this
    }

    def priority(priority: Int) = {
      this.priorityLevel = priority

      this
    }

    def topContext = {
      this.top = true

      this
    }

    def allowCaching = {
      this.caching = true

      this
    }
  }

  private var pairs = Vector.empty[TokenPairDefinition]
  private var stateMachineCache =
    Option.empty[Map[String, Map[Int, (SeamType, Int)]]]
  private[papacarlo] var lineCutTokens = Set.empty[String]

  def trackContext(open: String, close: String) = {
    val result = new TokenPairDefinition(open, close, pairs.length + 1)

    pairs :+= result
    stateMachineCache = None

    if (open == Token.LineBreakKind & close != Token.LineBreakKind)
      lineCutTokens += close

    if (open != Token.LineBreakKind & close == Token.LineBreakKind)
      lineCutTokens += open

    result
  }

  private def stateMachine = {
    stateMachineCache.getOrElse({
      var stateMachine = List.empty[(String, Int, SeamType, Int)]

      for (first <- pairs) {
        stateMachine ::= Tuple4(first.open, 0, EnterContext, first.context)
        stateMachine ::= Tuple4(first.close, first.context, LeaveContext, -1)

        if (first.open != first.close) {
          stateMachine ::= Tuple4(first.open, first.context, EnterContext,
            first.context)
          if (first.close != Token.LineBreakKind)
            stateMachine ::= Tuple4(first.close, 0, UnexpectedSeam, -1)
        }

        for (second <- pairs)
          if (!second.top
            && first.context != second.context
            && first.priorityLevel >= second.priorityLevel) {

            stateMachine ::= Tuple4(first.open, second.context, EnterContext,
              first.context)
            if (first.open != first.close &&
              first.close != Token.LineBreakKind) {
              stateMachine ::= Tuple4(first.close, second.context,
                UnexpectedSeam, -1)
            }
          }
      }

      val result =
        stateMachine.groupBy(_._1).mapValues(_.groupBy(_._2)
          .mapValues(list => (list.head._3, list.head._4)).view.force)
          .view.force

      this.stateMachineCache = Some(result)

      result
    })
  }

  def contextualize(entryContext: Context, tokens: Seq[Token]) = {
    val stateMachine = this.stateMachine

    var context = entryContext
    for (token <- tokens) {
      val next = stateMachine.get(token.kind).flatMap(_.get(context.kind))
        .getOrElse(Pair(RegularSeam, 0))

      token.seam = next._1

      next._1 match {
        case EnterContext =>
          context = context.branch(next._2)
          token.context = context
        case LeaveContext =>
          token.context = context
          context = context.parent.getOrElse(Context.Base)
        case _ => token.context = context
      }
    }

    context
  }

  private[lexis] def isCachableContext(context: Context) =
    pairs.lift(context.kind - 1).exists(_.caching)

  private[lexis] def getContextSkipLevel(context: Context) =
    pairs.lift(context.kind - 1).map(_.skipLevel)
}
