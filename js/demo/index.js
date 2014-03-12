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

var $progressBar = d3.select('#progress');
var $editor = d3.select('#editor');
var $errors = d3.select('#errors').select('tbody');
var $errorsCounter = d3.select('#errors-counter');
var $stats = {
  initTime: d3.select('#stats-init-time'),
  lastTime: d3.select('#stats-last-time'),
  lines: d3.select('#stats-lines'),
  chars: d3.select('#stats-chars'),
  ast: d3.select('#stats-ast')
};

var lastTime = -1;

var parser = Demo();

$progressBar.style('width', '70%');
$progressBar.text('User interface initialization...');

d3.select('#main').classed('hidden', false);

var editor = CodeMirror.fromTextArea($editor.node(), {
  mode: 'javascript',
  lineNumbers: true,
  styleActiveLine: true,
  matchBrackets: true,
  theme: 'mbo'
});

editor.on('change', function(self, changes) {
  var start = new Date().getTime();
  parser.input(
    changes.text.join("\n"),
    changes.from.line, changes.from.ch,
    changes.to.line, changes.to.ch
  );
  var end = new Date().getTime();

  if (lastTime < 0) {
    lastTime = end - start;
    $stats.initTime.text(lastTime);
  } else {
    lastTime = end - start;
  }

  updateStats();
  markErrors();
});

$progressBar.style('width', '85%');
$progressBar.text('Parsing bootstrap input code...');

d3.text('./input.json', function(error, code) {
  if (error) {
    console.error(error);
    return;
  }

  $progressBar.style('width', '90%');
  setTimeout(function() {
    editor.setValue(code);

    $progressBar.style('width', '100%');
    d3.select('#loading').classed('hidden', true);
  }, 500);
});

var updateStats = function() {
  $stats.lastTime.text(lastTime);
  $stats.lines.text(editor.lineCount());
  $stats.chars.text(editor.getValue().length);
  $stats.ast.text(parser.getNodeCount());
};

var markErrors = (function() {
  var errorMarkers = [];

  return function() {
    var errors = parser.getErrors();

    for (var marker; marker = errorMarkers.pop();) marker.clear();
    $errors.selectAll('*').remove();

    if (errors.length > 0) {
      $errorsCounter
        .text('(' + errors.length + ')')
        .classed('hidden', false);

      errors.forEach(function(error, index) {
        errorMarkers.push(editor.markText(error.from, error.to, {
          className: 'cm-error',
          title: error.description
        }));

        var errorRow = $errors.append('tr');

        errorRow.append('td').text(index + 1);
        errorRow.append('td').text((error.from.line + 1) + ':' +
          (error.from.ch + 1));
        errorRow.append('td').text(error.description);

        errorRow.on('click', function() {
            editor.setCursor(error.from);
            editor.focus();
          });
      });
    } else {
      $errorsCounter.classed('hidden', true);
    }
  }
})();
