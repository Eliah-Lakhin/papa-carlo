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

// CHORE bump versions
ThisBuild / scalaVersion := "2.13.4"
ThisBuild / crossScalaVersions := Seq("2.13.4")

// needed for scalafix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalacOptions ++=
  Seq(
    "-unchecked", "-deprecation", // make compiler more verbose
    "-Ywarn-unused", // needed for scalafix
  )

ThisBuild / organization := "name.lakhin.eliah.projects"
ThisBuild / organizationHomepage := Some(url("http://lakhin.com/"))
ThisBuild / organizationName := "papacarlo"
ThisBuild / description := "Constructor of incremental parsers in Scala using PEG grammars"
ThisBuild / version := "0.8.0-SNAPSHOT"
ThisBuild / homepage := Some(url("http://lakhin.com/projects/papa-carlo/"))
ThisBuild / licenses := List("Apache 2.0" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / startYear := Some(2013)
ThisBuild / scmInfo :=
  Some(
    ScmInfo(
      url("https://github.com/Eliah-Lakhin/papa-carlo"),
      "scm:git@github.com:Eliah-Lakhin/papa-carlo.git"
    )
  )
ThisBuild / developers :=
  List(
    Developer(
      id = "Eliah-Lakhin",
      name = "Ilya Lakhin",
      email = "eliah.lakhin@gmail.com",
      url = url("http://lakhin.com/")
    )
  )

import sbt.Project
import com.jsuereth.sbtpgp.SbtPgp

// used by JVM and JSDemo
lazy val baseSettings =
  Seq(
    sourceDirectory := file("./src/")
  )

lazy val PapaCarlo =
  Project(
    id = "root",
    base = file("./")
  )
  .aggregate(JVM)
  .aggregate(JSDemo)

lazy val JVM =
  Project(
    id = "jvm",
    base = file("./jvm/")
  )
  .enablePlugins(SbtPgp)
  .settings(baseSettings: _*)
  .settings(
    // CHORE bump versions
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.3.0-SNAP3" % "test",
      "net.liftweb" %% "lift-json" % "3.4.3"
    ),
    testOptions in Test += Tests.Argument("-oD"),
    publishTo :=
      {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishArtifact in Test := false,
  )

lazy val JSDemo =
  Project(
    id = "js-demo",
    base = file("./js/demo/"),
  )
  .enablePlugins(ScalaJSPlugin)
  .settings(baseSettings: _*)
  .settings(
    // CHORE bump versions
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.7.5" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
    // TODO verify. what was this supposed to do?
    //excludeFilter in unmanagedSources := "test"
  )
  // TODO what was this supposed to do?
  /*
  .settings(
    Seq(
      unmanagedSources in (Compile, ScalaJSKeys.packageJS) +=
        (baseDirectory in PapaCarlo).value / "js" / "demo" / "Demo.js"
    )
  )
  */
