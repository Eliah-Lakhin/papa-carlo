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
package papacarlo.test.utils

import scala.io.Source
import java.io.{FileWriter, File}
import net.liftweb.json.{NoTypeHints, Serialization}

final class Resources(
  inputBase: String = Resources.DefaultResourceBase,
  outputBase: String = Resources.DefaultResourceBase
) {

  private implicit val formats = Serialization.formats(NoTypeHints)

  def exist(category: String, name: String) =
    getClass.getResource(inputBase + fileName(category, name)) != null

  def input(category: String, name: String) : String =
    try {
      val filePath = "src/test/resources" + inputBase + fileName(category, name)
      val textSource = scala.io.Source.fromFile(filePath)
      val str = textSource.mkString
      textSource.close
      return str
    } catch {
      case _: java.io.FileNotFoundException => return ""
    }

  def update(category: String, name: String, content: String) {
    try {
      val resource = getClass.getResource(outputBase)

      if (resource.getProtocol == "file") {
        val file = new File(resource.getPath + fileName(category, name))

        if (!file.exists()) {
          val parent = file.getParentFile
          if (!parent.exists()) parent.mkdirs()
          file.createNewFile()
        }

        val writer = new FileWriter(file, false)

        writer.write(content)

        writer.close()
      }
    } catch {
      case _: RuntimeException =>
    }
  }

  def json[A](category: String, name: String)
             (implicit mf : scala.reflect.Manifest[A]) =
    Serialization.read[A](input(category, name))

  private def fileName(category: String, name: String) = category + "/" + name
}

object Resources {
  val DefaultResourceBase = "/fixtures/"
}
