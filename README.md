About Papa Carlo
----------------

Constructor of incremental parsers in Scala using PEG grammars.

Papa Carlo was designed to construct programming language parsers that may be
used in a wide range of code editors from simple syntax highlighters to
full-feature IDEs like Eclipse or IntelliJ Idea.

It also may be a useful tool to improve programming language syntax support in
a number of popular source code editors like Sublime Text, Notepad++ or even
terminal-based like Vim and Emacs.

The core feature of the parsers made with Papa Carlo is utilizing incremental
parsing approach. That means the parser doesn't need to parse again the whole
source code when the user makes small changes to it. Thus user can continuously
edit source code with thousands lines in real time. And the parser keeps Parse
Tree in touch with the source code permanently without performance penalties.

This is a list of main Papa Carlo's features provided out of the box:
 * Incremental parsing.
 * Error-recovery mechanism.
   The parser can build Parse Tree even if the source code contains syntax
   errors.
 * Syntax definition directly in the Scala code using library's API.
 * Recursive descent parsing based on
   [PEG](http://en.wikipedia.org/wiki/Parsing_expression_grammar) grammars.
 * Expression parsing with
   [Pratt algorithm](http://en.wikipedia.org/wiki/Pratt_parser) and prepared
   primitives.

Example parsers:
 * [JSON parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Json.scala).
 * [Calculator parser](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/src/main/scala/name.lakhin.eliah.projects/papacarlo/examples/Calculator.scala).

Additional information:
 * [Introduction blog post](http://lakhin.com/blog/15.11.2013-handy-incremental-parser/).
 * [Problem description](https://github.com/Eliah-Lakhin/papa-carlo/wiki/What-is-it-about).


How to learn
------------

There is detailed tutorial on the project's website:
[http://lakhin.com/projects/papa-carlo/](http://lakhin.com/projects/papa-carlo/).

---

The tutorial is generated based mostly on materials from the
[wiki](https://github.com/Eliah-Lakhin/papa-carlo/wiki). The wiki is open for
read/write access to everyone. And contribution is very welcome.


Development plan
----------------
Currently the project is in Beta stage. All planned features are done and ready
to use. Source code is covered by a number of functional
[tests](https://github.com/Eliah-Lakhin/papa-carlo/tree/master/src/test).

Before the project becomes in stable stage, ready to use in production it must
pass a "trial by combat". My another project includes two components that will
be built on top of Papa Carlo:
 * Compiler of the general purpose programming language Malvina with static
   type system.
 * Web-based Integrated Development Environment for this language. With deep
   code syntax/semantic analysis and manipulation features.

I hope these two projects could become a proof of readiness the library to use
in production.

Meanwhile if you decide to use the library in your project right now, I will
gladly assist you. Please, feel free to contact me by email/jabber
eliah.lakhin [at] gmail.com.


Current version
---------------
The current version of the library is **0.4.0**.

I use [Semantic Version policy](http://semver.org/) in naming project's
versions. So the first stable release version will be "1.0.0".

Please see change log for details:
[CHANGES](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/CHANGES.md)


How to build and test
---------------------
1. Install [Simple ProjectBuildConfig Tools](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt)
2. From the project's root dir type "sbt test" to compile the project and run
   tests


License
-------

Please read [LICENSE](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/LICENSE) for
licensing details.


Author
------

Ilya Lakhin (Илья Александрович Лахин), eliah.lakhin [at] gmail.com
