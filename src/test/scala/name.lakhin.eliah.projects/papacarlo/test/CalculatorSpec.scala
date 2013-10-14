package name.lakhin.eliah.projects
package papacarlo.test

import name.lakhin.eliah.projects.papacarlo.examples.Calculator
import name.lakhin.eliah.projects.papacarlo.test.utils.ParserSpec

class CalculatorSpec extends ParserSpec(
  parserName = "calculator",
  lexerConstructor = Calculator.lexer _,
  syntaxConstructor = Calculator.syntax
)
