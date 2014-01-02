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
  final class RuleDefinition(val name: String) {
    private[papacarlo] var productKind: String = name
    private[papacarlo] var cachingFlag = false
    private[papacarlo] var transformer: Option[Node => Node] = None

    private var constructor = Option.empty[() => Rule]
    private[papacarlo] lazy val body = constructor match {
      case Some(bodyConstructor) => bodyConstructor()
      case _ => throw new RuntimeException("Rule " + name + " undefined")
    }

    def produce(kind: String) = {
      productKind = kind

      this
    }

    def main = {
      if (Syntax.this.mainRule.isEmpty) Syntax.this.mainRule = Some(name)

      this
    }

    def cachable = {
      cachingFlag = true
      transformer = None

      this
    }

    def transform(transformer: Node => Node) = {
      cachingFlag = false
      this.transformer = Some(transformer)

      this
    }

    def reference = NamedRule(name, ReferentialRule(name))

    def apply(body: => Rule) = {
      constructor match {
        case None =>
          constructor = Some(() => body)

          if (mainRule.exists(_ == name)) {
            val rootFragment = lexer.fragments.rootFragment
            val rootNode = new Node(name, rootFragment.begin,
              rootFragment.end)

            nodes.add(id => {rootNode.id = id; rootNode})

            new Cache(Syntax.this, rootFragment, rootNode)

            Syntax.this.rootNode = Some(rootNode)
          }

        case _ =>
      }

      reference
    }
  }

  private[papacarlo] var rules = Map.empty[String, RuleDefinition]
  private var rootNode: Option[Node] = None
  private var mainRule: Option[String] = None
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

  lexer.fragments.onInvalidate.bind {
    case (fragment, range) =>
      var candidate = Option(fragment)

      while (candidate.exists(fragment => !cache.contains(fragment.id)))
        candidate = candidate.flatMap(_.parent)

      for (cache <-
           cache.get(candidate.getOrElse(lexer.fragments.rootFragment).id)) {
        onCacheInvalidate.trigger(cache)
        cache.invalidate(range)
      }
  }

  def getErrors = cache.values.map(cache => cache.errors).flatten.toList

  def rule(name: String) =
    rules.get(name) match {
      case Some(definition) => definition

      case None =>
        val definition = new RuleDefinition(name)

        rules += name -> definition

        definition
    }

  def getRootNode = rootNode

  def getNode(id: Int) = nodes.get(id)
}