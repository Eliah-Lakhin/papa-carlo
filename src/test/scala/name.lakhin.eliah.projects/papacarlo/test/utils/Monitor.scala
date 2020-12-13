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

abstract class Monitor(lexer: Lexer) {
  var shortOutput = false

  final def input(text: String) = {
    val start = System.currentTimeMillis()
    lexer.input(text)
    val end = System.currentTimeMillis()

    end - start
  }

  def prepare()
  def getResult: String
  def release()

  protected final def unionLog(log: List[(Symbol, String)],
                               enterLeaveIndentation: Boolean = false) = {
    val result = new StringBuilder
    var indent = 0

    for ((title, details) <- log.reverse) {
      if (title == 'leave) indent -= 1

      val offset = "  " * (indent * 2)

      result ++= offset + " > " + title.name + ":\n"
      result ++= details.split("\n", -1).map(offset + _).mkString("\n")
      result ++= "\n\n"

      if (title == 'enter) indent += 1
    }

    result.toString()
  }
}