importScripts('./target/scala-2.10/papa-carlo-opt.js');

var parser = Demo();

function getStats() {
  return {
    nodes: parser.getNodeStats(),
    errors: parser.getErrors()
  }
}

onmessage = function(event) {
  switch (event.data.kind) {
    case 'input':
      var start = new Date().getTime();
      parser.input.apply(parser, event.data.params);
      var end = new Date().getTime();
      postMessage({kind: 'response', delta: end - start, stats: getStats()});
      break;
  }
};

postMessage({kind: 'ready'});

