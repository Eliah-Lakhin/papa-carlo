package name.lakhin.eliah.projects
package papacarlo.syntax

import name.lakhin.eliah.projects.papacarlo.syntax.rules.ExpressionRule

object Expressions {
  def infix(rule: ExpressionRule,
            operator: String,
            precedence: Int,
            rightAssociativity: Boolean = false) {
    val leftBindingPower = Int.MaxValue - precedence * 10
    val rightBindingPower =
      leftBindingPower - (if (rightAssociativity) 1 else 0)

    rule.parselet(operator)
      .leftBindingPower(leftBindingPower)
      .leftDenotation {
        (expression, left, operatorReference) =>
          val node = new Node(operator, operatorReference, operatorReference)

          node.branches += "left" -> List(left)

          for (right <- expression.parseRight(rightBindingPower))
            node.branches += "right" -> List(right)

          node
      }
  }

  def postfix(rule: ExpressionRule, operator: String, precedence: Int) {
    rule.parselet(operator)
      .leftBindingPower(Int.MaxValue - precedence * 10)
      .leftDenotation {
        (expression, left, operatorReference) =>
          val node = new Node(operator, operatorReference, operatorReference)
          node.branches += "operand" -> List(left)
          node
      }
  }

  def prefix(rule: ExpressionRule, operator: String, precedence: Int) {
    val power = Int.MaxValue - precedence * 10

    rule.parselet(operator).nullDenotation {
      (expression, operatorReference) =>
        val node = new Node(operator, operatorReference, operatorReference)
        for (right <- expression.parseRight(power))
          node.branches += "operand" -> List(right)
        node
    }
  }

  def group(rule: ExpressionRule, open: String, close: String) {
    rule.parselet(open).nullDenotation {
      (expression, _) =>
        val result = expression.parseRight()

        expression.consume(")")

        result.getOrElse(expression.placeholder)
    }
  }
}
