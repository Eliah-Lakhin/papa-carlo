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
import name.lakhin.eliah.projects.papacarlo.utils.{Registry, Bounds, Signal}

final class Node(private[syntax] var kind: String,
                 private[syntax] var begin: TokenReference,
                 private[syntax] var end: TokenReference) {
  private[papacarlo] var id = Node.Unbound
  private[syntax] var branches = Map.empty[String, List[Node]]
  private[syntax] var references = Map.empty[String, List[TokenReference]]
  private[syntax] var cachable = false

  val onChange = new Signal[Node]
  val onRemove = new Signal[Node]

  private val reflection =
    (reference: TokenReference) => { onChange.trigger(this) }

  def bound = id != Node.Unbound

  def getId = id

  def getKind = kind

  def getBegin = begin

  def getEnd = end

  def getRange = Bounds(begin.index, end.index + 1)

  def getBranches(tag: String) = branches.getOrElse(tag, Nil)

  def getBranch(tag: String) = getBranches(tag).headOption

  def hasBranch(tag: String) = branches.contains(tag)

  def getValue(tag: String) =
    references.lift(tag).map(_.map(_.token.value).mkString).getOrElse("")

  def hasValue(tag: String) = references.contains(tag)

  def range = Bounds(begin.index, end.index + 1)

  def getCachable = cachable

  private[syntax] def remove(registry: Registry[Node]) {
    if (bound) {
      onRemove.trigger(this)
      releaseReflection()
      registry.remove(id)
      id = Node.Unbound
    }
  }

  private[syntax] def merge(registry: Registry[Node], replacement: Node) = {
    kind = replacement.kind
    releaseReflection()
    begin = replacement.begin
    end = replacement.end
    references = replacement.references
    initializeReflection()

    var unregistered = List.empty[Node]
    var registered = Set.empty[Int]
    replacement.visitBranches(newDescendant => {
      if (newDescendant.bound) registered += newDescendant.id
      else unregistered ::= newDescendant
    })

    reverseVisitBranches(oldDescendant => {
      if (!registered.contains(oldDescendant.id))
        oldDescendant.remove(registry)
    })

    branches = replacement.branches

    for (descendant <- unregistered)
      registry.add {
        id =>
          descendant.id = id
          descendant
      }

    unregistered
  }

  private def visitBranches(enter: Node => Any) {
    for (branch <- branches.map(_._2).flatten) {
      enter(branch)
      branch.visitBranches(enter)
    }
  }

  private def reverseVisitBranches(leave: Node => Any) {
    for (branch <- branches.map(_._2).flatten) {
      branch.reverseVisitBranches(leave)
      leave(branch)
    }
  }

  private def initializeReflection() {
    for (reference <- references.map(_._2.filter(_.token.isMutable)).flatten)
      reference.onUpdate.bind(reflection)
  }

  private def releaseReflection() {
    for (reference <- references.map(_._2.filter(_.token.isMutable)).flatten)
      reference.onUpdate.unbind(reflection)
  }

  override def toString = kind + ":" + id + (if (cachable) " cachable" else "")

  def prettyPrint(prefix: String = ""): String = {
    val result = new StringBuilder

    result ++= prefix + kind + " " + id

    if (cachable) result ++= "\n" + prefix + "cachable"

    result ++= " {"

    for (reference <- references)
      result ++= "\n" + prefix + "  " + reference._1 + ": " +
        getValue(reference._1)

    for ((name, subnodes) <- branches; branch <- subnodes) {
      result ++= "\n" + prefix + "  " + name + ":\n"
      result ++= branch.prettyPrint(prefix + "    ")
    }

    result ++= "\n" + prefix + "}"

    result.toString()
  }
}

object Node {
  val Unbound = -1
}