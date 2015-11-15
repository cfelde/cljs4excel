var app = (function () {
  "use strict";

  var app = {};

  // Common initialization function (to be called from each page)
  app.initialize = function () {
    $('body').append(
      '<div id="notification-message">' +
      '<div class="padding">' +
      '<div id="notification-message-close"></div>' +
      '<div id="notification-message-header"></div>' +
      '<div id="notification-message-body"></div>' +
      '</div>' +
      '</div>');

      $('#notification-message-close').click(function () {
        $('#notification-message').hide();
      });

      // After initialization, expose a common notification function
      app.showNotification = function (header, text) {
        $('#notification-message-header').text(header);
        $('#notification-message-body').text(text);
        $('#notification-message').slideDown('fast');
      };

      app.loadScript = function (url) {
        // TODO: This should all be improved to deal with loading errors, etc..
        var id = new Date().getTime();
        var ifr = $('<iframe/>', {
          id: 'sl-' + id,
          src: url,
          style: 'display:none',
          load: function () {
            var lines = $(this).contents().text().split(/\r?\n/);
            lines.forEach(function (element, index, array) {
              cljs_bootstrap.core.read_eval_print(element, function (success, result) { });
            });
            ifr.remove();
          }
        });
        $('body').append(ifr);
      };

      app.getSelection = function (callback) {
        Office.context.document.getSelectedDataAsync(Office.CoercionType.Matrix,
          function (result) {
            if (result.status === Office.AsyncResultStatus.Succeeded) {
              callback(result.value);
            } else {
              app.showNotification('Error:', result.error.message);
            }
          }
        );
      }

      app.setSelection = function (matrix) {
        Office.context.document.setSelectedDataAsync(
          matrix,
          { coercionType: Office.CoercionType.Matrix },
          function (result) {
            if (result.status == "failed") {
              app.showNotification('Error:', result.error.message);
            }
          }
        );
      }

      app.addBindingFromNamedItem = function (name, id, callback) {
        // TODO Testing of named item
        Office.context.document.bindings.addFromNamedItemAsync(name,
          Office.BindingType.Matrix,
          { id: id },
          function (result) {
            if (result.status === Office.AsyncResultStatus.Succeeded) {
              callback(result.value.id, result.value.type, result.value.columnCount, result.value.rowCount);
            } else {
              app.showNotification('Error:', result.error.message);
            }
          }
        );
      }

      app.addBindingFromPrompt = function (id, callback) {
        Office.context.document.bindings.addFromPromptAsync(Office.BindingType.Matrix,
          { id: id, promptText: 'Select text to bind to.' },
          function (result) {
            if (result.status === Office.AsyncResultStatus.Succeeded) {
              callback(result.value.id, result.value.type, result.value.columnCount, result.value.rowCount);
            } else {
              app.showNotification('Error:', result.error.message);
            }
          }
        );
      }

      app.addBindingFromSelection = function (id, callback) {
        Office.context.document.bindings.addFromSelectionAsync(Office.BindingType.Matrix,
          { id: id },
          function (result) {
            if (result.status === Office.AsyncResultStatus.Succeeded) {
              callback(result.value.id, result.value.type, result.value.columnCount, result.value.rowCount);
            } else {
              app.showNotification('Error:', result.error.message);
            }
          }
        );
      }

      app.getAllBindings = function (callback) {
        Office.context.document.bindings.getAllAsync(function (result) {
          if (result.status === Office.AsyncResultStatus.Succeeded) {
            callback(result.value.map(function(v) {return v.id}));
          } else {
            app.showNotification('Error:', result.error.message);
          }
        });
      }

      app.getBindingDetails = function (id, callback) {
        Office.context.document.bindings.getByIdAsync(id, function (result) {
          if (result.status === Office.AsyncResultStatus.Succeeded) {
            callback(result.value.id, result.value.type, result.value.columnCount, result.value.rowCount);
          } else {
            app.showNotification('Error:', result.error.message);
          }
        });
      }

      app.getBindingData = function (id, callback) {
        Office.context.document.bindings.getByIdAsync(id, function (result1) {
          if (result1.status === Office.AsyncResultStatus.Succeeded) {
            result1.value.getDataAsync(function (result2) {
              if (result2.status === Office.AsyncResultStatus.Succeeded) {
                callback(result1.value.id, result1.value.type, result1.value.columnCount, result1.value.rowCount, result2.value);
              } else {
                app.showNotification('Error:', result2.error.message);
              }
            });
          } else {
            app.showNotification('Error:', result1.error.message);
          }
        });
      }

      app.setBindingData = function (id, matrix) {
        Office.context.document.bindings.getByIdAsync(id, function (result1) {
          if (result1.status === Office.AsyncResultStatus.Succeeded) {
            result1.value.setDataAsync(matrix, function (result2) {
              if (result2.status === Office.AsyncResultStatus.Failed) {
                app.showNotification('Error:', result2.error.message);
              }
            });
          } else {
            app.showNotification('Error:', result1.error.message);
          }
        });
      }

      app.addBindingDataEvent = function (id, callback) {
        Office.context.document.bindings.getByIdAsync(id, function (result1) {
          if (result1.status === Office.AsyncResultStatus.Succeeded) {
            result1.value.addHandlerAsync(Office.EventType.BindingDataChanged, function (event) {
              callback(event.binding.id);
            }, function (result2) {
              if (result2.status === Office.AsyncResultStatus.Failed) {
                app.showNotification('Error:', result2.error.message);
              }
            });
          } else {
            app.showNotification('Error:', result1.error.message);
          }
        });
      }

      app.removeBinding = function (id) {
        Office.context.document.bindings.releaseByIdAsync(id, function (result) {
          if (result.status === Office.AsyncResultStatus.Failed) {
            app.showNotification('Error:', result.error.message);
          }
        });
      }

      app.removeBindingDataEvent = function (id) {
        Office.context.document.bindings.getByIdAsync(id, function (result1) {
          if (result1.status === Office.AsyncResultStatus.Succeeded) {
            result1.value.removeHandlerAsync(Office.EventType.BindingDataChanged, function (result2) {
              if (result2.status === Office.AsyncResultStatus.Failed) {
                app.showNotification('Error:', result2.error.message);
              }
            });
          } else {
            app.showNotification('Error:', result1.error.message);
          }
        });
      }
    };

    return app;
  })();

  (function () {
    "use strict";

    // The initialize function must be run each time a new page is loaded
    Office.initialize = function (reason) {
      $(document).ready(function () {
        app.initialize();
      });
    };
  })();
