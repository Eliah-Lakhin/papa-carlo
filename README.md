About Papa Carlo
----------------

Constructor of incremental parsers in Scala using PEG grammars.

Papa Carlo was designed to construct programming language parsers that may be
used in a wide range of code editors from simple syntax highlighters to
full-featured IDEs like Eclipse or IntelliJ Idea.

It also may be a useful tool to improve programming language syntax support in
a number of popular source code editors like Sublime Text, Notepad++ or even
terminal-based like Vim and Emacs.

The core feature of the parsers made with Papa Carlo is utilizing incremental
parsing approach. That means the parser doesn't need to parse again the whole
source code when end users make small changes to it. Thus users can continuously
edit source code with thousands lines in real time. And the parser keeps Parse
Tree in touch with the source code permanently without performance penalties.

This is a list of the main Papa Carlo's features provided out of the box:
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

Example parsers:

 * [JSON parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Json.scala).
 * [Calculator parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Calculator.scala).


Getting started
---------------

##### Using distributed artifacts

1. Install [SBT](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt).
2. Add dependency to the project's configuration: `libraryDependencies += "name.lakhin.eliah.projects.papacarlo" %% "papa-carlo" % "<lib version>"`.

Remote artifact URLs:

 * Release artifacts on Maven Central:
   [http://central.maven.org/maven2/name/lakhin/eliah/projects/papacarlo/](http://central.maven.org/maven2/name/lakhin/eliah/projects/papacarlo/)
 * Snapshot and release artifacts on Sonatype:
   [http://oss.sonatype.org/content/groups/public/name/lakhin/eliah/projects/papacarlo/](http://oss.sonatype.org/content/groups/public/name/lakhin/eliah/projects/papacarlo/papa-carlo_2.10/)

##### Build from sources

1. Install [SBT](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt). Version 0.13.0 is preferable.
2. Run `sbt test` to compile and run tests.
3. Run `sbt packageBin` to build JAR package. The JAR package can be found in
   the `target/` directory.


Documentation
-------------

There is detailed tutorial on the project's website:
[http://lakhin.com/projects/papa-carlo/](http://lakhin.com/projects/papa-carlo/).

The tutorial is generated based mostly on materials from the
[wiki](https://github.com/Eliah-Lakhin/papa-carlo/wiki). The wiki is open for
read/write access to everyone. And contribution is very welcome.

##### Another links

 * [Introduction blog post](http://lakhin.com/blog/15.11.2013-handy-incremental-parser/).
 * [Discussion on Reddit](http://www.reddit.com/r/programming/comments/1rfyzx/whats_wrong_with_the_most_programming_language/).
 * [Introduction article on Habrahabr in Russian](http://habrahabr.ru/post/201774/).
 * [Approach brief description on Lambda the Ultimate](http://lambda-the-ultimate.org/node/4840).

If you have a project, or an article, or link to discussion related to the the
topic, please bring me a line to: eliah.lakhin [at] gmail.com. I will be glad
to include it on the list.


Development status
------------------
The current version of the library is **0.5.0**.

I use [Semantic Version policy v. 2.0](http://semver.org/) in naming project's
versions. So the first stable release version will be "1.0.0".

Please see change log for details:
[CHANGES](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/CHANGES.md)

---

Currently the project is in Beta stage. All planned features are done and ready
to use. Source code is covered by a number of functional
[tests](https://github.com/Eliah-Lakhin/papa-carlo/tree/master/src/test).

Before the project becomes in stable stage, ready to use in production it must
pass a "trial by combat". My another project includes two components that will
be built on top of Papa Carlo:
 * Incremental compiler of the general purpose programming language Malvina with
   static type system.
 * Web-based IDE for this language. With deep code syntax/semantic analysis and
   manipulation features.

I hope these two projects could become a proof of readiness the library to use
in production.


License
-------

Please read [LICENSE](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/LICENSE) for
licensing details.


Author
------

Ilya Lakhin (Илья Александрович Лахин), eliah.lakhin [at] gmail.com
