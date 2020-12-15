// https://github.com/sbt/sbt-pgp
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.1.1")

// https://www.scala-js.org/doc/project/index.html
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.3.1")

// https://github.com/ceedubs/sbt-ctags
addSbtPlugin("net.ceedubs" %% "sbt-ctags" % "0.3.0")
// TODO sbt-ctags is abandoned, replace with metals
// https://scalameta.org/metals/docs/build-tools/sbt.html



// optional plugins for auto-correction and formatting

// https://scalacenter.github.io/scalafix/docs/users/installation.html
// CHORE to activate this, also enable `ThisBuild / semanticdb` in `build.sbt`
//addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.24")

// https://github.com/lucidsoftware/neo-sbt-scalafmt
//addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.16")
