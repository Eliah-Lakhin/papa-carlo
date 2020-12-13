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

ThisBuild / organization := "name.lakhin.eliah.projects"
ThisBuild / organizationName := "papacarlo"
ThisBuild / name := "Papa Carlo"
ThisBuild / version := "0.8.0-SNAPSHOT"
ThisBuild / organizationHomepage := Some(url("http://lakhin.com/"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/your-account/your-project"),
    "scm:git@github.com:Eliah-Lakhin/papa-carlo.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "Eliah-Lakhin",
    name = "Ilya Lakhin",
    email = "eliah.lakhin@gmail.com",
    url = url("http://lakhin.com/")
  )
)
ThisBuild / description := "Constructor of incremental parsers in Scala using PEG grammars"
ThisBuild / licenses := List("Apache 2.0" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / startYear := Some(2013)
ThisBuild / homepage := Some(url("http://lakhin.com/projects/papa-carlo/"))
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

ThisBuild / scalaVersion := "latest.integration"
ThisBuild / crossScalaVersions := Seq("latest.integration")

import sbt._
import sbt.Keys._

//addSbtPlugin("com.jsuereth" % "sbt-pgp" % "latest.integration")
import com.jsuereth.sbtpgp._

//addSbtPlugin("org.scala-js" % "sbt-scalajs" % "latest.integration")
enablePlugins(ScalaJSPlugin)

// "If you are using a Build.scala definition, import the following"
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._



  // used by JVM and JSDemo
  val baseSettings = Seq(
    scalacOptions += "-unchecked",
    sourceDirectory := (sourceDirectory in PapaCarlo).value,
  )



  val jsSettings =
    Defaults.coreDefaultSettings ++
    baseSettings ++
    ScalaJSPlugin.projectSettings ++
    Seq(
      libraryDependencies += "org.scala-js" %%
        "scalajs-jasmine-test-framework" % scalaJSVersion % "test",

      sourceDirectory := (sourceDirectory in PapaCarlo).value,

      excludeFilter in unmanagedSources := "test"
    )



  lazy val PapaCarlo: sbt.Project = Project(
    id = "root",
    base = file(".")
  ).aggregate(JVM, JSDemo)



  lazy val JVM = Project(
    id = "jvm",
    base = file("./jvm/")
  )

  .settings(sbt.Defaults.coreDefaultSettings: _*)

  .settings(SbtPgp.projectSettings: _*) // projectSettings globalSettings

  // java.lang.NullPointerException, also with jdk 14
  .settings(baseSettings: _*)

  .settings(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % "latest.integration",
          "net.liftweb" %% "lift-json" % "latest.integration"
        ),
        resolvers ++= Seq(
          "sonatype" at "https://oss.sonatype.org/content/repositories/releases",
          "typesafe" at "https://dl.bintray.com/typesafe/maven-releases/"
        ),

        testOptions in Test += Tests.Argument("-oD"),

        credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
        publishArtifact in Test := false,
  )



  lazy val JSDemo = Project(
    id = "js-demo",
    base = file("./js/demo/"),
  )

  // java.lang.NullPointerException, also with jdk 14
  .settings(jsSettings: _*)

  /*
  .settings(
    Seq(
      unmanagedSources in (Compile, ScalaJSKeys.packageJS) +=
        (baseDirectory in PapaCarlo).value / "js" / "demo" / "Demo.js"
    )
  )
  */
