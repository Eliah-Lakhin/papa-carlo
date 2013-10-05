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
package papacarlo.syntax

import name.lakhin.eliah.projects.papacarlo.syntax.rules._

abstract class Rule {
  final def &(another: Rule) = this match {
    case SequentialRule(steps) => SequentialRule(steps :+ another)
    case _ => SequentialRule(List(this, another))
  }

  final def |(another: Rule) = this match {
    case ChoiceRule(choices) => ChoiceRule(choices :+ another)
    case _ => ChoiceRule(List(this, another))
  }

  final def |(exception: String) = this match {
    case RecoveryRule(rule, _) => RecoveryRule(rule, exception)
    case _ => RecoveryRule(this, exception)
  }

  final def min(min: Int) = this match {
    case RepetitionRule(element, division, minOption, max) =>
      RepetitionRule(element, division, minOption.orElse(Some(min)), max)
    case _ => this
  }

  final def max(max: Int) = this match {
    case RepetitionRule(element, division, min, maxOption) =>
      RepetitionRule(element, division, min, maxOption.orElse(Some(max)))
    case _ => this
  }

  final def by(division: Rule) = this match {
    case RepetitionRule(element, divisionOption, min, max) =>
      RepetitionRule(element, divisionOption.orElse(Some(division)), min, max)
    case _ => this
  }

  final def as(tag: String): Rule = this match {
    case NamedRule(subrule, name) => NamedRule(subrule.as(tag), name)
    case ReferentialRule(ruleName, None) =>
      ReferentialRule(ruleName, Some(tag))
    case _ => this
  }

  def apply(session: Session): Int
}

object Rule {
  def token(kind: String) = TokenRule(kind)

  def marker(kind: String, tag: String = "") =
    capture(token(kind), if (tag.size > 0) tag else kind)

  def capture(rule: Rule, tag: String) = CapturingRule(rule, tag)

  def composition(label: String)(subrule: Rule) =
    NamedRule(subrule, label)

  def seqUntil(leaveKind: String) = TokenRule(leaveKind, matchUntil = true)

  def optional(rule: Rule) = repeat(rule).min(0).max(1)

  def repeat(rule: Rule) = rule match {
    case RepetitionRule(_, _, _, _) => rule
    case _ => RepetitionRule(rule)
  }
}