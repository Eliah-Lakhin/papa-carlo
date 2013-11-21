import sbt._
import Keys._
import com.typesafe.sbt._

object ProjectBuildConfig extends Build {
  lazy val PapaCarlo = Project(
    id = "papa-carlo",
    base = file("."),
    settings = Defaults.defaultSettings ++ SbtPgp.settings ++
      Seq(
        name := "Papa Carlo",
        version := "0.4.0-SNAPSHOT",

        description :=
          "Constructor of incremental parsers in Scala using PEG grammars",
        homepage := Some(new URL("http://lakhin.com/projects/papa-carlo/")),

        organization := "name.lakhin.eliah.projects.papacarlo",
        organizationHomepage  := Some(new URL("http://lakhin.com/")),

        licenses := Seq("The Apache Software License, Version 2.0" ->
          new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
        startYear := Some(2013),

        scalaVersion := "2.10.0",

        libraryDependencies ++= Seq(
          "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP8",
          "net.liftweb" % "lift-json_2.10" % "2.5-RC6"
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
              Some("releases"  at nexus + "service/local/staging/deploy/maven2")
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
}