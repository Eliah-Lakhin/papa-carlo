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

import name.lakhin.eliah.projects.papacarlo.utils.Bounds

final case class State(virtualPosition: Int = 0,
                       issues: List[Issue] = Nil,
                       products: List[(String, Node)] = Nil,
                       captures: List[(String, Bounds)] = Nil) {
  def issue(description: String) =
    copy(issues = Issue(Bounds.cursor(virtualPosition), description) :: issues)

  def issue(range: Bounds, description: String) =
    copy(issues = Issue(range, description) :: issues)
}
