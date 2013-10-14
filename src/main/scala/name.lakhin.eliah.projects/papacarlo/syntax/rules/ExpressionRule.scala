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
package papacarlo.syntax.rules

import name.lakhin.eliah.projects.papacarlo.syntax.{Result, Node, Session, Rule}

final case class ExpressionRule(tag: String, atom: Rule) extends Rule {

  final case class ExpressionState(private[ExpressionRule] val session: Session)

  final class Parselet {
    private[ExpressionRule] var lbp: Int = 0
    private[ExpressionRule] var nud = Option.empty[ExpressionState => Node]
    private[ExpressionRule] var led =
      Option.empty[(ExpressionState, Node) => Node]

    private var partParsers = Map.empty[String, TokenRule]

    def leftBindingPower(lbp: Int) = {
      this.lbp = lbp

      this
    }

    def nullDenotation(procedure: ExpressionState => Node) = {
      this.nud = Some(procedure)

      this
    }

    def leftDenotation(procedure: (ExpressionState, Node) => Node) = {
      this.led = Some(procedure)

      this
    }

    def parts(parts: String*) = {
      this.partParsers ++=
        parts.map(token => token -> new TokenRule(token))

      this
    }

    def parseRight(expression: ExpressionState, rightBindingPower: Int) =
      ExpressionRule.this.parse(expression, rightBindingPower)

    def currentTokenReference(expression: ExpressionState) =
      expression.session.reference(expression.session
        .relativeIndexOf(expression.session.state.virtualPosition))

    def consume(expression: ExpressionState, expectedToken: String) = {
      val currentPosition = expression.session.state.virtualPosition

      partParsers.get(expectedToken)
        .map(_(expression.session))
        .flatMap(result =>
          if (result != Result.Failed)
            Some(expression.session.reference(expression.session
              .relativeIndexOf(currentPosition)))
          else
            None)
    }
  }

  private var parselets = Map.empty[String, Parselet]

  def apply(session: Session): Int = {
    0
  }

  def parselet(operator: String) =
    parselets.getOrElse(operator, {
      val parselet = new Parselet

      parselets += operator -> parselet

      parselet
    })

  private def token(session: Session) =
    session
      .tokens
      .lift(session.state.virtualPosition)
      .map(_.kind)
      .getOrElse(TokenRule.EndOfFragmentKind)

  private def next(session: Session) {
    session.state = session.state.copy(virtualPosition =
      session.state.virtualPosition + 1)
  }

  private def operand(session: Session) = {
    val initialState = session.state

    atom(session)

    val result = session.state.products
      .headOption
      .filter(_._1 == "operand" && session.state.products.length >
        initialState.products.length)
      .map(_._2)

    session.state.copy(products = initialState.products)

    result
  }

  private def parse(expression: ExpressionState,
                    rightBindingPower: Int): Option[Node] = {
    var step = parselets.get(token(expression.session))
      .flatMap(parselet => parselet.nud.map(nud => {
        next(expression.session)
        (Some(nud(expression)), parselet.lbp)
      }))
      .getOrElse((operand(expression.session), 0))

    while (step._1.isDefined && rightBindingPower > step._2)
      step = parselets.get(token(expression.session))
      .flatMap(parselet => parselet.led.map(led => {
        next(expression.session)
        (step._1.map(left => led(expression, left)), parselet.lbp)
      }))
      .getOrElse(step.copy(_2 = Int.MaxValue))

    step._1
  }
}
