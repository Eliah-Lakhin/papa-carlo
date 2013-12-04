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
package papacarlo

import name.lakhin.eliah.projects.papacarlo.lexis.Token
import name.lakhin.eliah.projects.papacarlo.syntax.{State, Rule, Node, Cache}
import name.lakhin.eliah.projects.papacarlo.utils.{Signal, Registry}
import name.lakhin.eliah.projects.papacarlo.syntax.rules.{NamedRule,
  ReferentialRule}

final class Syntax(val lexer: Lexer) {

  final class RuleDefinition(val productKind: String,
                             bodyInitializer: => Rule,
                             val interceptor: Option[Node => Node] = None,
                             val cachable: Boolean = false) {
    lazy val body = bodyInitializer

    private[Syntax] def copy(interceptor: Option[Node => Node] = interceptor,
                             cachable: Boolean = cachable) =
      new RuleDefinition(productKind, bodyInitializer, interceptor, cachable)
  }

  var rules = Map.empty[String, RuleDefinition]
  private[papacarlo] var nodes = new Registry[Node]
  private[papacarlo] var cache = Map.empty[Int, Cache]

  val onCacheCreate = new Signal[Cache]
  val onCacheRemove = new Signal[Cache]
  val onCacheInvalidate = new Signal[Cache]
  val onNodeCreate = nodes.onAdd
  val onNodeMerge = new Signal[Node]
  val onNodeRemove = nodes.onRemove
  val onParseStep = new Signal[Seq[Token]]
  val onRuleEnter = new Signal[(Rule, State)]
  val onRuleLeave = new Signal[(Rule, State, Int)]

  lexer.fragments.onInvalidate.bind(fragment => {
    var candidate = Option(fragment)

    while (candidate.exists(fragment => !cache.contains(fragment.id)))
      candidate = candidate.flatMap(_.parent)

    for (cache <-
         cache.get(candidate.getOrElse(lexer.fragments.rootFragment).id)) {
      onCacheInvalidate.trigger(cache)
      cache.invalidate()
    }
  })

  def getErrors = cache.values.map(cache => cache.errors).flatten.toList

  def rule(name: String)(body: => Rule) = {
    val definition = new RuleDefinition(name, body)

    rules += name -> definition

    NamedRule(name, ReferentialRule(name))
  }

  def mainRule(name: String)(body: => Rule) = {
    val rootFragment = lexer.fragments.rootFragment
    val rootNode = new Node(name, rootFragment.begin, rootFragment.end)

    nodes.add(id => {rootNode.id = id; rootNode})

    new Cache(this, rootFragment, rootNode)

    rule(name)(body)
  }

  def cachable(refs: Rule*) {
    for (rule <- refs)
      rule match {
        case NamedRule(_, rule: Rule) => cachable(rule)

        case ReferentialRule(name, _) =>
          for (definition <- rules.get(name)) {
            rules += name -> definition.copy(cachable = true,
              interceptor = None)
          }

        case _ =>
      }
  }

  def intercept(ref: Rule)(interceptor: Node => Node) {
    ref match {
      case NamedRule(_, rule: Rule) => intercept(rule)(interceptor)

      case ReferentialRule(name, _) =>
        for (definition <- rules.get(name)) {
          rules += name -> definition.copy(cachable = false,
            interceptor = Some(interceptor))
        }

      case _ =>
    }
  }
}