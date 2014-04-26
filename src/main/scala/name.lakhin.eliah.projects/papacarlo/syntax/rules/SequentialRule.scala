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

import name.lakhin.eliah.projects.papacarlo.syntax.{Session, Rule}
import name.lakhin.eliah.projects.papacarlo.syntax.Result._

final case class SequentialRule(steps: List[Rule]) extends Rule {
  def apply(session: Session): Int = {
    session.syntax.onRuleEnter.trigger(this, session.state)

    var result = Successful
    val initialState = session.state

    for (step <- steps)
      step(session) match {
        case Failed =>
          session.state = initialState.copy(issues = session.state.issues)
          session.syntax.onRuleLeave.trigger(this, session.state, Failed)
          return Failed

        case Recoverable => result = Recoverable

        case _ =>
      }

    session.syntax.onRuleLeave.trigger(this, session.state, result)
    result
  }

  override val show = steps.map(_.showOperand(3)).mkString(" & ") -> 3

  override val captures = steps.map(_.captures).reduce(_ ++ _)

  override val branches = steps.map(_.branches).reduce {
    (left, right) =>
      var result = left
      for ((key, values) <- right)
        result += key -> (result.getOrElse(key, Set.empty) ++ values)
      result
  }
}
