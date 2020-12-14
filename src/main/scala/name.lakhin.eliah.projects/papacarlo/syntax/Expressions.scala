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
package papacarlo.syntax

import name.lakhin.eliah.projects.papacarlo.syntax.rules.ExpressionRule

object Expressions {
  def infix(rule: ExpressionRule,
            operator: String,
            precedence: Int,
            rightAssociativity: Boolean = false): Unit = {
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

  def postfix(rule: ExpressionRule, operator: String, precedence: Int): Unit = {
    rule.parselet(operator)
      .leftBindingPower(Int.MaxValue - precedence * 10)
      .leftDenotation {
        (expression, left, operatorReference) =>
          val node = new Node(operator, operatorReference, operatorReference)
          node.branches += ExpressionRule.Operand -> List(left)
          node
      }
  }

  def prefix(rule: ExpressionRule, operator: String, precedence: Int): Unit = {
    val power = Int.MaxValue - precedence * 10

    rule.parselet(operator).nullDenotation {
      (expression, operatorReference) =>
        val node = new Node(operator, operatorReference, operatorReference)
        for (right <- expression.parseRight(power))
          node.branches += ExpressionRule.Operand -> List(right)
        node
    }
  }

  def group(rule: ExpressionRule, open: String, close: String): Unit = {
    rule.parselet(open).nullDenotation {
      (expression, _) =>
        val result = expression.parseRight()

        expression.consume(")")

        result.getOrElse(expression.placeholder)
    }
  }
}
