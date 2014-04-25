var initParser = function(main) {
  if (!!window.Worker) {
    var
      worker = new Worker('./server.js'),
      buffer = null,
      api = function(code) {
        if (api.busy) {
          buffer = code;
          return;
        }

        api.busy = true;

        worker.postMessage({
          kind: 'input',
          code: code,
          ast: api.ast
        });
      };

    api.ast = true;
    api.async = true;
    api.busy = false;

    worker.onerror = function(error) {
      console.error(error);
    };

    worker.onmessage = function(event) {
      switch (event.data.kind) {
        case 'ready':
          main(api);

          break;

        case 'response':
          api.busy = false;

          if (buffer !== null) {
            var code = buffer;
            buffer = null;
            api(code);
          }

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

        var
          parser = Demo(),
          buffer = null,
          api = function(code) {
            if (api.busy) {
              buffer = true;
              return;
            }

            api.busy = true;
            var start = new Date().getTime();
            parser.inputAll(code);
            var end = new Date().getTime();
            api.busy = false;

            if (!!api.response) {
              api.response(end - start, {
                nodes: parser.getNodeStats(true),
                errors: parser.getErrors()
              });
            }

            if (buffer !== null) {
              code = buffer;
              buffer = null;
              api(code);
            }
          };

        api.ast = true;
        api.async = false;
        api.busy = false;

        main(api);
      }
    );
  }
};

