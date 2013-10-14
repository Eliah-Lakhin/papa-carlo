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

import name.lakhin.eliah.projects.papacarlo.syntax.{Rule, Session}
import name.lakhin.eliah.projects.papacarlo.syntax.Result._

final case class ChoiceRule(choices: List[Rule]) extends Rule {
  def apply(session: Session): Int = {
    val initialState = session.state
    var bestResult = (Failed, session.state)

    for (choice <- choices) {
      choice(session) match {
        case Successful => return Successful

        case Recoverable =>
          if (bestResult._1 < Recoverable ||
            bestResult._2.virtualPosition >= session.state.virtualPosition)
            bestResult = (Recoverable, session.state)

          session.state = initialState

        case Failed =>
          if (bestResult._1 == Failed) bestResult = (Failed, session.state)

          session.state = initialState
      }
    }

    session.state = bestResult._2
    bestResult._1
  }
}
