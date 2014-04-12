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
import name.lakhin.eliah.projects.papacarlo.syntax.Node
import name.lakhin.eliah.projects.papacarlo.examples.Json

@JSExport
object Demo {
  private val lexer = Json.lexer
  private val syntax = Json.syntax(lexer)
  private var addedNodes = List.empty[Node]
  private var removedNodes = List.empty[Node]
  
  syntax.onNodeCreate.bind { node => addedNodes ::= node }
  syntax.onNodeRemove.bind { node => removedNodes ::= node }

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
    listToArray(syntax.getErrors.map {
      error =>
        val from = tokenCursor(error.from)
        val to = tokenCursor(error.to, after = true)

        js.Dynamic.literal(
          "from" -> from,
          "to" -> to,
          "description" -> error.description
        )
    })
  }

  @JSExport
  def getNodeStats() = {
    val result = js.Dynamic.literal(
      "total" -> syntax.nodes.size,
      "added" -> listToArray(addedNodes.map(exportNode)),
      "removed" -> listToArray(removedNodes.map(exportNode))
    )

    addedNodes = Nil
    removedNodes = Nil

    result
  }

  private def listToArray(list: List[js.Any]) = {
    val result = new js.Array[js.Any]

    for (element <- list) result.push(element)

    result
  }

  private def mapToObject(map: Map[String, js.Array[js.Any]]) = {
    val result = js.Dictionary.empty[js.Any]

    for ((key, values) <- map) result(key) = values

    result
  }

  private def exportNode(node: Node) = {
    val parentId = node.getParent.map(_.getId).getOrElse(-1)

    js.Dynamic.literal(
      "id" -> node.getId,
      "parent" -> parentId,
      "kind" -> node.getKind,
      "values" -> mapToObject(node.getValues
        .map {
          case (key, values) =>
            key -> listToArray(values.map(s => s.asInstanceOf[js.String]))
        }
      )
    )
  }

  private def tokenCursor(token: TokenReference, after: Boolean = false) = {
    val pair = token.collection.cursor(token.index + (if (after) 1 else 0))

    js.Dynamic.literal("line" -> (pair._1 - 1), "ch" -> (pair._2 - 1))
  }
}

