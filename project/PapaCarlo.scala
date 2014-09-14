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
import sbt._
import Keys._
import com.typesafe.sbt._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import ScalaJSKeys._

object PapaCarlo extends Build {
  val papaCarloVersion = "0.8.0-SNAPSHOT"
  
  val baseSettings = Seq(
    name := "Papa Carlo",
    version := papaCarloVersion,

    scalacOptions += "-unchecked",

    sourceDirectory := (sourceDirectory in PapaCarlo).value,

    description :=
      "Constructor of incremental parsers in Scala using PEG grammars",
    homepage := Some(new URL("http://lakhin.com/projects/papa-carlo/")),

    organization := "name.lakhin.eliah.projects.papacarlo",
    organizationHomepage  := Some(new URL("http://lakhin.com/")),

    licenses := Seq("The Apache Software License, Version 2.0" ->
      new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    startYear := Some(2013),

    scalaVersion := "2.11.1",

    crossScalaVersions := Seq("2.10.4", "2.11.1")
  )

  val jsSettings = Defaults.defaultSettings ++ baseSettings ++
    scalaJSSettings ++ Seq(
      libraryDependencies += "org.scala-lang.modules.scalajs" %%
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
    base = file("./jvm/"),
    settings = Defaults.defaultSettings ++ SbtPgp.settings ++ baseSettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % "2.2.0",
          "net.liftweb" %% "lift-json" % "2.6-M4"
        ),
        resolvers ++= Seq(
          "sonatype" at "http://oss.sonatype.org/content/repositories/releases",
          "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
        ),

        testOptions in Test += Tests.Argument("-oD"),

        publishMavenStyle := true,
        pomIncludeRepository := { _ => false },
        SbtPgp.useGpg := true,
        credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
        publishArtifact in Test := false,
        publishTo <<= version {
          version =>
            val nexus = "https://oss.sonatype.org/"
            if (version.endsWith("SNAPSHOT"))
              Some("snapshots" at nexus + "content/repositories/snapshots")
            else
              Some("releases" at nexus + "service/local/staging/deploy/maven2")
        },
        pomExtra :=
          <scm>
            <url>git@github.com:Eliah-Lakhin/papa-carlo.git</url>
            <connection>scm:git:git@github.com:Eliah-Lakhin/papa-carlo.git</connection>
          </scm>
          <developers>
            <developer>
              <id>Eliah-Lakhin</id>
              <name>Ilya Lakhin</name>
              <email>eliah.lakhin@gmail.com</email>
              <url>http://lakhin.com/</url>
            </developer>
          </developers>
      )
  )

  lazy val JSDemo = Project(
    id = "js-demo",
    base = file("./js/demo/"),
    settings = jsSettings/* ++ Seq(
      unmanagedSources in (Compile, ScalaJSKeys.packageJS) +=
        (baseDirectory in PapaCarlo).value / "js" / "demo" / "Demo.js"
    )                      */
  )
}
