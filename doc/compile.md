# compile

to compile `papa-carlo`, you need

* JDK, the Java Development Kit.  
`sbt` recommends [AdoptOpenJDK JDK 11](https://adoptopenjdk.net/?variant=openjdk11) or [AdoptOpenJDK JDK 8](https://adoptopenjdk.net/?variant=openjdk8)
* [sbt](https://www.scala-sbt.org/release/docs/Setup.html), the Scala Build Tool, version 1.x

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

enter `; reload; jvm/test` to compile the papa-carlo library for java.  
to compile again, press `arrow up` and hit enter

enter `jvm/packageBin` to build a JAR package to [jvm/target/](../jvm/target/)

enter `js-demo/fullLinkJS` to build the JavaScript demo
with [scala-js](https://www.scala-js.org/doc/tutorial/basic/).  
This will generate `js/demo/target/scala-2.13/js-demo-opt/main.js`,
which is included by `js/demo/client.js` and `js/demo/server.js`,
which are included by `js/demo/index.html`.  
To use the demo, [start an HTTP server](https://gist.github.com/willurd/5720255)
in the folder [js/demo/](../js/demo).  
for example on Linux:

```
cd js/demo/
python -m http.server 8000 &
xdg-open http://localhost:8000/index.html
```

enter `js-demo/fastLinkJS` to build the JavaScript demo in development mode.
in `js/demo/client.js` and `js/demo/server.js`,
replace `js-demo-opt/main.js` with `js-demo-fastopt/main.js`
