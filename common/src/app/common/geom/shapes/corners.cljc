;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.common.geom.shapes.corners)

(defn fix-radius
  ;; https://www.w3.org/TR/css-backgrounds-3/#corner-overlap
  ;;
  ;; > Corner curves must not overlap: When the sum of any two adjacent border radii exceeds the size of the border box,
  ;; > UAs must proportionally reduce the used values of all border radii until none of them overlap.
  ;;
  ;; > The algorithm for reducing radii is as follows: Let f = min(Li/Si), where i ∈ {top, right, bottom, left}, Si is
  ;; > the sum of the two corresponding radii of the corners on side i, and Ltop = Lbottom = the width of the box, and
  ;; > Lleft = Lright = the height of the box. If f < 1, then all corner radii are reduced by multiplying them by f.
  ([width height r]
   (let [f (min (/ width  (* 2 r))
                (/ height (* 2 r)))]
     (if (< f 1)
       (* r f)
       r)))

  ([width height r1 r2 r3 r4]
   (let [f (min (/ width  (+ r1 r2))
                (/ height (+ r2 r3))
                (/ width  (+ r3 r4))
                (/ height (+ r4 r1)))]
     (if (< f 1)
       [(* r1 f) (* r2 f) (* r3 f) (* r4 f)]
       [r1 r2 r3 r4]))))

(defn shape-corners-1
  "Retrieve the effective value for the corner given a single value for corner."
  [{:keys [width height rx] :as shape}]
  (if (some? rx)
    (fix-radius width height rx)
    0))

(defn shape-corners-4
  "Retrieve the effective value for the corner given four values for the corners."
  [{:keys [width height r1 r2 r3 r4]}]
  (if (and (some? r1) (some? r2) (some? r3) (some? r4))
    (fix-radius width height r1 r2 r3 r4)
    [r1 r2 r3 r4]))
