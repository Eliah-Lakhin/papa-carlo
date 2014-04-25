importScripts('./target/scala-2.10/papa-carlo-opt.js');

var parser = Demo();

function getStats(data) {
  return {
    nodes: parser.getNodeStats(data.ast),
    errors: parser.getErrors()
  }
}

onmessage = function(event) {
  switch (event.data.kind) {
    case 'input':
      var start = new Date().getTime();
      parser.inputAll(event.data.code);
      var end = new Date().getTime();

      postMessage({
        kind: 'response',
        delta: end - start,
        stats: getStats(event.data)
      });

      break;
  }
};

postMessage({kind: 'ready'});

