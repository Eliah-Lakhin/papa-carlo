# chores

the boring but necessary work to keep the project running

## keep dependencies up to date

in sbt console, run

```
; dependencyUpdates; reload plugins; dependencyUpdates; reload return
```

and (try to) use the latest versions of dependencies
in [build.sbt](../build.sbt) and [project/plugins.sbt](../project/plugins.sbt)

see [add-sbt-plugin-sbt-updates.sh](add-sbt-plugin-sbt-updates.sh)
to install the `sbt-updates` plugin, providing the `dependencyUpdates` command

reference: [getting updates for sbt plugins](https://github.com/rtimush/sbt-updates/issues/10)

## avoid version conflicts

in sbt console, run `evicted` to check for version conflicts

## optional chores

these chores are optional:
they are not needed to compile the project,
and need significant extra downloads

the corresponding plugins must be activated (uncommented)
in [project/plugins.sbt](../project/plugins.sbt)

### replace deprecated code

in [build.sbt](../build.sbt), use

```
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation")
```

to make sbt more verbose

### prettify code

run `scalafmt` in the sbt console
