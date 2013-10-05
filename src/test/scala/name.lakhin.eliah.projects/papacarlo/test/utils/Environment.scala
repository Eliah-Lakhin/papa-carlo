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

abstract class Environment(lexerConstructor: () => Lexer) {
  var shortOutput = false

  protected val lexer = lexerConstructor()

  final def input(text: String) = {
    val start = System.currentTimeMillis()
    lexer.input(text)
    val end = System.currentTimeMillis()

    end - start
  }

  def prepare()
  def getResult: String

  protected final def unionLog(log: List[(Symbol, String)]) = {
    val result = new StringBuilder

    for (log <- log.reverse) {
      result ++= " > " + log._1.name + ":\n"
      result ++= log._2
      result ++= "\n\n"
    }

    result.toString()
  }
}