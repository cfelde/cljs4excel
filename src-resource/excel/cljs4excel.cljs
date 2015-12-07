; The content of this file is dynamically loaded as if it was typed into
; the REPL directly. The cljs4excel.core namespace is already required at this
; point. When the output of the final println at the bottom is shown in the
; REPL console you can assume that all the content loaded successfully.

(defn js->fn
  "Take a function and JSON, convert JSON to a Clojure structure and pass it to the function."
  [f js]
  (f (js->clj js)))

(defn binding->fn
  "Called from JS for a Excel document binding.
   The given function is given a map with these keys:
   :id User provided id for binding.
   :type Type of binding, currently only matrix.
   :cc Binding column count.
   :rc Binding row count."
   [f id type cc rc]
   (f {:id id
        :type type
        :cc cc
        :rc rc}))

(defn binding-data->fn
  "Similar to binding->fn, but includes binding data keyed under :data."
  [f id type cc rc data]
  (f {:id id
      :type type
      :cc cc
      :rc rc
      :data (js->clj data)}))

(defn get-selection
  "Used to get the current document selection."
  [f]
  (.getSelection js/app (partial js->fn f)))

(defn set-selection!
  "Replace current document selection with given matrix data."
  [matrix]
  (.setSelection js/app (clj->js matrix)))

(defn add-binding-named-item
  "Add a named binding to a document region. The name can either refer to an
  existing named item in the document, or a region by using the A1 reference style.
  Examples of A1 references: A1 (single cell at A1), B1:C2 (2x2 at B1 to C2),
  Sheet1!$A$1:$B$2 (2x2 within Sheet1 on $A$1 to $B$2), etc.."
  [name id f]
  (.addBindingFromNamedItem js/app name id (partial binding->fn f)))

(defn add-binding-prompt
  "Add a document binding by prompting user to provide a region."
  [id f]
  (.addBindingFromPrompt js/app id (partial binding->fn f)))

(defn add-binding-selection
  "Add a document binding for the current selection."
  [id f]
  (.addBindingFromSelection js/app id (partial binding->fn f)))

(defn get-all-bindings
  "Called the given function with a vector of all current binding ids."
  [f]
  (.getAllBindings js/app (partial js->fn f)))

(defn get-binding-details
  "Call the given function with the details of given binding id.
  Multiple ids can be given as long as the final callback function can
  handle as many arguments as ids given. The given ids can either be contained
  within a sequence or not. Examples:
      (get-binding-details id println)
      (get-binding-details id1 id2 println)
      (get-binding-details '(id1 id2) println)
      (get-binding-details '(id1 id2) '(id3 id4) println)"
  [& args]
  (let [ids (-> args butlast flatten)
        f (last args)]
    (if (empty? ids)
      (f)
      (.getBindingDetails js/app (first ids)
        (partial binding->fn
          #(get-binding-details (rest ids) (partial f %)))))))

(defn get-binding-data
  "Pass current binding data to given function.
  Multiple ids can be given as long as the final callback function can
  handle as many arguments as ids given. The given ids can either be contained
  within a sequence or not. Examples:
      (get-binding-data id println)
      (get-binding-data id1 id2 println)
      (get-binding-data '(id1 id2) println)
      (get-binding-data '(id1 id2) '(id3 id4) println)"
  [& args]
  (let [ids (-> args butlast flatten)
        f (last args)]
    (if (empty? ids)
      (f)
      (.getBindingData js/app (first ids)
        (partial binding-data->fn
          #(get-binding-data (rest ids) (partial f %)))))))

(defn set-binding-data!
  "Replace binding selection with given matrix data.
  Multiple pairs of id and matrix may be given."
  [id matrix & rest]
  (.setBindingData js/app id (clj->js matrix))
  (if (= 2 (count (take 2 rest)))
    (set-binding-data! (first rest) (second rest) (drop 2 rest))))

(defn add-binding-data-event
  "Subscribe to data changes on binding id, passing it to given function
  whenever it is changed."
  [id f]
  (.addBindingDataEvent js/app id f))

(defn remove-binding
  "Removes an existing binding. Any data within the binding remains in sheet."
  [id]
  (.removeBinding js/app id))

(defn remove-binding-data-event
  "Unsubscribe to data changes on binding id."
  [id]
  (.removeBindingDataEvent js/app id))

(defn show-sp
  "Show scratchpad editor"
  []
  (.showScratchpad js/app))

(defn hide-sp
  "Hide scratchpad editor"
  []
  (.hideScratchpad js/app))

(defn boot
  "Eval the content on specified reference. The reference is either a named item
  or A1 style reference. If no reference is given the current selection is used."
  ([]
   (letfn [(eval-content
            [content]
            (doall (map (partial cljs4excel.core/eval true true) (flatten content))))]
    (get-selection eval-content)))
  ([named-item]
   (letfn [(eval-content
            [binding-data]
            (remove-binding (:id binding-data))
            (doall (map (partial cljs4excel.core/eval true true) (flatten (:data binding-data)))))
           (binding-callback
            [binding-result]
            (get-binding-data (:id binding-result) eval-content))]
    (add-binding-named-item named-item (str "boot-" (rand-int 1000000)) binding-callback))))

(println "cljs4excel ready..")
