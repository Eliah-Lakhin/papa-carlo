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

import name.lakhin.eliah.projects.papacarlo.utils.Bounds
import name.lakhin.eliah.projects.papacarlo.syntax.{Rule, Session}
import name.lakhin.eliah.projects.papacarlo.syntax.InterpretationResult._

final case class CapturingRule(subrule: Rule,
                               tag: String) extends Rule {
  def apply(session: Session) = {
    val initialState = session.state
    var result = subrule(session)

    if (initialState.virtualPosition < session.state.virtualPosition) {
      session.state = session.state.copy(captures =
        (
          tag,
          session.relativeSegmentOf(Bounds(
            initialState.virtualPosition,
            session.state.virtualPosition
          ))
        ) :: session.state.captures
      )
    } else if (result != Failed) {
      session.state = initialState.copy(issues = session.state.issues)
      result = Failed
    }

    result
  }
}
