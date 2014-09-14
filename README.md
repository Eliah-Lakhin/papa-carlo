What is "incremental parser"?
-----------------------------

Papa Carlo is the first Scala parsing library utilises incremental parsing
approach.

An incremental parser is one that can recompile only those portions of a program
that have been modified. Ordinary parsers must process entire source code file.

Therefore, when the end user makes small and frequent changes in the source
code, an incremental parser indexes them immediately, without any
significant time delays. Even if the program consists of thousands lines of
code.

This property is very important in development of programming language analysis
tools such as Integrated Development Environments that managing codebase in real
time.

See the [Demo Webapp](http://lakhin.com/projects/papa-carlo/demo/) of the
incremental JSON parser based on Papa Carlo.

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

Contribution policy
-------------------

##### Branch quick reference
 * `master`: Primary GitHub branch that one should fork from and Pull Request to.
   This branch is for upcoming release commits. So all of the accepted Pull Request
   need to be tested at least with provided functional tests
 * `release`: All release commits starting from the version 0.7.1
 * `development`: This branch may contain intermediate development commits that
   are not well tested. It is used internally by the repository maintainers

##### Steps

 1. Fork the master branch of the repository to your GitHub account.
 2. Test your changes locally using `sbt jvm/test` command.
 3. Commit your code changes and push them to your fork on GitHub.
 4. Make a Pull Request back to the master branch of [Eliah-Lakhin/papa-carlo](https://github.com/Eliah-Lakhin/papa-carlo).
 5. Repository maintainers will review your Pull Request and merge in a few days.
 6. New master branch will be built and the artifact will be pushed to
    [Sonatype's Snapshot Repository](http://oss.sonatype.org/content/groups/public/name/lakhin/eliah/projects/papacarlo/papa-carlo_2.10/).
 7. Sooner or later `master` branch with your commits will be merged onto
    `releases` branch, and the new release artifact will appear in
    [Maven Central](http://central.maven.org/maven2/name/lakhin/eliah/projects/papacarlo/).
    Also I'll add you to the
    [Contributors List](https://github.com/Eliah-Lakhin/papa-carlo/blob/a78fc592d8499b9f1f209b64114d45a276813986/project/PapaCarlo.scala#L98).

If you find something that needs to be reviewed and merged quickly(important bug
or something that stucks your development process), please poke me by
email/GoogleTalk: eliah.lakhin [at] gmail.com. Or by Skype: eliah.lakhin. I'll
do the review as soon as possible.

##### License

Please read [LICENSE](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/LICENSE) for
licensing details.


Related projects
----------------
 * [Papa Carlo's Aunt](https://github.com/nightscape/papa-carlos-aunt). ANTLR4
   grammar port for Papa Carlo by [Martin Mauch](https://github.com/nightscape).
 * [Java Incremental Parser](https://github.com/ftomassetti/JavaIncrementalParser)
   by [Federico Tomassetti](https://github.com/ftomassetti).
 * [Malvina programming language compiler](https://github.com/Eliah-Lakhin/malvina-in-scala).


Documentation
-------------

There is detailed tutorial on the project's website:
[http://lakhin.com/projects/papa-carlo/](http://lakhin.com/projects/papa-carlo/).

The tutorial is generated based mostly on materials from the
[wiki](https://github.com/Eliah-Lakhin/papa-carlo/wiki). The wiki is open for
read/write access to everyone. And contribution is very welcome!

Users support forum: [https://groups.google.com/forum/#!forum/papa-carlo](https://groups.google.com/forum/#!forum/papa-carlo).

##### Example parsers

 * [JSON parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Json.scala).
 * [Calculator parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Calculator.scala).

##### Another links

 * [Introduction blog post](http://lakhin.com/blog/15.11.2013-handy-incremental-parser/).
 * [Scala Days 2014](http://www.parleys.com/play/53a7d2cbe4b0543940d9e555/chapter35/agenda) presentation by [Sébastien Doeraene](https://github.com/sjrd).
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
4. Run `sbt js-demo/optimizeJS` to build JavaScript demo using [Scala-JS](https://github.com/scala-js/scala-js) compiler. To start demo run static http web server with `./js/demo/` as a root.


Author
------

Ilya Lakhin (Илья Александрович Лахин), eliah.lakhin [at] gmail.com
