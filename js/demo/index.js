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

initParser(function(parser) {
  var lastTime = -1,
    retain = 0;

  parser.response = function(delta, data) {
    if (lastTime < 0) {
      lastTime = delta;
      $stats.initTime.text(lastTime);
      $progressBar.style('width', '100%');
      d3.select('#loading')
        .transition().delay(1000).style('opacity', 0)
        .transition().style('display', 'none');
    } else {
      lastTime = delta;
    }

    retain--;

    updateStats(data);
    markErrors(data);
    logPerformance(delta, data);
  };

  var $progressBar = d3.select('#progress');
  var $editor = d3.select('#editor');
  var $errors = d3.select('#errors').select('tbody');
  var $errorsCounter = d3.select('#errors-counter');
  var $stats = {
    ready: d3.select('#stats-ready'),
    mode: d3.select('#stats-mode'),
    initTime: d3.select('#stats-init-time'),
    lastTime: d3.select('#stats-last-time'),
    lines: d3.select('#stats-lines'),
    chars: d3.select('#stats-chars'),
    ast: d3.select('#stats-ast'),
    performance: d3.select('#performance')
  };

  $progressBar.style('width', '70%');
  $progressBar.text('User interface initialization...');

  d3.select('#main').classed('hidden', false);

  if (parser.async) {
      $stats.mode.text('Asynchronous. Parser works in WebWorker');
  } else {
      $stats.mode.text('Synchronous. WebWorkers unavailable');
  }

  var editor = CodeMirror.fromTextArea($editor.node(), {
    mode: 'javascript',
    lineNumbers: true,
    styleActiveLine: true,
    matchBrackets: true,
    theme: 'mbo'
  });

  editor.on('change', function(self, changes) {
    $stats.ready.text('busy');
    retain++;
    parser([
      changes.text.join("\n"),
      changes.from.line, changes.from.ch,
      changes.to.line, changes.to.ch
    ]);
  });

  $progressBar.style('width', '85%');
  $progressBar.text('Parsing bootstrap input code...');

  d3.text('./input.json', function(error, code) {
    if (error) {
      console.error(error);
      return;
    }

    $progressBar.style('width', '90%');
    editor.setValue(code);
  });

  var updateStats = function(data) {
    if (retain <= 0) {
      $stats.ready.text('ready');
    }
    $stats.lastTime.text(lastTime);
    $stats.lines.text(editor.lineCount());
    $stats.chars.text(editor.getValue().length);
    $stats.ast.text(data.nodes.total);
  };

  var markErrors = (function() {
    var errorMarkers = [];

    return function(data) {
      var errors = data.errors;

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

  var logPerformance = (function() {
    var logs = [];

    return function(delta, data) {
      console.log(data);
      $stats.performance
        .selectAll('.bar')
        .data(data)
        .enter()
        .append('rect')
        .attr('class', '.bar')
        .attr('x', function(d) { return d * 100; })
        .attr('width', 75)
        .attr('height', 50);
    };
  })();
});

