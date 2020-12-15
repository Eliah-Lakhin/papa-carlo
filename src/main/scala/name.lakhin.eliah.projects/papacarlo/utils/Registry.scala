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

final class Registry[A] {
  private var records = Map.empty[Int, A]
  private var index = 0

  val onAdd = new Signal[A]
  val onRemove = new Signal[A]

  def elements = records.values

  def size = records.size

  def add(element: A) = {
    val name = generateName
    records += name -> element
    onAdd.trigger(element)
    name
  }

  def add(element: Int => A) = {
    val name = generateName
    val applied = element(name)
    records += name -> applied
    onAdd.trigger(applied)
    applied
  }

  def get(id: Int) = records.get(id)

  def remove(id: Int) = {
    val value = records.get(id)

    for (value <- value) {
      records -= id
      onRemove.trigger(value)
    }

    value
  }

  private def generateName = { index += 1; index }
}
