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
package papacarlo

import name.lakhin.eliah.projects.papacarlo.utils.Bounds
import name.lakhin.eliah.projects.papacarlo.lexis._
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

final class Lexer(tokenizer: Tokenizer,
                  contextualizer: Contextualizer) {
  private var code = ""
  private val tokens = new TokenCollection(contextualizer.lineCutTokens)

  val fragments = new FragmentController(contextualizer, tokens)

  def input(newCode: String) {
    val prepared = prepareCode(newCode)

    if (code.isEmpty) inputPrepared(prepared, Bounds(0, 0))
    else {
      val actualCode = code.substring(0, code.length - 1)
      if (newCode != actualCode) {
        val difference = computeDifference(actualCode, newCode)
        inputPrepared(
          newCode.substring(difference._1, newCode.length - difference._2),
          Bounds(difference._1, actualCode.length - difference._2)
        )
      }
    }
  }

  def input(newCode: String, start: (Int, Int), end: (Int, Int)) =
    inputPrepared(
      prepareCode(newCode),
      Bounds(cursorToOffset(start), cursorToOffset(end))
    )

  def reference(index: Int) =
    tokens.references
      .lift(index)
      .getOrElse(if (index < 0) tokens.head else tokens.last)

  def rangeToString(range: Bounds) = tokens.range(range)

  def highlight(bounds: Bounds, limit: Option[Int] = None) =
    tokens.highlight(bounds, limit)

  private def computeDifference(first: String, second: String) = {
    val head = first.iterator
      .zip(second.iterator)
      .takeWhile(pair => pair._1 == pair._2)
      .length

    val tail = first.substring(head).reverse.iterator
      .zip(second.substring(head).reverse.iterator)
      .takeWhile(pair => pair._1 == pair._2)
      .length

    (head, tail)
  }

  private def prepareCode(source: String) =
    source.replace("\r\n", "\n").replace("\r", "\n")

  private def cursorToOffset(cursor: (Int, Int)) = {
    var offset = 0
    var current = (0, 0)

    while (offset < code.length - 1 && (current._1 < cursor._1 ||
      (cursor._1 == current._1 && cursor._2 < current._2))) {

      if (code.charAt(offset) == '\n') current = (current._1 + 1, 0)
      else current = (current._1, current._1 + 1)

      offset += 1
    }

    offset
  }

  private def inputPrepared(code: String, range: Bounds) {
    val tokenBounds = align(range)
    val codeBounds = tokenBounds.enlarge(0, -1).map(index =>
      tokens.descriptions.take(index).foldLeft(0)((offset, token) =>
        offset + token.value.length))
    val oldCode = codeBounds.substring(this.code)
    val targetCode = range.shift(-codeBounds.from).replace(oldCode, code)
    val targetTokens = new ListBuffer[Token]

    for (line <- targetCode.split("\n", -1)) {
      val lineTokens = tokenizer.tokenize(line)
      targetTokens ++= lineTokens
      targetTokens += Token.lineBreak
    }

    tokens.write(tokenBounds, targetTokens)
    this.code = codeBounds.replace(this.code, targetCode)

    if (this.code.nonEmpty && this.code.charAt(this.code.length - 1) != '\n') {
      this.code += '\n'
    }
  }

  private def align(range: Bounds) = {
    var start = 0
    var end = 0
    var line = 0
    var offset = 0
    var index = 0

    breakable {
      for (token <- this.tokens.descriptions) {
        val nextOffset = offset + token.value.length

        if (token.kind == Token.LineBreakKind) {
          if (offset < range.from) {
              start = index
          }
          if (range.until < nextOffset) {
              end = index + 1
              break()
          }

          line += 1
        }

        offset = nextOffset

        index += 1
      }
    }

    Bounds(start, end)
  }
}
