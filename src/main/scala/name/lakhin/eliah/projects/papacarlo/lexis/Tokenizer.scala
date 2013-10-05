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
package papacarlo.lexis

final class Tokenizer {
  private var rules = Map.empty[String, Matcher]
  private var order = List.empty[String]
  private var terminals = List.empty[(String, StringMatcher)]
  private var keywords = Set.empty[String]
  private var skips = Set.empty[String]
  private var mutables = Set.empty[String]

  final case class RuleDefinition(name: String) {
    def skip = {
      skips += name

      this
    }

    def mutable = {
      mutables += name

      this
    }
  }

  def tokenCategory(name: String, matcher: Matcher) = {
    if (!rules.contains(name)) {
      rules += Pair(name, matcher)
      order :+= name
    }

    RuleDefinition(name)
  }

  def terminals(patterns: String*) {
    for (pattern <- patterns)
      if (!terminals.exists(terminal => terminal._1 == pattern))
        terminals = (Pair(pattern, StringMatcher(pattern)) :: terminals)
          .sortBy(_._1.length)
  }

  def keywords(patterns: String*) {
    keywords ++= patterns
  }

  def tokenize(input: String) = {
    val rules = (terminals ::: this.rules.toList).toIterable

    var output = List.empty[Token]
    var position = 0
    while (position < input.length) {
      val start = position

      var application = Option.empty[(String, Int)]
      while (position < input.length && application.isEmpty) {
        application = rules
          .map(rule => (rule._1, rule._2(input, position)))
          .find(candidate => candidate._2.isDefined)
          .map(result => result.copy(_2 = result._2.getOrElse(position + 1)))
        if (application.isEmpty) position += 1
      }

      if (start < position)
        output ::= Token.unknown(input.substring(start, position))

      for (successful <- application) {
        val value = input.substring(position, successful._2)

        if (keywords.contains(value)) output ::= Token.terminal(value)
        else {
          val kind = successful._1

          output ::= new Token(
            kind = kind,
            value = value,
            skipped = skips.contains(kind),
            mutable = mutables.contains(kind)
          )
        }

        position = successful._2
      }
    }

    output.reverse
  }
}

object Tokenizer {
  def zeroOrMore(sub: Matcher) = RepetitionMatcher(sub, 0)

  def oneOrMore(sub: Matcher) = RepetitionMatcher(sub, 1)

  def optional(sub: Matcher) = RepetitionMatcher(sub, 0, 1)

  def repeat(sub: Matcher, counter: Int) =
    RepetitionMatcher(sub, counter, counter)

  def anyOf(pattern: String) = CharSetMatcher(pattern.toSet)

  def anyExceptOf(pattern: String) = CharSetMatcher(pattern.toSet).sup

  def any() = CharSetMatcher(Set.empty).sup

  def rangeOf(from: Char, to: Char) = CharRangeMatcher(from, to)

  def chunk(pattern: String) = StringMatcher(pattern)
}