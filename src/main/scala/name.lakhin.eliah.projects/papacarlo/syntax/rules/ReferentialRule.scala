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

import name.lakhin.eliah.projects.papacarlo.syntax._
import name.lakhin.eliah.projects.papacarlo.utils.Bounds
import name.lakhin.eliah.projects.papacarlo.syntax.Result._

final case class ReferentialRule(name: String,
                                 tag: Option[String] = None)
  extends Rule {

  def apply(session: Session) = {
    val packratKey =
      session.relativeIndexOf(session.state.virtualPosition) + name

    session.packrat.lift(packratKey) match {
      case Some(packrat) =>
        session.state = packrat.state
          .copy(virtualPosition = session.virtualIndexOf(packrat.range.until))

        packrat.result

      case None =>
        val initialPosition = session.state.virtualPosition

        val result = session.getCache(initialPosition, name) match {
          case Some(cache) =>
            session.state = session.state.copy(
              virtualPosition = session.virtualIndexOf(cache.end.index -
                session.sourceTokensOffset) + 1,
              products = tag
                .map(tag => (tag, cache) :: session.state.products)
                .getOrElse(session.state.products)
            )

            Successful

          case None => performReferredRule(session)
        }

        session.packrat += Pair(packratKey, Packrat(
          name,
          session.relativeSegmentOf(Bounds(
            initialPosition,
            session.state.virtualPosition
          )),
          result,
          session.state
        ))

        result
    }
  }

  private def performReferredRule(session: Session) = {
    var result = Failed
    val initialState = session.state

    for (rule <- session.syntax.rules.get(name)) {
      tag match {
        case Some(tag: String) =>
          session.state =
            State(virtualPosition = session.state.virtualPosition)
          result = rule.body(session)

          if (result != Failed) {
            var node =
              (if (!rule.cachable
                && session.state.captures.isEmpty
                && session.state.products.size == 1)
                session.state.products
                  .headOption
                  .flatMap {
                    product =>
                      if (product._1 == "result") Some(product._2)
                      else None
                  }
              else None).getOrElse {
                val begin = session.reference(
                  session.relativeIndexOf(initialState.virtualPosition)
                )
                val end = session.reference(
                  session.relativeIndexOf(session.state.virtualPosition - 1)
                )

                val node = new Node(rule.productKind, begin, end)

                node.cachable = rule.cachable
                node.branches =
                  session.state.products.groupBy(_._1)
                    .mapValues(_.map(_._2).reverse)
                node.references =
                  session.state.captures.groupBy(_._1)
                    .mapValues(_.map(_._2.iterator
                      .map(session.reference)).flatten)

                node
              }

            for (interceptor <- rule.interceptor) node = interceptor(node)

            session.state = initialState.copy(
              virtualPosition = session.state.virtualPosition,
              products = (tag, node) :: initialState.products,
              issues = session.state.issues ::: initialState.issues
            )
          }

        case None => result = rule.body(session)
      }
    }

    result
  }
}
