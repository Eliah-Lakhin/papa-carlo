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
  var $progressBar = $('#progress');
  var $editor = $('#editor');
  var $errors = $('#errors').find('tbody');
  var $errorsCounter = $('#errors-counter');
  var $stats = {
    initTime: $('#stats-init-time'),
    lastTime: $('#stats-last-time'),
    lines: $('#stats-lines'),
    chars: $('#stats-chars'),
    ast: $('#stats-ast')
  };

  var lastTime = -1;

  var parser = Demo();

  $progressBar.css('width', '70%');
  $progressBar.text('User interface initialization...');

  $('#main').removeClass('hidden');

  var editor = CodeMirror.fromTextArea($editor[0], {
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

  $progressBar.css('width', '85%');
  $progressBar.text('Parsing bootstrap input code...');

  $.get('./input.json', function(code) {
    $progressBar.css('width', '90%');
    setTimeout(function() {
      editor.setValue(code);

      $progressBar.css('width', '100%');
      $('#loading').hide();
    }, 500);
  }, 'text');

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
      $errors.empty();

      if (errors.length > 0) {
        $errorsCounter
          .text('(' + errors.length + ')')
          .removeClass('hidden');

        errors.forEach(function(error, index) {
          errorMarkers.push(editor.markText(error.from, error.to, {
            className: 'cm-error',
            title: error.description
          }));

          var errorRow = $('<tr>');

          errorRow
            .append($('<td>').text(index + 1))
            .append($('<td>').text((error.from.line + 1) + ':' +
              (error.from.ch + 1)))
            .append($('<td>').text(error.description))
            .bind('click', function() {
              editor.setCursor(error.from);
              editor.focus();
            });

          $errors.append(errorRow);
        });
      } else {
        $errorsCounter.addClass('hidden');
      }
    }
  })();
});