(ns cljs4excel.core
  (:require [replumb.core :as replumb]
            [cljs4excel.console :as console]))

(def *console*)

(defn fake-load-fn! [_ cb])

(defn handle-result!
  [console result]
  (let [write-fn (if (replumb/success? result) console/write-return! console/write-exception!)]
    (write-fn console (replumb/unwrap-result result))))

(defn cljs-read-eval-print!
  [console user-input]
  (replumb/read-eval-call {:load-fn! fake-load-fn!} (partial handle-result! console) user-input))

(defn cljs-console-prompt!
  [console]
  (doto console
    (.Prompt true (fn [input]
                    (cljs-read-eval-print! console input)
                    (.SetPromptLabel console (replumb/get-prompt)) ;; necessary for namespace changes
                    (cljs-console-prompt! console)))))

(defn expression-accumulator
  [{expressions :expressions open-count :open-count buffer :buffer} c]
  (let [output (cond
                  (= \( c) {:expressions expressions :open-count (inc open-count) :buffer (str buffer c)}
                  (= \) c) {:expressions expressions :open-count (dec open-count) :buffer (str buffer c)}
                  (= 0 open-count) {:expressions expressions :open-count open-count :buffer buffer}
                  :else {:expressions expressions :open-count open-count :buffer (str buffer c)})]
      (if (and (= 1 open-count) (= 0 (:open-count output)))
        (assoc output :expressions (conj (:expressions output) (:buffer output)) :buffer "")
        output)))

(defn split-expressions
  [s]
  (:expressions (reduce expression-accumulator {:expressions [] :open-count 0 :buffer ""} s)))

(defn eval
  ([s]
   (eval true true s))
  ([print-success print-error s]
   (letfn [(eval
            [expressions result]
            (if (not (nil? result))
              (if (and print-success (:success? result))
                (handle-result! *console* result)
                (if (and print-error (not (:success? result)))
                  (handle-result! *console* result))))
            (if (and
                  (or (nil? result) (:success? result))
                  (not (empty? expressions)))
              (replumb/read-eval-call {:load-fn! fake-load-fn!} (partial eval (rest expressions)) (first expressions))))

           (handler
            [result]
            (if (and print-error (not (:success? result)))
              (handle-result! *console* result))
            (if (:success? result)
              (eval (split-expressions (->> (:value result) rest butlast (apply str))) nil)))]

    (replumb/read-eval-call {:load-fn! fake-load-fn!} handler (str "'(" s ")")))))

(defn init
  []
  (let [console (console/new-jqconsole "#console" {:welcome-string nil
                                                   :prompt-label (replumb/get-prompt)
                                                   :continue-label nil
                                                   :disable-auto-focus false})]
      (cljs-console-prompt! console)
      (set! *console* console)
      (set! *print-newline* false)
      (set! *print-fn*
        (fn [& args]
          (console/write-output! console (into-array args)))))

  (println "Loading cljs4excel..")
  (.loadScript js/app "excel/cljs4excel.cljs"))
