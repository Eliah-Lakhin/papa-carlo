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

object Difference {
  def head[A](first: Seq[A],
                second: Seq[A],
                comparator: Pair[A, A] => Boolean) =
    first.zip(second).takeWhile(comparator).length

  def double[A](first: Seq[A],
                second: Seq[A],
                comparator: Pair[A, A] => Boolean) = {
    val pairs = first.zip(second)
    val head = pairs.takeWhile(comparator).length
    (
      head,
      first.drop(head).reverse.zip(second.drop(head).reverse)
        .takeWhile(comparator)
        .length
    )
  }
}
