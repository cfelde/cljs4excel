; The below code shows how you can access JSONP web servcies using cljs4excel.
; Here we load stock market data from Yahoo Finance and display that in
; the sheet binding. In addition we can have it automatically reload
; this data at some user defined interval.

; Example session:
; cljs.user=> (boot "S1")
; cljs.user=> (init)
; nil
; {:rc 100, :cc 11, :type matrix, :id output-id}
; Remote connection available

; cljs.user=> (add-ticker "GOOG")
; cljs.user=> (add-ticker "AAPL")
; cljs.user=> (add-ticker "MSFT")
; cljs.user=> (start-auto-fetch 3)
; cljs.user=> (stop-auto-fetch)
; cljs.user=> (remove-ticker "GOOG")
; cljs.user=> (remove-ticker "MSFT")

; Binding area for output
(def output-id "output-id")
(def output-range "A2:K101")

; Atoms for ticker and refresh state
(def tickers (atom #{}))
(def interval-id (atom nil))

; Utilities and setup
(defn add-script
  "Add a script element to DOM with given id and src url."
  [id url]
  (let [script-tag (.createElement js/document "script")
        id-attr (.createAttribute js/document "id")
        src-attr (.createAttribute js/document "src")]
     (set! (.-value id-attr) id)
     (set! (.-value src-attr) url)
     (.setAttributeNode script-tag id-attr)
     (.setAttributeNode script-tag src-attr)
     (.appendChild (.querySelector js/document "body") script-tag)))

(defn remove-element
  "Remove element by id from DOM."
  [id]
  (if-let [el (.getElementById js/document id)]
     (.removeChild (.-parentNode el) el)))

(defn test-connection-response
  "Callback from test-connection request."
  [_]
  (remove-element "test-connection")
  (println "Remote connection available"))

(defn test-connection
  "Test call to check we're not blocked by browser."
  []
  (let [url "https://finance.yahoo.com/webservice/v1/symbols/AAPL/quote?format=json&view=detail&callback=cljs.user.test_connection_response"]
    (try
      (do
        (add-script "test-connection" url)
        true)
      (catch :default e
        (do
          (println "Please unblock content from finance.yahoo.com")
          false)))))

(defn init-binding
  "Set up binding to sheet for ticker output data."
  []
  (add-binding-named-item output-range output-id println))

(defn init
  "Init binding after testing JSONP service."
  []
  (if (test-connection)
    (init-binding)))

; Functions for loading, showing and manipulating tickers and quotes
(defn quote-cb
  "Callback function for JSONP request."
  [js-value]
  (letfn [(field-extract
            [f]
            (vec [(:name f) (:symbol f) (:utctime f) (:price f) (:change f) (:chg-pct f) (:volume f) (:day-low f) (:day-high f) (:year-low f) (:year-high f)]))
          (show-quotes
            [q]
            (fill-binding-data! output-id (vec (map field-extract (sort #(compare (:name %1) (:name %2)) q)))))
          (parse-fields
            [f]
            {:name (get f "name")
             :symbol (get f "symbol")
             :ts (get f "ts")
             :utctime (get f "utctime")
             :price (js/parseFloat (get f "price"))
             :change (js/parseFloat (get f "change"))
             :chg-pct (js/parseFloat (get f "chg_percent"))
             :volume (js/parseFloat (get f "volume"))
             :day-high (js/parseFloat (get f "day_high"))
             :day-low (js/parseFloat (get f "day_low"))
             :year-high (js/parseFloat (get f "year_high"))
             :year-low (js/parseFloat (get f "year_low"))})
          (get-fields
             [v]
             (-> v (get "resource") (get "fields")))
          (get-resources
             [d]
             (get-in d ["list" "resources"]))]
    (->> (js->clj js-value)
      get-resources
      (map get-fields)
      (map parse-fields)
      show-quotes)))

(defn load-quote
  "Load quote and ticker stats for given tickers, using JSONP service."
  [& tickers]
  (if (not (empty? tickers))
    (let [id "quote-loader"
          ticker-list (apply str (butlast (flatten (map concat tickers (repeat ",")))))
          url (str "https://finance.yahoo.com/webservice/v1/symbols/" ticker-list "/quote?format=json&view=detail&callback=cljs.user.quote_cb")]
       (println (str "Loading " ticker-list))
       (remove-element id)
       (add-script id url))
    (fill-binding-data! output-id '())))

(defn add-ticker
  "Add a ticker to list of tickers."
  [ticker]
  (if (not (empty? ticker))
    (do
      (swap! tickers conj ticker)
      (apply load-quote @tickers))))

(defn remove-ticker
  "Remove a ticker from list of tickers."
  [ticker]
  (swap! tickers disj ticker)
  (apply load-quote @tickers))

(defn start-auto-fetch
  "Automatically fetch all requested tickers every X number of seconds."
  [delay]
  (letfn [(start
            [id]
            (if (nil? id)
              (.setInterval js/window #(apply load-quote @tickers) (* delay 1000))
              id))]
    (if (not= @interval-id (swap! interval-id start))
      (apply load-quote @tickers))))

(defn stop-auto-fetch
  "Stop any automatic ticker fetching."
  []
  (letfn [(stop
            [id]
            (if (not (nil? id))
              (.clearInterval js/window id))
            nil)]
    (swap! interval-id stop)))
