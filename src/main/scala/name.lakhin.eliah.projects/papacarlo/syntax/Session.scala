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

import name.lakhin.eliah.projects.papacarlo.Syntax
import name.lakhin.eliah.projects.papacarlo.lexis.{Token, Fragment}
import name.lakhin.eliah.projects.papacarlo.utils.Bounds
import name.lakhin.eliah.projects.papacarlo.syntax.rules.ReferentialRule

final class Session(val syntax: Syntax, fragment: Fragment) {
  private[syntax] var state = State()
  private[syntax] var packrat = Map.empty[String, Packrat]
  private[syntax] var tokens = IndexedSeq.empty[Token]

  private val sourceTokens = fragment.getTokens
  private[syntax] val sourceTokensOffset = fragment.begin.index

  private var index = IndexedSeq.empty[Int]

  private[syntax] def reference(relativeIndex: Int) =
    syntax.lexer.reference(relativeIndex + sourceTokensOffset)

  private[syntax] def relativeSegmentOf(virtualSegment: Bounds) =
    if (virtualSegment.length > 0)
      virtualSegment.map(
        relativeIndexOf,
        until => relativeIndexOf(until - 1) + 1
      )
    else
      Bounds.cursor(relativeIndexOf(virtualSegment.from))

  private[syntax] def relativeIndexOf(virtualIndex: Int) =
    if (virtualIndex < index.size)
      index.lift(virtualIndex).getOrElse(0)
    else index.lastOption.getOrElse(0) + 1

  private[syntax] def virtualIndexOf(relativeIndex: Int) = {
    val result = index.indexOf(relativeIndex)

    if (result >= 0) result
    else index.size
  }

  private[syntax] def getCache(virtualIndex: Int, nodeKind: String) =
    reference(relativeIndexOf(virtualIndex))
      .getFragment
      .flatMap(fragment => syntax.cache.get(fragment.id))
      .filter(cache => cache.errors.isEmpty && cache.node.kind == nodeKind &&
        cache.ready)
      .map(_.node)

  private[syntax] def parse(ruleName: String) = {
    state = State()
    packrat = Map.empty
    sourceTokens.zipWithIndex.filter(!_._1.isSkippable).unzip match {
      case (preparedTokens, preparedIndex) =>
        tokens = preparedTokens
        index = preparedIndex
    }

    var parsed = false
    var issues = List.empty[Issue]

    while (!parsed) {
      state = State()

      syntax.onParseStep.trigger(tokens)

      val result = ReferentialRule(ruleName, Some("result"))(this)

      if (result == Result.Failed) {
        var firstIssue = state.issues.foldLeft(Issue(
          Bounds.cursor(tokens.length),
          "code mismatched"
        )) {
          (current, issue) =>
            if (issue.range.from <= current.range.from) issue
            else current
        }

        if (firstIssue.range.length == 0)
          firstIssue = firstIssue.copy(range = firstIssue.range.enlarge(0, 1))

        if (firstIssue.range.from >= tokens.length)
          firstIssue = firstIssue.copy(range = firstIssue.range.shift(-1))

        issues ::= exclude(firstIssue.range, firstIssue.description)
      } else {
        if (state.virtualPosition < tokens.length)
          issues ::= exclude(
            Bounds.point(state.virtualPosition),
            "code mismatched"
          )
        else {
          issues :::= state.issues
            .map(issue => issue.copy(range = relativeSegmentOf(issue.range)))
          parsed = true
        }
      }

      if (tokens.isEmpty) parsed = true
    }

    (
      state.products.headOption.filter(_._1 == "result").map(_._2),
      issues
        .groupBy(_.description)
        .map({
          case (exception, list) =>
            var result = List.empty[Issue]

            for (issue <- list.sortBy(_.range.from))
              result.headOption match {
                case Some(current) =>
                  if ((current.range.until < issue.range.from) &&
                    (virtualIndexOf(current.range.until) <
                      virtualIndexOf(issue.range.from))) result ::= issue
                  else result = current
                    .copy(range = current.range.union(issue.range)) ::
                      result.tail

                case None => result ::= issue
              }

            result.map(issueToError)
        })
        .flatten
        .toList
    )
  }

  private def issueToError(issue: Issue) =
    if (issue.range.length > 0)
      Error(
        reference(issue.range.from),
        reference(issue.range.until - 1),
        issue.description
      )
    else {
      val point = reference(issue.range.from)
      Error(point, point, issue.description, cursor = true)
    }

  private def exclude(virtualSegment: Bounds, reason: String) = {
    val relativeSegment = relativeSegmentOf(virtualSegment)
    packrat = packrat
      .filter(!_._2.range.touches(virtualSegment))
      .mapValues {
        packrat =>
          if (packrat.range.from >= virtualSegment.from)
            packrat.copy(range = packrat.range.shift(-virtualSegment.length))
          else
            packrat
      }

    tokens = virtualSegment.replace(tokens, Vector.empty)
    index = virtualSegment.replace(index, Vector.empty)

    Issue(relativeSegment, reason)
  }
}