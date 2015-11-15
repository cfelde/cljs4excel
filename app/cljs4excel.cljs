(defn js->fn
  "Take a function and JSON, convert JSON to a Clojure structure and pass it to the function."
  [fn js] (fn (js->clj js)))

(defn binding->fn
  "Called from JS for a Excel document binding.
   The given function is given a map with these keys:
   :id User provided id for binding.
   :type Type of binding, currently only matrix.
   :cc Binding column count.
   :rc Binding row count."
   [fn id type cc rc] (fn {:id id
                            :type type
                            :cc cc
                            :rc rc}))

(defn binding-data->fn
  "Similar to binding->fn, but includes binding data keyed under :data."
  [fn id type cc rc data] (fn {:id id
                                :type type
                                :cc cc
                                :rc rc
                                :data (js->clj data)}))

(defn get-selection
  "Used to get the current document selection."
  [fn] (.getSelection js/app (partial js->fn fn)))

(defn set-selection!
  "Replace current document selection with given matrix data."
  [matrix] (.setSelection js/app (clj->js matrix)))

(defn add-binding-named-item
  "Add a named binding to a document region. The name can either refer to an
  existing named item in the document, or a region by using the A1 reference style.
  Examples of A1 references: A1 (single cell at A1), B1:C2 (2x2 at B1 to C2),
  Sheet1!$A$1:$B$2 (2x2 within Sheet1 on $A$1 to $B$2), etc.."
  [name id fn] (.addBindingFromNamedItem js/app name id (partial binding->fn fn)))

(defn add-binding-prompt
  "Add a document binding by prompting user to provide a region."
  [id fn] (.addBindingFromPrompt js/app id (partial binding->fn fn)))

(defn add-binding-selection
  "Add a document binding for the current selection."
  [id fn] (.addBindingFromSelection js/app id (partial binding->fn fn)))

(defn get-all-bindings
  "Call the given function with a vector of all current binding ids."
  [fn] (.getAllBindings js/app (partial js->fn fn)))

(defn get-binding-details
  "Call the given function with the details of given binding id."
  [id fn] (.getBindingDetails js/app id (partial binding->fn fn)))

(defn get-binding-data
  "Pass current binding data to given function."
  [id fn] (.getBindingData js/app id (partial binding-data->fn fn)))

(defn set-binding-data!
  "Replace binding selection with given matrix data."
  [id matrix] (.setBindingData js/app id (clj->js matrix)))

(defn add-binding-data-event
  "Subscribe to data changes on binding id, passing it to given function
  whenever it is changed."
  [id fn] (.addBindingDataEvent js/app id fn))

(defn remove-binding
  "Remove an existing binding. Any data within the binding remains in sheet."
  [id] (.removeBinding js/app id))

(defn remove-binding-data-event
  "Unsubscribe to data changes on binding id."
  [id] (.removeBindingDataEvent js/app id))

(println "cljs4excel ready..")
