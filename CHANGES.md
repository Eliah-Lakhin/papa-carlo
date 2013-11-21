Development history
===================

0.4.0
-----
_2013-11-21_

 - AST improvements:
   - Node's reference to it's parent.
   - Update event propagation up to root.
   - Node numeration order bug fixed.
 - Error recovery minor improvements.
 - SBT configuration major improvement.
 - Artifacts can be published to Sonatype.

0.3.0
-----
_2013-10-15_

 - Expression parsing rule based on Pratt's algorithm implemented.
 - Several parslet constructors for Expression parser implemented.
 - Calculator parser example implemented. As well as appropriate functional
   tests.
 - Minor improvements:
    - Avoid rule construction for simple rule nesting cases.
    - Some identifiers were renamed.

0.2.0
-----
_2013-10-07_

 - Lexer and Syntax parser grammar definition API changed from operator based to
   function based.
 - Three additional functional tests for Json example parser implemented. These
   three test were designed specifically to test various code fragmentation
   cases.
 - Minor refactoring:
   - Term "environment" renamed to "monitor" in context of functional tests.
   - Directory structure simplified.

0.1.0
-----
_2013-10-05_

The project source code was derived from the internal project "Malvina"
developed and copyrighted by Ilya Lakhin (Илья Александрович Лахин). The code is
published under the terms of Apache Public License v2:
http://www.apache.org/licenses/LICENSE-2.0.txt

Last commit in the private repository where the "Papa Carlo" was
derived from is:
Eliah-Lakhin/malvina-prototype-b@d1d863f227e79ee27c4d69fdce1eb8c5146dacd4
