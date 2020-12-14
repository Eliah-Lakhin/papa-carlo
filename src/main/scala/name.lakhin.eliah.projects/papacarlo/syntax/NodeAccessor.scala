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

import name.lakhin.eliah.projects.papacarlo.lexis.TokenReference

final class NodeAccessor(val node: Node) {
  def setKind(kind: String) = {
    if (!node.bound) {
      node.kind = kind
      change()
    }

    this
  }

  def setBegin(reference: TokenReference) = {
    if (!node.bound) {
      node.begin = reference
      change()
    }

    this
  }

  def setEnd(reference: TokenReference) = {
    if (!node.bound) {
      node.end = reference
      change()
    }

    this
  }

  def setBranches(branches: Map[String, List[Node]]) = {
    if (!node.bound) {
      node.branches = branches
      change()
    }

    this
  }

  def setBranches(tag: String, branches: List[Node]) = {
    if (!node.bound) {
      node.branches += tag -> branches
      change()
    }

    this
  }

  def setReferences(references: Map[String, List[TokenReference]]) = {
    if (!node.bound) {
      node.references = references
      change()
    }

    this
  }

  def setReferences(tag: String, references: List[TokenReference]) = {
    if (!node.bound) {
      node.references += tag -> references
      change()
    }

    this
  }

  def setConstant(tag: String, value: String) = {
    if (!node.bound) {
      node.constants += tag -> value
      change()
    }

    this
  }

  private def change(): Unit = {
    node.cachable = false
    node.producer = None
  }
}
