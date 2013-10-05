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
package papacarlo.examples

import name.lakhin.eliah.projects.papacarlo.lexis.{Tokenizer, Contextualizer}
import name.lakhin.eliah.projects.papacarlo.Lexer

final class JsonLexer {
  val tokenizer = {
    val tokenizer = new Tokenizer()

    import tokenizer._
    import Tokenizer._

    tokenCategory("whitespace", oneOrMore(anyOf(" \t\f\n"))).skip

    tokenCategory("string", chunk("\"") &
      oneOrMore(anyExceptOf("\n\r\\\"") | chunk("\\\"") | chunk("\\\\") |
        chunk("\\/") | chunk("\\b") | chunk("\\f") | chunk("\\n") |
        chunk("\\r") | chunk("\\t") | (chunk("\\u") &
          repeat(rangeOf('a', 'f') | rangeOf('A', 'F') |
            rangeOf('0', '9'), 4))) & chunk("\""))

    tokenCategory("number", optional(chunk("-")) & (chunk("0") |
      (rangeOf('1', '9') & zeroOrMore(rangeOf('0', '9')))) &
      optional(chunk(".") & oneOrMore(rangeOf('0', '9'))) &
      optional(anyOf("eE") & optional(anyOf("+-")) &
        oneOrMore(rangeOf('0', '9'))))

    tokenCategory("alphanum", oneOrMore(rangeOf('a', 'z')))

    terminals(",", ":", "{", "}", "[", "]")

    keywords("true", "false", "null")

    tokenizer
  }

  val contextualizer = {
    val contextualizer = new Contextualizer

    import contextualizer._

    trackContext("[", "]").allowCaching
    trackContext("{", "}").allowCaching

    contextualizer
  }

  val lexer = new Lexer(tokenizer, contextualizer)
}