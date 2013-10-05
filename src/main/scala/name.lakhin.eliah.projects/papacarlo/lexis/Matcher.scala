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

sealed abstract class Matcher {
  def apply(code: String, position: Int): Option[Int]

  def &(another: Matcher) = SequentialMatcher(this, another)

  def |(another: Matcher) = ChoiceMatcher(this, another)

  def unary_!() = PredicativeMatcher(this, positive = false)
}

final case class StringMatcher(pattern: String)
  extends Matcher {

  def apply(code: String, position: Int) =
    Some(position + pattern.length)
      .filter(next => next <= code.length
        && code.substring(position, next) == pattern)
}

final case class CharSetMatcher(set: Set[Char],
                                positive: Boolean = true)
  extends Matcher {

  def apply(code: String, position: Int) =
    Some(position + 1)
      .filter(next => next <= code.length)
      .filter(_ => set.contains(code.charAt(position)) == positive)

  def sup = copy(positive = !positive)
}

final case class CharRangeMatcher(from: Char,
                                  to: Char,
                                  positive: Boolean = true)
  extends Matcher {

  def apply(code: String, position: Int) =
    Some(position + 1)
      .filter(next => next <= code.length)
      .filter(_ => (from <= code.charAt(position)
        && code.charAt(position) <= to) == positive)

  def sup = copy(positive = !positive)
}

final case class RepetitionMatcher(sub: Matcher,
                                   min: Int = 0,
                                   max: Int = Int.MaxValue)
  extends Matcher {

  def apply(code: String, position: Int) = {
    var count = 0
    var finished = false
    var current = position

    while (count < max && !finished) {
      sub(code, current) match {
        case Some(next) =>
          current = next
          count += 1
        case None => finished = true
      }
    }

    Some(current).filter(_ => count >= min)
  }
}

final case class ChoiceMatcher(first: Matcher,
                               second: Matcher) extends Matcher {
  def apply(code: String, position: Int) =
    first(code, position).orElse(second(code, position))
}

final case class SequentialMatcher(first: Matcher,
                                   second: Matcher)
  extends Matcher {

  def apply(code: String, position: Int) =
    first(code, position).flatMap(next => second(code, next))
}

final case class PredicativeMatcher(sub: Matcher,
                                    positive: Boolean = true)
  extends Matcher {

  def apply(code: String, position: Int) =
    Some(position)
      .filter(_ => sub.apply(code, position).isDefined == positive)
}