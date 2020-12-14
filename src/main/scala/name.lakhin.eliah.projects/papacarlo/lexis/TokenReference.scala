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

import name.lakhin.eliah.projects.papacarlo.utils.Signal

final class TokenReference(val collection: TokenCollection,
                           var index: Int) {
  private[lexis] var fragment = Option.empty[Fragment]
  private var removed: Boolean = false

  val onUpdate = new Signal[TokenReference]
  val onRemove = new Signal[TokenReference]

  def exists = !removed

  def token = {
    if (removed) throw new RuntimeException("Token was removed")
    collection.descriptions(index)
  }

  def getFragment = fragment

  private[lexis] def remove(): Unit = {
    removed = true
    onRemove.trigger(this)
  }

  override def toString =
    if (removed) "removed"
    else {
      val cursor = collection.cursor(index)

      cursor._1 + ":" + cursor._2
    }
}
