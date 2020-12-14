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

import name.lakhin.eliah.projects.papacarlo.utils.{Bounds, Signal}

final case class Fragment(id: Int,
                          begin: TokenReference,
                          end: TokenReference) {
  private var defined = true
  private val tokenRemoveReaction = (reference: TokenReference) => remove()

  val onInvalidate = new Signal[Fragment]
  val onRemove = new Signal[Fragment]

  def parent: Option[Fragment] =
    if (begin.index > 0) {
      val references = begin.collection.references.lift

      for (index <- (0 until begin.index).reverse;
           candidate <- references(index).flatMap(_.getFragment))
        if (candidate.end.index >= end.index) return Some(candidate)

      None
    }
    else None

  def range =
    if (defined) Bounds(begin.index, end.index + 1)
    else Bounds.undefined

  private[lexis] def remove(): Unit = {
    if (defined) {
      onRemove.trigger(this)
      defined = false
      begin.fragment = None
      onInvalidate.unbindAll()
      onRemove.unbindAll()
      begin.onRemove.unbind(tokenRemoveReaction)
      end.onRemove.unbind(tokenRemoveReaction)
    }
  }

  begin.onRemove.bind(tokenRemoveReaction)
  end.onRemove.bind(tokenRemoveReaction)

  def getTokens =
    Bounds(begin.index, end.index + 1).slice(begin.collection.descriptions)

  def highlight(limit: Option[Int] = None): String =
    if (defined)
      begin.collection.highlight(Bounds(begin.index, end.index + 1), limit)
    else "<removed>"

  override def toString =
    id + " " +
      this.begin.collection.range(Bounds(this.begin.index, this.end.index))
}
