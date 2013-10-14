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

final case class Test(parserName: String,
                      testName: String,
                      steps: Int,
                      monitors: Set[String],
                      shortOutput: Boolean,
                      outputFrom: Int,
                      independentSteps: Boolean) {
  val inputs = read("input")

  val prototypes = monitors.map(name => name -> read("prototype", name)).toMap

  private def read(dir: String, tag: String = "") = (
    for (step <- 0 until steps)
      yield (
        step,
        if (tag.nonEmpty)
          Resources.input(
            parserName + "/" + testName + "/" + dir + "/step" + step,
            tag + ".txt"
          )
        else
          Resources.input(
            parserName + "/" + testName + "/" + dir,
            "step" + step + ".txt"
          )
      )
  ).toMap

  def write(kind: String, step: Int, value: String) {
    Resources.update(
      parserName + "/" + testName + "/output/step" + step,
      kind + ".txt",
      value
    )
  }
}