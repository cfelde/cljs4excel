; This is an implementation of Conway's Game of Life.
; See https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life

; Example session
; cljs.user=> (boot "FF1")
; nil
; #'cljs.user/canvas-id
; #'cljs.user/canvas-range
; #'cljs.user/init-binding
; #'cljs.user/init-random
; #'cljs.user/init-blank
; #'cljs.user/value-at
; #'cljs.user/neighbour-coordinates
; #'cljs.user/neighbour-values
; #'cljs.user/apply-rule
; #'cljs.user/update-conway!
; #'cljs.user/stop-conway
; #'cljs.user/run-conway
; cljs.user=> (init-binding)
; nil
; {:rc 100, :cc 150, :type matrix, :id canvas-id}
; cljs.user=> (init-random)
; nil
; cljs.user=> (run-conway)
; nil

; You can clear the canvas with (init-blank) and then manually fill the canvas
; with 1 and 0 values, with a 1 indicating live cells.

; Binding id and size
(def canvas-id "canvas-id")
(def canvas-range "A1:ET100")

; Init functions for binding, and content
(defn init-binding
  "Set up binding to canvas."
  []
  (add-binding-named-item canvas-range canvas-id println))

(defn init-random
  "Fill canvas with random alive/dead cells.
  A cell with a value of 1 is considered to be alive, with 0 for dead."
  []
  (letfn [(fill-canvas
            [{h :rc w :cc}]
            (fill-binding-data! canvas-id (repeatedly (* h w) #(if (< (.random js/Math) 0.5) 1 0))))]
    (get-binding-details canvas-id fill-canvas)))

(defn init-blank
  "Fill canvas with only dead cells, i.e. all cell have a value of zero."
  []
  (letfn [(fill-canvas
            [{h :rc w :cc}]
            (fill-binding-data! canvas-id (repeat (* h w) 0)))]
    (get-binding-details canvas-id fill-canvas)))

; Helper functions for getting info about cells and cell neighbours
(defn value-at
  "Get cell value at x/y for d."
  [d x y]
  (nth (nth d y) x))

(defn neighbour-coordinates
  "Given the w/h dimentions of the canvas, get all neighbour coordinates for
  the cell at the given x/y location. The canvas will wrap around if needed."
  [w h x y]
  (list [(mod (dec x) w) (mod (dec y) h)]
        [(mod x w) (mod (dec y) h)]
        [(mod (inc x) w) (mod (dec y) h)]
        [(mod (dec x) w) y]
        [(mod (inc x) w) y]
        [(mod (dec x) w) (mod (inc y) h)]
        [(mod x w) (mod (inc y) h)]
        [(mod (inc x) w) (mod (inc y) h)]))

(defn neighbour-values
  "Return a sequence of cell values for neighbours of the cell at x/y."
  [d w h x y]
  (let [coordinates (neighbour-coordinates w h x y)]
    (map #(value-at d (first %) (second %)) coordinates)))

; Conway's Game of Life logic
(defn apply-rule
  "Given a current cell value, and the sum of neighbour cell values, apply
  game rules and return the new cell value."
  [[current neighbour-sum]]
  (if (= current 1)
    (if (or (= neighbour-sum 2) (= neighbour-sum 3)) 1 0)
    (if (= neighbour-sum 3) 1 0)))

(defn update-conway!
  "Take the current canvas and replace the canvas content with next iteration."
  []
  (letfn [(step
            [{h :rc w :cc d :data}]
            (let [c (for [y (range h) x (range w)] [x y])
                  v (map #(vec [(value-at d (first %) (second %)) (reduce + (neighbour-values d w h (first %) (second %)))]) c)]
              (fill-binding-data! canvas-id (map apply-rule v))))]
    (get-binding-data canvas-id step)))

; Tools to start/stop the game
(defn stop-conway
  "Stop any further iterations from proceeding."
  []
  (remove-binding-data-event canvas-id)
  (js* "$('#stop-div').remove()"))

(defn run-conway
  "Start the game."
  []
  (remove-binding-data-event canvas-id)
  (add-binding-data-event canvas-id #(update-conway!))
  (js* "$(document.body).append(\"<div id='stop-div' style='position:absolute; top:0; left:0; width:100%; height:100%; z-index:1000; opacity: 0.5; background-color: white'><button style='margin: auto; display: block; margin-top: 50%' onclick='cljs.user.stop_conway()'>Stop</button></div>\")")
  (update-conway!))
