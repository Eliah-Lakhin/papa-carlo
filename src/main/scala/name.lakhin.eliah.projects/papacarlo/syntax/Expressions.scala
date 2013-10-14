package name.lakhin.eliah.projects
package papacarlo.syntax

import name.lakhin.eliah.projects.papacarlo.syntax.rules.ExpressionRule

object Expressions {
  def infix(rule: ExpressionRule,
            operator: String,
            precedence: Int,
            rightAssociativity: Boolean = true) {
    val leftBindingPower = precedence * 10
    val rightBindingPower =
      leftBindingPower + (if (rightAssociativity) 1 else 0)

    rule.parselet(operator)
      .leftBindingPower(leftBindingPower)
      .leftDenotation {
        (expression, left) =>
          val operatorReference = expression.currentTokenReference
          val node = new Node(operator, operatorReference, operatorReference)

          node.branches += "left" -> List(left)

          for (right <- expression.parseRight(rightBindingPower))
            node.branches += "right" -> List(right)

          node
      }
  }

  def postfix(rule: ExpressionRule, operator: String, precedence: Int) {
    rule.parselet(operator)
      .leftBindingPower(precedence * 10)
      .leftDenotation {
        (expression, left) =>
          val operatorReference = expression.currentTokenReference
          val node = new Node(operator, operatorReference, operatorReference)
          node.branches += "operand" -> List(left)
          node
      }
  }

  def prefix(rule: ExpressionRule, operator: String, precedence: Int) {
    val power = precedence * 10

    rule.parselet(operator)
      .leftBindingPower(power)
      .nullDenotation {
        expression =>
          val operatorReference = expression.currentTokenReference
          val node = new Node(operator, operatorReference, operatorReference)
          for (right <- expression.parseRight(power))
            node.branches += "operand" -> List(right)
          node
      }
  }

  def group(rule: ExpressionRule, open: String, close: String) {
    rule.parselet(open)
      .leftBindingPower(0)
      .nullDenotation {
        expression =>
          val result = expression.parseRight()

          expression.consume(")")

          result.getOrElse(expression.placeholder)
      }
  }
}
