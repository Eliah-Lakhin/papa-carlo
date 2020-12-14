# compile

to compile `papa-carlo`, you need

* JDK, the Java Development Kit. `sbt` recommends [AdoptOpenJDK JDK 11](https://adoptopenjdk.net/) (version 15 should work too)
* [sbt](https://www.scala-sbt.org/release/docs/Setup.html), the Simple Build Tool for scala and java apps

the folder of the `sbt` executable should now be in your PATH environment variable

navigate to the `papa-carlo` project folder, open a terminal, and run

```
sbt
```

`sbt` should download dependencies, and show the sbt console

```
[info] welcome to sbt
....
[info] started sbt server
sbt:root> 
```

enter `; reload; compile` to compile

to compile again, press `arrow up` and hit enter
