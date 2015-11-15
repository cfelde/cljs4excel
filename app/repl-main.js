global = this;
$(function() {

  // Creating the console.
  window.jqconsole = $('#console').jqconsole(null, cljs_bootstrap.core.get_prompt());

  cljs.core._STAR_print_fn_STAR_ = function () {
    var str = Array.prototype.join.call(arguments, " ")
    jqconsole.Write(str + "\n", 'jqconsole-output');
  }

  jq_load_history(jqconsole);

  // Abort prompt on Ctrl+C.
  jqconsole.RegisterShortcut('C', function() {
    jqconsole.AbortPrompt();
    handler();
  });
  // Move to line start Ctrl+A.
  jqconsole.RegisterShortcut('A', function() {
    jqconsole.MoveToStart();
    handler();
  });
  // Move to line end Ctrl+E.
  jqconsole.RegisterShortcut('E', function() {
    jqconsole.MoveToEnd();
    handler();
  });
  jqconsole.RegisterMatching('{', '}', 'brace');
  jqconsole.RegisterMatching('(', ')', 'paren');
  jqconsole.RegisterMatching('[', ']', 'bracket');
  jqconsole.RegisterMatching('"', '"', 'dquote');
  var print_exception = function(exc) {
    if (exc.cause) {
      jqconsole.Write(exc.cause.message + '\n', 'jqconsole-error');
      jqconsole.Write(exc.cause.stack + '\n', 'jqconsole-error');
    } else if (exc.stack) {
      jqconsole.Write(exc.stack + '\n', 'jqconsole-error');
    } else {
      jqconsole.Write(exc + '\n', 'jqconsole-error');
    }
  }
  // Handle a command.
  var handler = function(line) {
    if (line) {
      try {
        cljs_bootstrap.core.read_eval_print(line, function(success, result) {
          if (success) {
            jqconsole.Write(result + '\n', 'jqconsole-return');
          } else {
            print_exception(result);
          }
          jq_save_history(jqconsole);
          jqconsole.SetPromptLabel(cljs_bootstrap.core.get_prompt());
          jqconsole.Prompt(true, handler);
        });
      } catch (exc) {
        print_exception(exc);
        jqconsole.SetPromptLabel(cljs_bootstrap.core.get_prompt());
        jqconsole.Prompt(true, handler);
      }
    }
    /*
    jqconsole.Prompt(true, handler, function(command) {
      // Continue line if can't compile the command.
      try {
        Function(command);
      } catch (e) {
        if (/[\[\{\(]$/.test(command)) {
          return 1;
        } else {
          return 0;
        }
      }
      return false;
    });
  */
  };

  // Load cljs4excel ClojureScript code
  cljs_bootstrap.core.read_eval_print('(.loadScript js/app "cljs4excel0.cljs")',
    function(success, result) {
      // TODO: Should report on error
    }
  );
  // Test end

  // Initiate the first prompt.
  handler();
  jqconsole.Prompt(true, handler);
});
