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

import name.lakhin.eliah.projects.papacarlo.syntax.{Issue,
  Result, Session, Rule}
import name.lakhin.eliah.projects.papacarlo.utils.Bounds

final case class NamedRule(label: String, rule: Rule) extends Rule {
  def apply(session: Session) = {
    session.syntax.onRuleEnter.trigger(this, session.state)

    val initialState = session.state
    val result = rule(session)

    if (result == Result.Failed)
      session.state = initialState.issue(label + " expected")

    session.syntax.onRuleLeave.trigger(this, session.state, result)
    result
  }

  override val show =
    rule match {
      case ReferentialRule(name, tag) if name == label => rule.show
      case _ =>
        rule.showOperand(Int.MaxValue) + ".name(" + label + ")" -> Int.MaxValue
    }

  override def map(mapper: Rule => Rule) =
    mapper(this.copy(label, rule.map(mapper)))
}
