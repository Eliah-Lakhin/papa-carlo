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
package papacarlo.syntax.rules

import name.lakhin.eliah.projects.papacarlo.syntax.{Issue, Rule, Session}
import name.lakhin.eliah.projects.papacarlo.utils.Bounds
import name.lakhin.eliah.projects.papacarlo.syntax.Result._

final case class TokenRule(kind: String,
                           matchUntil: Boolean = false) extends Rule {
  def apply(session: Session) = {
    session.syntax.onRuleEnter.trigger(this, session.state)

    var index = session.state.virtualPosition

    if (matchUntil) {
      while (session.tokens.lift(index).exists(_.kind != kind)) index += 1
    }

    val actualKind = session.tokens
      .lift(index).map(_.kind).getOrElse(TokenRule.EndOfFragmentKind)

    val result = if (actualKind == kind) {
      session.state = session.state.copy(virtualPosition = index + 1)
      Successful
    } else {
      session.state = session.state.issue(
        Bounds(session.state.virtualPosition, index + 1),
        kind + " expected, but " + actualKind  + " found"
      )
      Failed
    }

    session.syntax.onRuleLeave.trigger(this, session.state, result)
    result
  }

  override val show = {
    val terminal = !kind.toLowerCase.forall(char => 'a' <= char && char <= 'z')
    (if (terminal) kind else "'" + kind + "'") +
      (if (matchUntil) ".matchUntil" else "") -> Int.MaxValue
  }
}

object TokenRule {
  val EndOfFragmentKind = "end of fragment"
}