addSbtPlugin("com.jsuereth" % "sbt-pgp" % "latest.integration")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "latest.integration")

addSbtPlugin("net.ceedubs" %% "sbt-ctags" % "latest.integration")

resolvers += Resolver.url("scala-js-snapshots", url("https://repo.scala-js.org/repo/releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")
