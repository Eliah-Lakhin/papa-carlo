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
import name.lakhin.eliah.projects.papacarlo.utils.{Difference, Registry, Bounds,
  Signal}

final class Node(private[syntax] var kind: String,
                 private[syntax] var begin: TokenReference,
                 private[syntax] var end: TokenReference) {
  private[papacarlo] var id = Node.Unbound
  private[syntax] var branches = Map.empty[String, List[Node]]
  private[syntax] var references = Map.empty[String, List[TokenReference]]
  private[syntax] var constants = Map.empty[String, String]
  private[syntax] var cachable = false
  private[syntax] var parent = Option.empty[Node]

  val onChange = new Signal[Node]
  val onRemove = new Signal[Node]

  private val reflection = (reference: TokenReference) => update()

  def bound = id != Node.Unbound

  def getId = id

  def getKind = kind

  def getBegin = begin

  def getEnd = end

  def getRange = Bounds(begin.index, end.index + 1)

  def getBranches(tag: String) = branches.getOrElse(tag, Nil)

  def getBranch(tag: String) = getBranches(tag).headOption

  def hasBranch(tag: String) = branches.contains(tag)

  def getParent = parent

  def getValue(tag: String) =
    constants.get(tag).getOrElse(references.lift(tag)
      .map(_.map(_.token.value).mkString).getOrElse(""))

  def hasValue(tag: String) =
    constants.contains(tag) || references.contains(tag)

  def range = Bounds(begin.index, end.index + 1)

  def getCachable = cachable

  def update(ancestor: Boolean = false) {
    if (onChange.nonEmpty && !ancestor) onChange.trigger(this)
    else for (parent <- parent) parent.update(ancestor)
  }

  private[syntax] def remove(registry: Registry[Node]) {
    if (bound) {
      onRemove.trigger(this)
      releaseReflection()
      registry.remove(id)
      id = Node.Unbound
    }
  }

  private[syntax] def merge(registry: Registry[Node], replacement: Node) = {
    for ((tag, oldBranches) <- this.branches;
         newBranches <- replacement.branches.get(tag)) {
      val difference = Difference.double[Node](
        oldBranches,
        newBranches,
        (pair: Pair[Node, Node]) => pair._1.bound &&
          pair._1.sourceCode == pair._2.sourceCode
      )

      if (difference != (0, 0))
        replacement.branches += tag ->
          Bounds(difference._1, oldBranches.size - difference._2).replace(
            oldBranches,
            Bounds(difference._1, newBranches.size - difference._2)
              .slice(newBranches)
          )
    }

    kind = replacement.kind
    releaseReflection()
    begin = replacement.begin
    end = replacement.end
    references = replacement.references
    initializeReflection()

    var unregistered = List.empty[Node]
    var registered = Set.empty[Int]
    replacement.visitBranches(this, (parent, newDescendant) => {
      if (newDescendant.bound) registered += newDescendant.id
      else unregistered ::= newDescendant

      newDescendant.parent = Some(parent)
    })

    reverseVisitBranches(oldDescendant => {
      if (!registered.contains(oldDescendant.id))
        oldDescendant.remove(registry)
    })

    branches = replacement.branches

    for (descendant <- unregistered.reverseIterator)
      registry.add {
        id =>
          descendant.id = id
          descendant
      }

    unregistered
  }

  private def visitBranches(current: Node, enter: (Node, Node) => Any) {
    for (branch <- branches.map(_._2).flatten) {
      enter(current, branch)
      branch.visitBranches(branch, enter)
    }
  }

  private def reverseVisitBranches(leave: Node => Any) {
    for (branch <- branches.map(_._2).flatten) {
      branch.reverseVisitBranches(leave)
      leave(branch)
    }
  }

  private def subscribableReferences =
    references
      .filter(pair => !constants.contains(pair._1))
      .map(_._2.filter(reference => !reference.exists ||
        reference.token.isMutable))
      .flatten

  private def initializeReflection() {
    for (reference <- subscribableReferences)
      reference.onUpdate.bind(reflection)
  }

  private def releaseReflection() {
    for (reference <- subscribableReferences)
      reference.onUpdate.unbind(reflection)
  }

  override def toString = kind + ":" + id + (if (cachable) " cachable" else "")

  def sourceCode =
    if (begin.exists && end.exists)
      begin.collection.descriptions.slice(begin.index, end.index + 1)
        .map(_.value).mkString
    else
      ""

  def prettyPrint(prefix: String = ""): String = {
    val result = new StringBuilder

    result ++= kind + " " + id

    if (cachable) result ++= " cachable"

    result ++= parent.map(" >> " + _.id).getOrElse("")

    if (references.nonEmpty || branches.nonEmpty) {
      result ++= " {"

      for (reference <- references)
        result ++= "\n" + prefix + "  " + reference._1 + ": " +
          getValue(reference._1)

      for ((name, subnodes) <- branches; branch <- subnodes) {
        result ++= "\n" + prefix + "  " + name + ": "
        result ++= branch.prettyPrint(prefix + "  ")
      }

      result ++= "\n" + prefix + "}"
    }

    result.toString()
  }

  def accessor = new NodeAccessor(this)
}

object Node {
  val Unbound = -1

  def apply(kind: String,
            begin: TokenReference,
            end: TokenReference,
            branches: List[Pair[String, Node]] = Nil,
            references: List[Pair[String, TokenReference]] = Nil,
            constants: Map[String, String] = Map.empty) = {
    val result = new Node(kind, begin, end)

    result.branches = branches.groupBy(_._1).mapValues(_.map(_._2)).view.force
    result.references = references.groupBy(_._1).mapValues(_.map(_._2))
      .view.force
    result.constants = constants

    result
  }
}