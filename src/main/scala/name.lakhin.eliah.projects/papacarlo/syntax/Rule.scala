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
  def apply(session: Session): Int

  def permissive = RecoveryRule(this)

  def permissive(description: String) = RecoveryRule(this, Some(description))

  def required = RequiredRule(this)
}

object Rule {
  def token(kind: String) = TokenRule(kind)

  def tokensUntil(lastKind: String) = TokenRule(lastKind, matchUntil = true)

  def capture(tag: String, rule: Rule) = rule match {
    case CapturingRule(_, rule: Rule) => CapturingRule(tag, rule)
    case _ => CapturingRule(tag, rule)
  }

  def branch(tag: String, rule: Rule): Rule = rule match {
    case rule: ReferentialRule => rule.copy(tag = Some(tag))

    case ChoiceRule(cases) =>
      ChoiceRule(cases.map(choice => branch(tag, choice)))

    case SequentialRule(steps) =>
      ChoiceRule(steps.map(step => branch(tag, step)))

    case rule@RepetitionRule(element, _, _, _) =>
      rule.copy(element = branch(tag, element))

    case RecoveryRule(rule: Rule, exception, recoveryBranch) =>
      RecoveryRule(branch(tag, rule), exception, Some(tag))

    case NamedRule(label: String, rule: Rule) =>
      NamedRule(label, branch(tag, rule))

    case _ => rule
  }

  def name(label: String, rule: Rule) = rule match {
    case NamedRule(_, rule: Rule) => NamedRule(label, rule)
    case _ => NamedRule(label, rule)
  }

  def name(label: String)(body: => Rule): NamedRule = name(label, body)

  def optional(rule: Rule) = RepetitionRule(rule, max = Some(1))

  def zeroOrMore(rule: Rule) = RepetitionRule(rule)

  def oneOrMore(rule: Rule) = RepetitionRule(rule, min = Some(1))

  def repeat(rule: Rule, times: Int) =
    RepetitionRule(rule, min = Some(times), max = Some(times))

  def zeroOrMore(rule: Rule, separator: Rule) =
    RepetitionRule(rule, separator = Some(separator))

  def oneOrMore(rule: Rule, separator: Rule) =
    RepetitionRule(rule, separator = Some(separator), min = Some(1))

  def repeat(rule: Rule, separator: Rule, times: Int) =
    RepetitionRule(rule, separator = Some(separator), min = Some(times),
      max = Some(times))

  def sequence(steps: Rule*) = SequentialRule(steps.toList)

  def choice(cases: Rule*) = ChoiceRule(cases.toList)

  def recover(rule: Rule, exception: String) =
    RecoveryRule(rule, Some(exception))

  def recover(rule: Rule, nodeTag: String, exception: String) =
    RecoveryRule(rule, Some(exception), Some(nodeTag))

  def expression(tag: String, atom: Rule) = ExpressionRule(tag, atom)

  def expression(atom: Rule) = ExpressionRule("result", atom)
}