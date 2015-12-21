; This is an implementation of the k-means algorithm used within the
; cljs4excel system. It is meant both as an example of the k-means algorithm
; and as an example of how to use the various functions available within
; cljs4excel. This includes reading and updating Excel document content, but
; also how you can play on the interaction between Excel and ClojureScript
; using add-binding-data-event.

; Example sessions:
; cljs.user=> (init-headers)
; nil
; cljs.user=> (init-random-input -10 -10 0.1 100 0 0 0.2 100 10 10 0.3 100)
; nil
; cljs.user=> (init-centroids 3)
; nil
; cljs.user=> (run-centroids)
; nil
; Centroids converged
; cljs.user=> (reset-values)
; nil

; Binding ids
(def header-id "header-id")
(def input-id "input-id")
(def output-id "output-id")

; Math and utility functions
(defn rg
  "Return a normally distributed random number with given mean and stddev."
  [mean stddev]
  (loop []
    (let [v1 (- (* (.random js/Math) 2) 1)
          v2 (- (* (.random js/Math) 2) 1)
          s (+ (* v1 v1) (* v2 v2))]
      (if (not (or (>= s 1) (= s 0)))
        (let [m (/ (* -2 (.log js/Math s)) s)]
          (+ mean (* v1 m stddev)))
        (recur)))))

(defn get-random-input
  "Return count number of random points with given mean and stddev.
  Further sets of input parameters can be given."
  [mean-x mean-y stddev count & rest]
  (letfn [(xy [] (vec [(rg mean-x stddev) (rg mean-y stddev)]))]
    (let [output (into [] (repeatedly count xy))]
      (if (empty? rest)
        output
        (reduce conj output (apply get-random-input rest))))))

(defn pow
  "Returns the value of x raised to the power of y."
  [x y]
  (.pow js/Math x y))

(defn sqrt
  "Return the square of x."
  [x]
  (.sqrt js/Math x))

(defn mean
  "Return the mean of a sequence or zero if empty."
  [coll]
  (let [sum (apply + coll)
        count (count coll)]
    (if (pos? count)
      (double (/ sum count))
      0)))

(defn get-distance
  "Get the Euclidean distance between two points."
  [[x1 y1] [x2 y2]]
  (sqrt (+ (pow (- x2 x1) 2) (pow (- y2 y1) 2))))

(defn get-closest-centroid-index
  "Get the row index of the centroid closest to point."
  [index-centroids point]
  (ffirst (sort-by #(get-distance point (second %)) index-centroids)))

; Utility functions for setting up sheet data
(defn init-headers
  "Insert header text."
  []
  (letfn [(set-headers!
            [b]
            (set-binding-data! (:id b) [["Input X", "Input Y", "Centroid X", "Centroid Y"]]))]
    (add-binding-named-item "A1:D1" header-id set-headers!)))

(defn init-random-input
  "A wrapper around get-random-input, setting up the binding and filling it."
  [& args]
  (let [output (apply get-random-input args)
        row-count (count output)
        range (str "A2:B" (+ row-count 1))]
    (letfn [(set-data!
              [b]
              (set-binding-data! (:id b) output))]
      (add-binding-named-item range input-id set-data!))))

(defn init-centroids
  "Init c number of centroids centered around the mean of all input points."
  [c]
  (letfn [(init-from-input
            [{input :data}]
            (let [xs (map first input)
                  ys (map second input)
                  mean-x (mean xs)
                  mean-y (mean ys)
                  cxs (repeatedly c #(rg mean-x 0.1))
                  cys (repeatedly c #(rg mean-y 0.1))
                  cxy (vec (map #(vec [%1 %2]) cxs cys))]
              (set-binding-data! output-id cxy)))]
    (let [range (str "C2:D" (inc c))]
      (add-binding-named-item range output-id #(get-binding-data input-id init-from-input)))))

(defn reset-values
  "Remove all input and centroid values."
  []
  (fill-binding-data! output-id [])
  (fill-binding-data! input-id []))

; Functions for running the k-means clustering algorithm
(defn update-centroids
  "Take a sequence of points and a sequence of centroids,
  and return an updated set of centroids."
  [points centroids]
        ; index-centroids: Store centroid coordinates keyed by row index
  (let [index-centroids (into (sorted-map) (map-indexed vector centroids))
        ; index-points: Sequence of point coordinates prefixed by closest centroid index
        index-points (map #(vec [(get-closest-centroid-index index-centroids %) %]) points)
        ; index-groups: Map keyed by centroid index with all points having that centroid as its closest
        index-groups (into (sorted-map) (map #(vec [(first %) (map second (second %))]) (group-by first index-points)))
        ; index-x: Same as index-groups, but with only x coordinates
        index-x (into (sorted-map) (map #(vec [(first %) (map first (second %))]) index-groups))
        ; index-y: Same as index-groups, but with only y coordinates
        index-y (into (sorted-map) (map #(vec [(first %) (map second (second %))]) index-groups))
        ; index-x-mean: Mean value of all point x coordinates, keyed by centroid index
        index-x-mean (into (sorted-map) (map #(vec [(first %) (-> % second mean)]) index-x))
        ; index-y-mean: Mean value of all point y coordinates, keyed by centroid index
        index-y-mean (into (sorted-map) (map #(vec [(first %) (-> % second mean)]) index-y))]
    ; Return the same format as centroids, but with updated coordinates based on points belonging to each centroid
    (vec (map #(vec [(get index-x-mean % (first (get index-centroids %))) (get index-y-mean % (second (get index-centroids %)))]) (keys index-centroids)))))

(defn step-centroids
  "Run one update of centroids based on input points."
  []
  (letfn [(get-data-2
                [input {centroids :data}]
                (set-binding-data! output-id (update-centroids input centroids)))
          (get-data-1
                [{input :data}]
                (get-binding-data output-id (partial get-data-2 input)))]
      (get-binding-data input-id get-data-1)))

(defn run-centroids
  "Run above step-centroids function multiple times till the centroids have
  converged and stop changing their positions."
  []
  (remove-binding-data-event output-id)
  (let [last-value (atom [])]
    (letfn [(check-update
              [{current-value :data}]
              (if (= @last-value current-value)
                (do
                  (println "Centroids converged")
                  (remove-binding-data-event output-id))
                (do
                  (reset! last-value current-value)
                  (step-centroids))))]
      (add-binding-data-event output-id #(get-binding-data output-id check-update))
      (step-centroids))))
