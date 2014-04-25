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
  var
    lastTime = -1,
    selectedNode = -1;

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

    updateStats(data);
    markErrors(data);
    logPerformance(delta, data);
    if (data.nodes.all) {
      ast(data.nodes.all, data.nodes.added, data.nodes.removed);
    }
  };

  parser.receiveFragment = (function() {
    var
      fragment = null,
      marker = null;

    return function(data) {
      fragment = data;

      if (marker) {
        marker.clear();
        marker = null;
      }

      if (fragment && fragment.exists && fragment.id === selectedNode) {
        marker = editor.markText(fragment.from, fragment.to, {
          className: 'cm-node',
          title: 'Node: ' + fragment.id
        });

        editor.setCursor(fragment.from);
        editor.focus();
      }
    };
  })();

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
    statsPane: d3.select('#logs'),
    performance: d3.select('#performance')
  };

  $('a[href="#logs"]').bind('shown.bs.tab', function() {
    logPerformance();
  });

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
    parser(self.getValue('\n'));
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
    if (!parser.busy) {
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
    var
      logs = [],
      barWidth = 20,
      container = $stats.performance.node(),
      mainGroup = $stats.performance.append('g'),
      barGroup = mainGroup.append('g'),
      timeAxis = d3.svg.axis()
        .ticks(4)
        .tickFormat(function(d) { return d + 'µ'; }),
      timeGroup = mainGroup.append('g').attr('class', 'axis'),
      marginV = 20,
      marginH = 40;

    timeAxis.orient('left');

    mainGroup.attr('transform', 'translate(' + marginH + ',' + marginV + ')');

    var
      index = 0,
      skipped = 0;
    logs.push({index: 0, time: 0, nodes: {total: 0, added: 0, removed: 0}});

    var clip = barGroup
      .append('defs').append('clipPath')
      .attr('id', 'perf-clip')
      .append('rect')
      .attr('width', 2000)
      .attr('height', 500);

    barGroup.attr('clip-path', 'url("#perf-clip")');

    return function(delta, data) {
      if (delta > 0) {
        logs.push({
          index: ++index,
          time: delta,
          nodes: {
            total: data.nodes.total,
            added: data.nodes.added.length,
            removed: data.nodes.removed.length
          }
        });
      }

      if (!$stats.statsPane.node().classList.contains('active')) {
        return;
      }

      var
        width = container.clientWidth - marginH * 2,
        height = container.clientHeight - marginV * 2;

      clip.attr('width', width);

      while (logs.length > width / barWidth * 1.5) {
        logs.shift();
        skipped++;
      }

      var y = d3.scale.linear()
        .domain([0, d3.max(logs, function(d) { return d.time; })])
        .range([height, 0]);

      timeAxis.scale(y);
      timeGroup.call(timeAxis);

      function translateGroups(addition) {
        var
          localClip = clip,
          localBars = barGroup,
          offset = width - (index - skipped) * barWidth;

        if (!addition) {
          localClip = localClip.transition();
          localBars = localBars.transition();
        } else {
          offset += barWidth * addition
        }

        offset = Math.min(0, offset);

        localClip.attr('transform', 'translate(' + (-offset) + ',0)');
        localBars.attr('transform', 'translate(' + offset + ',0)');
      }

      if (skipped > 0) {
        translateGroups(1);
      }

      translateGroups();

      var bars = barGroup
        .selectAll('.bar')
        .data(logs, function(d) { return d.index; });

      function updateHeight(bar) {
        bar
          .transition()
          .attr('y', function(d) { return y(d.time); })
          .attr('height', function(d) { return height - y(d.time); });
      }

      function updateOffset(barGroup) {
        barGroup = barGroup.attr('transform', function(d) {
          return 'translate(' + ((d.index - skipped) * barWidth) + ',0)';
        });
      }

      updateOffset(bars);
      updateHeight(bars.select('rect'));

      var newBar = bars
        .enter()
        .append('g')
        .attr('class', 'bar')
        .call(updateOffset, true);

      newBar
        .append('title')
        .text(function(d) {
          return 'AST total node count: ' + d.nodes.total +
            '; new nodes: ' + d.nodes.added +
            '; removed nodes: ' + d.nodes.removed +
            '; affected: ' + (d.nodes.added + d.nodes.removed + 1);
        });

      newBar
        .append('rect')
        .attr('x', 1)
        .attr('width', barWidth - 1)
        .call(updateHeight);

      newBar
        .append('text')
        .text(function(d) { return d.nodes.added + d.nodes.removed + 1; })
        .attr('x', barWidth / 2)
        .attr('y', function() { return y(0) + 20; });

      bars.exit().remove();
    };
  })();

  var ast = (function() {
    var
      $svg = d3.select('#ast'),
      distance = 20
      force = d3.layout.force()
        .linkDistance(distance)
        .charge(-90)
        .gravity(0.1)
        .size([$svg.node().clientWidth, $svg.node().clientHeight]),
      nodes = force.nodes(),
      links = force.links(),
      node = $svg.selectAll('.node'),
      link = $svg.selectAll('.link');

    nodes.push({
      index: 0,
      x: force.size()[0] / 2,
      y: force.size()[1] / 2,
      fixed: true,
      orig: {id: 1}
    });

    force.on('tick', function() {
      link
        .attr('x1', function(d) { return d.source.x; })
        .attr('y1', function(d) { return d.source.y; })
        .attr('x2', function(d) { return d.target.x; })
        .attr('y2', function(d) { return d.target.y; });

      node.attr('transform', function(d) {
        return 'translate(' + d.x + ',' + d.y + ')';
      });
    });

    function getNodeId(node) {
      return node.orig.id;
    }

    function restart() {
      link = link.data(links);
      link.enter().insert('line', '.node').attr('class', 'link');
      link.exit().remove();

      node = node.data(nodes, getNodeId);

      setColor(node);

      var newNode = node
        .enter()
        .append('g').attr('class', 'node')
        .call(force.drag)

      newNode.on('click', function(d) {
        selectedNode = d.orig.id;
        setColor(node, 250);
        parser.getFragment(selectedNode);
      });

      function setColor(node, transition) {
        node = node.transition().duration(transition || 3000);

        node
          .selectAll('circle')
          .attr('fill', function(d) {
            if (d.orig.id === selectedNode) {
              return '#3c763d';
            }

            return d.fixed ? '#ffcf6c' : '#ffffff';
          });

        node
          .selectAll('text')
          .attr('fill', function(d) {
            return d.orig.id === selectedNode ? '#fff' : '#000';
          });
      }

      newNode
        .append('circle')
        .attr('r', 10)
        .attr('fill', '#31708f');

      newNode
        .append('text')
        .attr('y', 3)
        .text(function(d) { return d.orig.id; });

      setColor(newNode);

      node.exit().remove();

      force.start();
    }

    restart();

    function nodeIndex(id) {
      for (var index = 0, length = nodes.length; index < length; index++) {
        if (nodes[index].orig.id === id) {
          return index;
        }
      }

      return -1;
    }

    function removeNode(idToRemove) {
      nodes.splice(nodeIndex(idToRemove), 1);
    }

    function addNode(node) {
      var
        index = nodes.length,
        parent = nodes[nodeIndex(node.parent)];

      nodes.push({
        index: index,
        x: parent.x + (Math.random() - 0.5) * distance,
        y: parent.y + (Math.random() - 0.5) * distance,
        orig: node
      });
    }

    return function(tree, added, removed) {
      removed.forEach(removeNode);
      added.map(function(id) {return tree[id]; }).forEach(addNode);
      
      links.splice(0, links.length);

      nodes.forEach(function(node, index) {
        node.orig = tree[node.orig.id];

        var parentIndex = nodeIndex(node.orig.parent);

        if (parentIndex >= 0) {
          links.push({source: index, target: parentIndex});
        }
      });

      if (selectedNode && !tree[selectedNode]) {
        selectedNode = false;
        parser.receiveFragment();
      }

      restart();
    };
  })();
});

