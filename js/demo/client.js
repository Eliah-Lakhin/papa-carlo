var initParser = function(main) {
  if (!!window.Worker) {
    var worker = new Worker('./server.js'),
      api = function(params) {
        worker.postMessage({
          kind: 'input',
          params: params
        });
      };

    api.async = true;

    worker.onerror = function(error) {
      console.error(error);
    };

    worker.onmessage = function(event) {
      switch (event.data.kind) {
        case 'ready':
          main(api);

          break;

        case 'response':
          if (!!api.response) {
            api.response(event.data.delta, event.data.stats);
          }
      }
    };
  } else {
    d3.text(
      './target/scala-2.10/papa-carlo-opt.js',
      function(error, parserCode) {
        if (error) {
          console.error(error);
          return;
        }

        eval(parserCode);

        var parser = Demo(),
          api = function(params) {
            var start = new Date().getTime();
            parser.input.apply(parser, params);
            var end = new Date().getTime();
            if (!!api.response) {
              api.response(end - start, {
                nodeCount: parser.getNodeCount(),
                errors: parser.getErrors()
              });
            }
          };

        api.async = false;

        main(api);
      }
    );
  }
};
