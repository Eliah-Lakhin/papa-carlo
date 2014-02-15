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

import name.lakhin.eliah.projects.papacarlo.lexis.{Matcher, Tokenizer,
  Contextualizer}
import name.lakhin.eliah.projects.papacarlo.{Syntax, Lexer}
import name.lakhin.eliah.projects.papacarlo.syntax.Rule
import name.lakhin.eliah.projects.papacarlo.syntax.rules.NamedRule

object Json {
  private def tokenizer = {
    val tokenizer = new Tokenizer()

    import tokenizer._
    import Matcher._

    tokenCategory(
      "whitespace",
      oneOrMore(anyOf(" \t\f\n"))
    ).skip

    tokenCategory(
      "string",
      sequence(
        chunk("\""),
        oneOrMore(choice(
          anyExceptOf("\n\r\\\""),
          sequence(chunk("\\"), anyOf("\"\\/bfnrt")),
          sequence(
            chunk("\\u"),
            repeat(
              choice(rangeOf('a', 'f'), rangeOf('A', 'F'), rangeOf('0', '9')),
              times = 4
            )
          )
        )),
        chunk("\"")
      )
    )

    tokenCategory(
      "number",
      sequence(
        optional(chunk("-")),
        choice(
          chunk("0"),
          sequence(rangeOf('1', '9'), zeroOrMore(rangeOf('0', '9')))
        ),
        optional(sequence(chunk("."), oneOrMore(rangeOf('0', '9')))),
        optional(sequence(
          anyOf("eE"),
          optional(anyOf("+-")),
          oneOrMore(rangeOf('0', '9'))
        ))
      )
    )

    tokenCategory(
      "alphanum",
      oneOrMore(rangeOf('a', 'z'))
    )

    terminals(",", ":", "{", "}", "[", "]")

    keywords("true", "false", "null")

    tokenizer
  }

  private def contextualizer = {
    val contextualizer = new Contextualizer

    import contextualizer._

    trackContext("[", "]").allowCaching
    trackContext("{", "}").allowCaching

    contextualizer
  }

  def lexer = new Lexer(tokenizer, contextualizer)

  def syntax(lexer: Lexer) = new {
    val syntax = new Syntax(lexer)

    import syntax._
    import Rule._

    val jsonObject = rule("object").cachable.main {
      sequence(
        token("{"),
        zeroOrMore(
          branch("entry", objectEntry),
          separator =
            recover(token(","), "object entries must be separated with , sign")
        ),
        recover(token("}"), "object must end with } sign")
      )
    }

    val objectEntry = rule("entry") {
      sequence(
        capture("key", token("string")),
        token(":"),
        branch("value", jsonValue)
      )
    }

    val jsonArray = rule("array").cachable {
      sequence(
        token("["),
        zeroOrMore(
          branch("value", jsonValue),
          separator =
            recover(token(","), "array entries must be separated with , sign")
        ),
        recover(token("]"), "array must end with ] sign")
      )
    }

    val jsonString = rule("string") {
      capture("value", token("string"))
    }

    val jsonNumber = rule("number") {
      capture("value", token("number"))
    }

    val jsonBoolean = rule("boolean") {
      capture("value", choice(token("true"), token("false")))
    }

    val jsonNull = rule("null") {
      token("null")
    }

    val jsonValue: NamedRule = subrule("value") {
      choice(jsonString, jsonNumber, jsonObject, jsonArray, jsonBoolean,
        jsonNull)
    }
  }.syntax
}