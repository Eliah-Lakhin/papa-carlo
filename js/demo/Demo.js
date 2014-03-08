/*
   Copyright 2013 Ilya Lakhin (Илья Александрович Лахин)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

$(function() {
  var $editor = $('#editor');
  var $errors = $('#errors');
  var parser =
    ScalaJS.modules.name_lakhin_eliah_projects_papacarlo_js_demo_Demo();

  parser.input($editor.text());

  var errorMarkers = [];

  CodeMirror
    .fromTextArea($editor[0], {
      mode: 'javascript',
      lineNumbers: true,
      styleActiveLine: true,
      matchBrackets: true,
      theme: 'mbo'
    })
    .on('change', function (self, changes) {
      parser.input(
        changes.text.join("\n"),
        changes.from.line, changes.from.ch,
        changes.to.line, changes.to.ch
      );

      for (var marker; marker = errorMarkers.pop();) marker.clear();
      $errors.empty();

      if (parser.hasErrors()) {
        var errors = parser.getErrors();
        for (var error; error = errors.shift();) {
          errorMarkers.push(self.markText(error.from, error.to, {
            className: 'cm-error',
            title: error.description
          }));

          var errorItem = $('<li>');

          errorItem.text('(' + (error.from.line + 1) + ', ' +
            (error.from.ch + 1) + ') - (' + (error.to.line + 1) + ', ' +
            (error.to.ch + 1) + '): ' + error.description);

          $errors.prepend(errorItem);
        }
      }
    });
});