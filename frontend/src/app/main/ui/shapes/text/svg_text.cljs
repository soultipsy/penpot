;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.ui.shapes.text.svg-text
  (:require
   [app.common.data :as d]
   [app.common.data.macros :as dm]
   [app.common.geom.shapes :as gsh]
   [app.config :as cfg]
   [app.main.ui.context :as muc]
   [app.main.ui.shapes.attrs :as attrs]
   [app.main.ui.shapes.custom-stroke :refer [shape-custom-strokes]]
   [app.main.ui.shapes.gradients :as grad]
   [app.util.object :as obj]
   [rumext.alpha :as mf]))

(def fill-attrs [:fill-color :fill-color-gradient :fill-opacity])

(defn set-white-fill
  [shape]
  (let [update-color
        (fn [data]
          (-> data
              (dissoc :fill-color :fill-opacity :fill-color-gradient)
              (assoc :fills [{:fill-color "#FFFFFF" :fill-opacity 1}])))]
    (-> shape
        (d/update-when :position-data #(mapv update-color %))
        (assoc :stroke-color "#FFFFFF" :stroke-opacity 1))))

(mf/defc text-shape
  {::mf/wrap-props false
   ::mf/wrap [mf/memo]}
  [props]

  (let [render-id (mf/use-ctx muc/render-ctx)
        shape (obj/get props "shape")
        shape (cond-> shape (:is-mask? shape) set-white-fill)

        {:keys [x y width height position-data]} shape

        transform (str (gsh/transform-matrix shape))

        ;; These position attributes are not really necesary but they are convenient for for the export
        group-props (-> #js {:transform transform
                             :x x
                             :y y
                             :width width
                             :height height}
                        (attrs/add-style-attrs shape render-id))
        get-gradient-id
        (fn [index]
          (str render-id "_" (:id shape) "_" index))]
    [:*
     ;; Definition of gradients for partial elements
     (when (d/seek :fill-color-gradient position-data)
       [:defs
        (for [[index data] (d/enumerate position-data)]
          (when (some? (:fill-color-gradient data))
            [:& grad/gradient {:id (str "fill-color-gradient_" (get-gradient-id index))
                               :key index
                               :attr :fill-color-gradient
                               :shape data}]))])

     [:> :g group-props
      (for [[index data] (d/enumerate position-data)]
        (let [y (if (cfg/check-browser? :safari)
                  (- (:y data) (:height data))
                  (:y data))

              alignment-bl (when (cfg/check-browser? :safari) "text-before-edge")
              dominant-bl (when-not (cfg/check-browser? :safari) "ideographic")
              props (-> #js {:key (dm/str "text-" (:id shape) "-" index)
                             :x (:x data)
                             :y y
                             :alignmentBaseline alignment-bl
                             :dominantBaseline dominant-bl
                             :style (-> #js {:fontFamily (:font-family data)
                                             :fontSize (:font-size data)
                                             :fontWeight (:font-weight data)
                                             :textTransform (:text-transform data)
                                             :textDecoration (:text-decoration data)
                                             :fontStyle (:font-style data)
                                             :direction (if (:rtl data) "rtl" "ltr")
                                             :whiteSpace "pre"}
                                        (obj/set! "fill" (str "url(#fill-" index "-" render-id ")")))})
              shape (assoc shape :fills (:fills data))]

          [:& shape-custom-strokes {:shape shape :key index}
           [:> :text props (:text data)]]))]]))
