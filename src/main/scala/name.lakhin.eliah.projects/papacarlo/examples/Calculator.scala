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
      val rule = expression(
        "tree",
        branch("operand", recover(number, "operand required"))
      )

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
