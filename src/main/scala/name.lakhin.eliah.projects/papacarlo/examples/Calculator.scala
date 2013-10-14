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
      postfix(rule, "%", 3)
      prefix(rule, "+", 4)
      prefix(rule, "-", 4)
      infix(rule, "*", 5)
      infix(rule, "/", 5, rightAssociativity = true)
      infix(rule, "+", 6)
      infix(rule, "-", 6)

      rule
    }

    val number = rule("number") {capture("value", token("number"))}
  }.syntax
}
