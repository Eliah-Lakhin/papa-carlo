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
package papacarlo.test.utils

import name.lakhin.eliah.projects.papacarlo.syntax.rules.RecoveryRule
import name.lakhin.eliah.projects.papacarlo.utils.Bounds
import name.lakhin.eliah.projects.papacarlo.{Syntax, Lexer}
import name.lakhin.eliah.projects.papacarlo.syntax.Node

final class NodeMonitor(lexerConstructor: () => Lexer,
                            syntaxConstructor: Lexer => Syntax)
  extends SyntaxMonitor(lexerConstructor, syntaxConstructor) {

  private var nodeLog = List.empty[(Symbol, String)]

  syntax.onNodeCreate.bind {
    node => nodeLog ::= ('create, nodeInfo(node))
  }

  syntax.onNodeMerge.bind {
    node => nodeLog ::= ('merge, node.prettyPrint())
  }

  syntax.onNodeRemove.bind {
    node => nodeLog ::= ('remove, nodeInfo(node))
  }

  private def nodeInfo(node: Node) =
    node.toString + (
      if (shortOutput) " " + lexer.rangeToString(node.getRange)
      else {
        var range = node.getRange

        if (node.getKind == RecoveryRule.PlaceholderKind) {
          range = Bounds.cursor(range.from)
        }

        "\n" + lexer.highlight(range, Some(10))
      }
    )

  def getResult = unionLog(nodeLog)

  def prepare() {nodeLog = Nil}
}