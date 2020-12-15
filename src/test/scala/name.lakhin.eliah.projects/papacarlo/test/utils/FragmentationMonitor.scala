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

import name.lakhin.eliah.projects.papacarlo.Lexer
import name.lakhin.eliah.projects.papacarlo.lexis.Fragment
import name.lakhin.eliah.projects.papacarlo.utils.Bounds

private final class FragmentationMonitor(lexer: Lexer) extends Monitor(lexer) {
  private var fragmentLog = List.empty[(Symbol, String)]

  val onCreate = (fragment: Fragment) =>
    fragmentLog ::= ('create, fragmentToString(fragment))

  val onInvalidate = (pair: (Fragment, Bounds)) =>
    fragmentLog ::= ('invalidate, fragmentToString(pair._1))

  val onRemove = (fragment: Fragment) =>
    fragmentLog ::= ('remove, fragmentToString(fragment))

  private def fragmentToString(fragment: Fragment) =
    if (shortOutput) fragment.toString
    else fragment.highlight(Option(10))

  def getResult = unionLog(fragmentLog)

  def prepare() {
    fragmentLog = Nil
    lexer.fragments.onCreate.bind(onCreate)
    lexer.fragments.onInvalidate.bind(onInvalidate)
    lexer.fragments.onRemove.bind(onRemove)
  }

  def release() {
    fragmentLog = Nil
    lexer.fragments.onCreate.unbind(onCreate)
    lexer.fragments.onInvalidate.unbind(onInvalidate)
    lexer.fragments.onRemove.unbind(onRemove)
  }
}