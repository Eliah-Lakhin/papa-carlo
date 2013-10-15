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
source code when the user makes small changes to it. As a result it makes
possible to do a lot of small changes in the source code in real time having
actual Parse Tree without performance penalties.

This is a list of main Papa Carlo's features provided out of the box:
 * Incremental parsing.
 * Error-recovery mechanism.
   The parser can build Parse Tree even if the source code contains syntax
   errors.
 * Syntax definition directly in the Scala code using library's API.
 * Recursive-descendant parsing based on
   [PEG](http://en.wikipedia.org/wiki/Parsing_expression_grammar) grammars.
 * Expression parsing with
   [Pratt algorithm](http://en.wikipedia.org/wiki/Pratt_parser) and prepared
   primitives.


Development plan
----------------
Currently the project is in Alpha stage. So at this moment the most important
parts required to build full-feature parsers are done and tested. But there are
still a number of things that should be done before it will become Beta and
Final. During this process library's public API may be changed many times.

Here is a To-Do list:

 - ~~Lexical parser combinator.~~
 - ~~Fragment controller.~~
 - ~~Syntax parser combinator.~~
 - ~~Example of JSON parser.~~
 - ~~Functional tests for Json parser.~~
 - ~~Publish alpha version ready to use.~~
 - ~~Additional functional tests for complicated fragmentation cases.~~
 - ~~Support of [Pratt grammar](http://en.wikipedia.org/wiki/Pratt_parser).~~
 - ~~Example of Calculator parser and appropriate functional tests.~~
 - Library documentation and tutorials.

I use [Semantic Version policy](http://semver.org/) in naming project's
versions. So the Final and ready to use version will be "1.0.0".

Please see change log for details:
[CHANGES](https://github.com/Eliah-Lakhin/papa-carlo/blob/master/CHANGES.md)


How to build and test
---------------------
1. Install [Simple Build Tools](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt)
2. From the project's root dir type "sbt test" to compile the project and run
   tests


License
-------

Please read [LICENSE](https://github.com/Eliah-Lakhin/papa-carlo/LICENSE) for
licensing details.


Author
------

Ilya Lakhin (Илья Александрович Лахин), eliah.lakhin [at] gmail.com
