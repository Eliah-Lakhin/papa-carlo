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
package papacarlo.test.utils

import name.lakhin.eliah.projects.papacarlo.lexis.Token
import name.lakhin.eliah.projects.papacarlo.syntax.rules.NamedRule
import name.lakhin.eliah.projects.papacarlo.syntax.{Result, Rule, State, Cache}
import name.lakhin.eliah.projects.papacarlo.{Syntax, Lexer}

final class DebugMonitor(lexer: Lexer, syntax: Syntax)
  extends SyntaxMonitor(lexer, syntax) {

  private var log = List.empty[(Symbol, String)]
  private var fragment = IndexedSeq.empty[Token]
  private var segment = IndexedSeq.empty[Token]
  private var deep = 0

  val onCacheInvalidate = (cache: Cache) => {
    log ::= Symbol("fragment of node " + cache.node.id) ->
      (if (shortOutput) cache.fragment.toString
      else cache.fragment.highlight(Some(10)))
    fragment = cache.fragment.getTokens
  }

  val onParseStep = (tokens: Seq[Token]) => {
    segment = tokens.toIndexedSeq
    if (shortOutput) log ::= 'segment -> ("Length: " + segment.size)
    else log ::= 'segment -> segment.map(_.value).mkString(" ")
  }

  val onRuleEnter: ((Rule, State)) => Any = {
    case (rule: Rule, state: State) => {
      if (isTraceable(rule) || deep > 0) deep += 1

      if (rule.isInstanceOf[NamedRule] && deep > 0)
        log ::= 'enter -> (rule.show._1 + stateInfo(state))
    }
  }

  val onRuleLeave: ((Rule, State, Int)) => Any = {
    case (rule: Rule, state: State, result: Int) => {
      if (rule.isInstanceOf[NamedRule] && deep > 0)
        log ::= 'leave -> (rule.show._1 + stateInfo(state) + (result match {
          case Result.Failed => "\nFailed"
          case Result.Recoverable => "\nRecoverable"
          case _ => "\nSuccessful"
        }))

      if (deep > 0) deep -= 1
    }
  }

  private def isTraceable(rule: Rule) = rule match {
    case NamedRule(_, _, true) => true
    case _ => false
  }

  private def stateInfo(state: State) = {
    var result = ""

    if (shortOutput) result += "\nposition: " + state.virtualPosition
    else result += "\n" +
      segment.take(state.virtualPosition).map(_.kind).mkString(" ") +
      ":|:" + segment.drop(state.virtualPosition).map(_.kind).mkString(" ")

    if (state.captures.nonEmpty) {
      result += "\ncaptures:"
      if (shortOutput) result += state.captures.size
      else for ((key, value) <- state.captures)
        result += "\n  " + key + " = " +
          value.slice(fragment).map(_.value).mkString
    }

    if (state.products.nonEmpty) {
      result += "\nproducts:"
      if (shortOutput) result += state.products.size
      else for ((key, value) <- state.products)
        result += "\n  " + key + " = " + value.toString
    }

    if (state.issues.nonEmpty) {
      result += "\nissues:"
      if (shortOutput) result += state.issues.size
      else for (issue <- state.issues)
        result += "\n  \"" + issue.description + "\" = " +
          issue.range.slice(segment).map(_.kind).mkString(" ")
    }

    result
  }

  def getResult = unionLog(log, enterLeaveIndentation = true)

  def prepare() {
    log = Nil
    syntax.onCacheInvalidate.bind(onCacheInvalidate)
    syntax.onParseStep.bind(onParseStep)
    syntax.onRuleEnter.bind(onRuleEnter)
    syntax.onRuleLeave.bind(onRuleLeave)
  }

  def release() {
    log = Nil
    syntax.onCacheInvalidate.unbind(onCacheInvalidate)
    syntax.onParseStep.unbind(onParseStep)
    syntax.onRuleEnter.unbind(onRuleEnter)
    syntax.onRuleLeave.unbind(onRuleLeave)
  }
}