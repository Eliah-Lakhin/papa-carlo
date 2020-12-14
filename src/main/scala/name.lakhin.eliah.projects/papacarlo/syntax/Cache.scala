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
import name.lakhin.eliah.projects.papacarlo.lexis.Fragment
import name.lakhin.eliah.projects.papacarlo.utils.Bounds

final class Cache(syntax: Syntax,
                  val fragment: Fragment,
                  val node: Node) {
  private[papacarlo] var errors = List.empty[Error]
  private[syntax] var ready = true

  for (oldCache <- syntax.cache.get(fragment.id)) oldCache.remove()

  syntax.cache += fragment.id -> this

  private val fragmentRemove = (_: Fragment) => remove()
  private val nodeRemove = (_: Node) => remove()

  fragment.onRemove.bind(fragmentRemove)
  node.onRemove.bind(nodeRemove)

  syntax.onCacheCreate.trigger(this)

  private[papacarlo] def invalidate(range: Bounds): Unit = {
    val session = new Session(syntax, fragment)

    ready = false
    val result = session.parse(node.kind)
    ready = true

    errors = result._2

    for (replacement <- result._1) {
      val errorBounds = errors.map(_.range)
      for (descendant <- node.merge(syntax.nodes, replacement, range))
        if (descendant.cachable)
          for (descendantFragment <- descendant.begin.getFragment)
            if (descendantFragment.end.index == descendant.end.index
              && !errorBounds.exists(_.intersects(descendant.range)))
                new Cache(syntax, descendantFragment, descendant)

      syntax.onNodeMerge.trigger(node)
      node.update()
    }
  }

  private def remove(): Unit = {
    syntax.onCacheRemove.trigger(this)
    syntax.cache -= fragment.id
    node.onRemove.unbind(nodeRemove)
    fragment.onRemove.unbind(fragmentRemove)
  }
}