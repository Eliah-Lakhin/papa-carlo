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

import name.lakhin.eliah.projects.papacarlo.{Syntax, Lexer}
import name.lakhin.eliah.projects.papacarlo.lexis.{Contextualizer, Matcher,
  Tokenizer}
import name.lakhin.eliah.projects.papacarlo.syntax.{Expressions, Rule}

object Calculator {
  private def tokenizer = {
    val tokenizer = new Tokenizer()

    import tokenizer._
    import Matcher._

    tokenCategory(
      "whitespace",
      oneOrMore(anyOf(" \t\f\n"))
    ).skip

    tokenCategory(
      "number",
      choice(
        chunk("0"),
        sequence(rangeOf('1', '9'), zeroOrMore(rangeOf('0', '9')))
      )
    )

    terminals("(", ")", "%", "+", "-", "*", "/")

    tokenizer
  }

  def lexer = new Lexer(tokenizer, new Contextualizer)

  def syntax(lexer: Lexer) = new {
    val syntax = new Syntax(lexer)

    import syntax._
    import Rule._
    import Expressions._

    mainRule("expression") {
      val rule =
        expression(branch("operand", recover(number, "operand required")))

      group(rule, "(", ")")
      postfix(rule, "%", 1)
      prefix(rule, "+", 2)
      prefix(rule, "-", 2)
      infix(rule, "*", 3)
      infix(rule, "/", 3, rightAssociativity = true)
      infix(rule, "+", 4)
      infix(rule, "-", 4)

      rule
    }

    val number = rule("number") {capture("value", token("number"))}
  }.syntax
}
