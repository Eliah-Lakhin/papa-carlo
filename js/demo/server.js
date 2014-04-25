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

importScripts('./target/scala-2.10/papa-carlo-opt.js');

var parser = Demo();

function getStats(data) {
  return {
    nodes: parser.getAST(data.ast),
    errors: parser.getErrors()
  }
}

onmessage = function(event) {
  switch (event.data.kind) {
    case 'fragment':
      postMessage({
        kind: 'fragment',
        response: parser.getNodeFragment(event.data.id)
      });

      break;

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

