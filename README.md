What is "incremental parser"?
-----------------------------

Papa Carlo is the first Scala parser library utilises incremental parsing
approach.

In contrast to all other parsing libraries that designed to parse complete
source code file at once, Papa Carlo continuously parses source code while end
users edit the file. And parser's performance is always relative to the changes
in the code. Hence parsing time does not depend on the whole code size, small
changes performing in no time even if the code consists of thousands lines.

![Incremental parser workflow illustration](https://raw.github.com/Eliah-Lakhin/papa-carlo-media/master/snapshots/manuscript.jpg)

Typical use cases of incremental parser
---------------------------------------

 * Language support plugins for code editors. For example full-featured Java IDE
   based on Sublime Text or VIM.
 * Realtime code analysis tools. Imaging smart semantic Diff!
 * [Incremental compilers](http://en.wikipedia.org/wiki/Incremental_compiler).
 * Client-server compilation [environment](http://lakhin.com/blog/15.11.2013-handy-incremental-parser/).

Papa Carlo features
-------------------

 * Syntax definition directly in the Scala code using library's API.
 * Resulting parser builds and incrementally updates Abstract Syntax Tree.
 * Error-recovery mechanism.
   The parser can build Parse Tree even if the source code contains syntax
   errors.
 * Recursive descent parsing based on
   [PEG](http://en.wikipedia.org/wiki/Parsing_expression_grammar) grammars.
 * Expressions parsing with
   [Pratt algorithm](http://en.wikipedia.org/wiki/Pratt_parser) and prepared
   primitives.


Documentation
-------------

There is detailed tutorial on the project's website:
[http://lakhin.com/projects/papa-carlo/](http://lakhin.com/projects/papa-carlo/).

The tutorial is generated based mostly on materials from the
[wiki](https://github.com/Eliah-Lakhin/papa-carlo/wiki). The wiki is open for
read/write access to everyone. And contribution is very welcome!

Users support forum: [https://groups.google.com/forum/#!forum/papa-carlo](https://groups.google.com/forum/#!forum/papa-carlo).


##### Example parsers

 * [Malvina programming language compiler](https://github.com/Eliah-Lakhin/malvina-in-scala).
 * [JSON parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Json.scala).
 * [Calculator parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Calculator.scala).

##### Another links

 * [Introduction blog post](http://lakhin.com/blog/15.11.2013-handy-incremental-parser/).
 * [Discussion on Reddit](http://www.reddit.com/r/programming/comments/1rfyzx/whats_wrong_with_the_most_programming_language/).
 * [Introduction article on Habrahabr in Russian](http://habrahabr.ru/post/201774/).
 * [Approach brief description on Lambda the Ultimate](http://lambda-the-ultimate.org/node/4840).

If you have a project, or an article, or link to discussion related to the
topic, please bring me a line to: eliah.lakhin [at] gmail.com. Or start a topic
on the [Forum](https://groups.google.com/forum/#!forum/papa-carlo). I will be
glad to include it on the list.


Development status
------------------
Current version of the library is **0.7.0**. The project is in Beta stage.
All planned features are done and ready to use. Source code is covered by a
number of functional [tests](https://github.com/Eliah-Lakhin/papa-carlo/tree/master/src/test).

I use [Semantic Version policy v. 2.0](http://semver.org/) in naming project's
versions. So the first stable release version will be "1.0.0".

Please see change log for details:
[CHANGES](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/CHANGES.md)


JAR artifacts
--------------

 * Release artifacts on Maven Central:
   [http://central.maven.org/maven2/name/lakhin/eliah/projects/papacarlo/](http://central.maven.org/maven2/name/lakhin/eliah/projects/papacarlo/)
 * Snapshot and release artifacts on Sonatype:
   [http://oss.sonatype.org/content/groups/public/name/lakhin/eliah/projects/papacarlo/](http://oss.sonatype.org/content/groups/public/name/lakhin/eliah/projects/papacarlo/papa-carlo_2.10/)

##### Build from sources

1. Install [SBT](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt). Version 0.13.0 is preferable.
2. Run `sbt jvm/test` to compile and run tests.
3. Run `sbt jvm/packageBin` to build JAR package. The JAR package can be found in the `./jvm/target/` directory.
4. Run `sbt js-demo/optimizeJS` to build JavaScript demo using [Scala-JS](https://github.com/scala-js/scala-js) compiler. To start demo run static http web server with `./demo/` as a root.


License
-------

Please read [LICENSE](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/LICENSE) for
licensing details.


Author
------

Ilya Lakhin (Илья Александрович Лахин), eliah.lakhin [at] gmail.com
