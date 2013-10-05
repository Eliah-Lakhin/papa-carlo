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

import name.lakhin.eliah.projects.papacarlo.{Syntax, Lexer}
import org.scalatest.FunSpec
import net.liftweb.json.JsonAST._

abstract class ParserSpec(parserName: String,
                          lexerConstructor: () => Lexer,
                          syntaxConstructor: Lexer => Syntax)
  extends FunSpec {

  private val environments:
    Map[String, (String, () => Environment)] = Map(
      "token" ->
        (
          "tokenize",
          () => new TokenizerEnvironment(lexerConstructor)
        ),
      "fragment" ->
        (
          "produce fragments",
          () => new FragmentationEnvironment(lexerConstructor)
        ),
      "cache" ->
        (
          "track cache",
          () => new CacheEnvironment(lexerConstructor, syntaxConstructor)
        ),
      "node" ->
        (
          "produce syntax nodes",
          () => new NodeEnvironment(lexerConstructor, syntaxConstructor)
        ),
      "error" ->
        (
          "produce syntax errors",
          () => new ErrorEnvironment(lexerConstructor, syntaxConstructor)
        )
    )

  private val tests =
    Resources.json[Map[String, Map[String, JValue]]](parserName, "config.json")
      .map({
        case (testName, settings) =>
          Test(
            parserName = parserName,

            testName = testName,

            steps = settings
              .get("steps")
              .flatMap(_ match {
                case JInt(value) => Some(value.toInt)
                case _ => None
              })
              .getOrElse((0 until 100)
                .find(step => !Resources.exist(
                  parserName + "/" + testName + "/input",
                  "step" + step + ".txt"
                ))
                .getOrElse(100)),

            allowedEnvironments = environments
              .keys
              .filter(name => !settings
                .get(name)
                .exists(_ == JBool(value = false)))
              .toSet,

            shortOutput = settings
              .get("shortOutput")
              .exists(_ == JBool(value = true)),

            outputFrom = settings
              .get("outputFrom")
              .flatMap(_ match {
                case JInt(value) => Some(value.toInt)
                case _ => None
              })
              .getOrElse(0)
          )
      })

  for (test <- tests) {
    describe(test.testName + " test") {

      for ((environmentName, (description, environmentConstructor))
           <- environments)
        if (test.allowedEnvironments.contains(environmentName))
          it("should " + description) {
            val environment = environmentConstructor()
            environment.shortOutput = test.shortOutput

            var statistics = List.empty[Long]
            var results = List.empty[String]

            for (step <- 0 until test.steps) {
              environment.prepare()
              statistics ::= environment.input(test.inputs.getOrElse(step, ""))
              val result = environment.getResult
              if (step >= test.outputFrom)
                test.write(environmentName, step, result)
              results ::= result
            }

            Resources.update(
              parserName + "/" + test.testName + "/statistics",
              environmentName + ".txt",
              statistics.reverse.zipWithIndex.map {
                case (time, step) => "Step " + step + ": " + time + "ms"
              }.mkString("\n")
            )

            for ((result, step) <- results.reverse.zipWithIndex)
              if (step >= test.outputFrom)
                assert(
                  result == test.prototypes.get(environmentName)
                    .flatMap(_.get(step)).getOrElse(""),
                  "Step " + step + " result did not equal to the prototype"
                )
          }
    }
  }
}