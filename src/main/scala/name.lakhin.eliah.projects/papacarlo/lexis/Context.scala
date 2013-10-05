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

final case class Context(kind: Int,
                         parent: Option[Context] = None) {
  val view: String = kind + parent.map(":" + _.view).getOrElse("")

  val depth: Int = parent.map(_.depth + 1).getOrElse(0)

  def branch(childKind: Int) = Context(childKind, Some(this))

  def intersect(another: Context) = {
    var first = this
    var second = another

    while (first.depth > second.depth) {
      first = first.parent.getOrElse(second)
    }

    while (second.depth > first.depth) {
      second = second.parent.getOrElse(first)
    }

    while (first.view != second.view) {
      first = first.parent.getOrElse(Context.Base)
      second = second.parent.getOrElse(Context.Base)
    }

    first
  }

  override def equals(another: Any) =
    another.isInstanceOf[Context] && view == another.asInstanceOf[Context].view

  override def toString = view
}

object Context {
  val Base: Context = Context(0)
}
