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
package papacarlo.examples

import name.lakhin.eliah.projects.papacarlo.{Syntax, Lexer}
import name.lakhin.eliah.projects.papacarlo.syntax.Rule
import name.lakhin.eliah.projects.papacarlo.syntax.rules.NamedRule

final class JsonSyntax(lexer: Lexer) {
  val syntax = new Syntax(lexer)

  import syntax._
  import Rule._

  private val jsonObject = mainRule("object") {
    token("{") &
      repeat(objectEntry.as("entry")).by(token(",") |
        "object entries must be separated with , sign") &
      (token("}") | "object must end with } sign")
  }

  private val objectEntry = rule("entry") {
    capture(token("string"), "key") &
      token(":") &
      jsonValue
  }

  private val jsonArray = rule("array") {
    token("[") &
      repeat(jsonValue).by(token(",") |
        "array entries must be separated with , sign") &
      (token("]") | "array must end with ] sign")
  }

  private val jsonString = rule("string") {
    capture(token("string"), "value")
  }

  private val jsonNumber = rule("number") {
    capture(token("number"), "value")
  }

  private val jsonBoolean = rule("boolean") {
    capture(token("true") | token("false"), "value")
  }

  private val jsonNull = rule("null") {
    token("null")
  }

  private val jsonValue: NamedRule = composition("value") {
    jsonString.as("value") |
      jsonNumber.as("value") |
      jsonObject.as("value") |
      jsonArray.as("value") |
      jsonBoolean.as("value") |
      jsonNull.as("value")
  }

  cachable(jsonObject, jsonArray)
}