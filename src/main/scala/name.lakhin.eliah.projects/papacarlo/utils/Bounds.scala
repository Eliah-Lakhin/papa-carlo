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
package papacarlo.utils

final case class Bounds(from: Int, until: Int) {
  val defined = from <= until
  val length = until - from

  def iterator = (if (defined) from until until else 0 until 0).iterator

  def pair = from -> until

  def map(from: Int => Int, until: Int => Int) =
    if (defined) Bounds(from(this.from), until(this.until))
    else this

  def map(offset: Int => Int) =
    if (defined) Bounds(offset(this.from), offset(this.until))
    else this

  def union(point: Int) =
    if (defined) Bounds(point min from, (point + 1) max until)
    else Bounds(point, point + 1)

  def union(another: Bounds) =
    if (defined && another.defined)
      Bounds(from min another.from, until max another.until)
    else if (defined) this
    else another

  def inject(injection: Bounds) =
    if (defined && injection.defined) {
      if (injection.from <= from)
        Bounds(injection.from, until + injection.length)
      else if (injection.from <= until)
        Bounds(from, until + injection.length)
      else
        Bounds(from, injection.until)
    } else if (defined) this
    else injection

  def takeout(another: Bounds) =
    if (defined && another.defined) {
      if (until <= another.from) this
      else if (another.until <= from)
        Bounds(from - another.length, until - another.length)
      else if (from <= another.from)
        Bounds(from, (until - another.length) max another.from)
      else
        Bounds(another.until, until - another.from)
    } else this

  def enlarge(leftRadius: Int, rightRadius: Int) =
    if (defined) Bounds(from - leftRadius, until + rightRadius)
    else this

  def shift(offset: Int) =
    if (defined) Bounds(from + offset, until + offset)
    else this

  def slice[A](seq: IndexedSeq[A]) =
    if (defined) seq.slice(from, until)
    else seq

  def slice[A](seq: List[A]) =
    if (defined) seq.slice(from, until)
    else seq

  def substring(string: String) =
    if (defined) string.substring(from, until)
    else string

  def replace[A](target: IndexedSeq[A], replacement: Seq[A]) =
    if (defined) target.take(from) ++ replacement ++ target.drop(until)
    else target

  def replace[A](target: List[A], replacement: List[A]) =
    if (defined) target.take(from) ::: replacement ::: target.drop(until)
    else target

  def replace[A](target: String, replacement: String) =
    if (defined)
      target.substring(0, from) + replacement + target.substring(until)
    else target

  def includes(value: Int) = defined && from <= value && value < until

  def intersects(another: Bounds) =
    defined && another.defined && from < another.until && another.from < until

  def touches(another: Bounds) =
    defined && another.defined && from <= another.until && another.from <= until
}

object Bounds {
  val undefined = Bounds(0, -1)

  def point(position: Int) = Bounds(position, position + 1)

  def cursor(position: Int) = Bounds(position, position)
}
