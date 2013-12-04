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

import name.lakhin.eliah.projects.papacarlo.syntax.{Node, Issue, Session, Rule}
import name.lakhin.eliah.projects.papacarlo.utils.Bounds
import name.lakhin.eliah.projects.papacarlo.syntax.Result._

final case class RecoveryRule(rule: Rule,
                              exception: Option[String] = None,
                              branch: Option[String] = None)
  extends Rule {

  def apply(session: Session) = {
    val initialState = session.state
    var result = rule(session)

    if (result == Failed) {
      val issues =
        exception
          .map {
            description =>
              Issue(
                Bounds.cursor(initialState.virtualPosition),
                description
              ) :: initialState.issues
          }
          .getOrElse(session.state.issues)

      branch match {
        case Some(tag) =>
          val place = session.reference(session
            .relativeIndexOf(initialState.virtualPosition))

          session.state = initialState.copy(
            issues = issues,
            products = (
              tag,
              new Node(RecoveryRule.PlaceholderKind, place, place)
            ) :: initialState.products
          )

        case None => session.state = initialState.copy(issues = issues)
      }
      result = Recoverable
    }

    result
  }

  override val show = {
    val atom = rule.showOperand(Int.MaxValue) + ".recover" +
      exception.map("(" + _ + ")").getOrElse("")

    branch match {
      case Some(branch: String) => atom + " -> " + atom -> 5
      case None => atom -> Int.MaxValue
    }
  }
}

object RecoveryRule {
  val PlaceholderKind = "placeholder"
}
