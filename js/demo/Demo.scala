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

package name.lakhin.eliah.projects.papacarlo.js.demo

import scala.scalajs.js
import js.annotation.{ JSName, JSExport }

import name.lakhin.eliah.projects.papacarlo.lexis.TokenReference
import name.lakhin.eliah.projects.papacarlo.examples.Json

@JSExport
object Demo {
  private val lexer = Json.lexer
  private val syntax = Json.syntax(lexer)

  @JSExport
  def input(text: String) {
    lexer.input(text)
  }

  @JSExport
  def input(text: String,
            fromLine: Int,
            fromChar: Int,
            toLine: Int,
            toChar: Int) {
    lexer.input(text, fromLine -> fromChar, toLine -> toChar)
  }

  @JSExport
  def getErrors() = {
    val result = new js.Array[js.Dynamic]

    for (error <- syntax.getErrors) {
      val from = tokenCursor(error.from)
      val to = tokenCursor(error.to, after = true)

      result.push(js.Dynamic.literal(
        "from" -> from,
        "to" -> to,
        "description" -> error.description
      ))
    }

    result
  }

  @JSExport
  def getNodeCount() = syntax.nodes.size

  @JSExport
  def getRootNode() = syntax.getRootNode

  private def tokenCursor(token: TokenReference, after: Boolean = false) = {
    val pair = token.collection.cursor(token.index + (if (after) 1 else 0))

    js.Dynamic.literal("line" -> (pair._1 - 1), "ch" -> (pair._2 - 1))
  }
}