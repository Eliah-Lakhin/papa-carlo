addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")

addSbtPlugin("org.scala-lang.modules.scalajs" %% "scalajs-sbt-plugin" % "0.4-SNAPSHOT")

addSbtPlugin("com.kalmanb.sbt" % "sbt-ctags" % "0.3.0")

resolvers += Resolver.url("scala-js-snapshots", url("http://repo.scala-js.org/repo/snapshots/"))(Resolver.ivyStylePatterns)
