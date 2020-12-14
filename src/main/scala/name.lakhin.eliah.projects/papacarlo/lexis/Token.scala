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

final class Token(val kind: String,
                  val value: String,
                  private var skipped: Boolean = false,
                  private var mutable: Boolean = false,
                  private var indentation: Boolean = false) {
  private val originallySkipped = skipped
  private val originallyMutable = mutable

  private[lexis] var context = Context.Base
  private[lexis] var seam: SeamType = RegularSeam

  def isSkippable = skipped
  def isMutable = mutable
  def getContext = context

  private[lexis] def applySkipLevel(level: SkipLevel): Unit = {
    level match {
      case ForceSkip => skipped = true
      case ForceUse => skipped = false
      case OriginalSkipping => skipped = originallySkipped
    }
  }

  private[lexis] def revertMutability(): Unit = {
    mutable = originallyMutable
  }

  private[lexis] def sameAs(another: Token) = {
    value == another.value || kind == another.kind &&
      (mutable || another.mutable)
  }
}

object Token {
  val LineBreakKind = "lineBreak"
  val UnknownKind = "unknown"

  def unknown(value: String) =
    new Token(
      kind = UnknownKind,
      value = value,
      skipped = false,
      mutable = false
    )

  def terminal(value: String) =
    new Token(
      kind = value,
      value = value,
      skipped = false,
      mutable = false
    )

  def lineBreak =
    new Token(
      kind = LineBreakKind,
      value = "\n",
      skipped = true,
      mutable = false
    )
}