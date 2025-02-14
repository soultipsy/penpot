;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.ui.workspace.shapes.frame.node-store
  (:require
   [app.util.dom :as dom]
   [app.util.globals :as globals]
   [rumext.alpha :as mf]))

(defn use-node-store
  "Hook responsible of storing the rendered DOM node in memory while not being used"
  [thumbnail? node-ref rendered?]

  (let [;; when `true` the node is in memory
        in-memory? (mf/use-var true)

        ;; State just for re-rendering
        re-render  (mf/use-state 0)

        parent-ref (mf/use-var nil)

        on-frame-load
        (mf/use-callback
         (fn [node]
           (when (and (some? node) (nil? @node-ref))
             (let [content (-> (.createElementNS globals/document "http://www.w3.org/2000/svg" "g")
                               (dom/add-class! "frame-content"))]
               ;;(.appendChild node content)
               (reset! node-ref content)
               (reset! parent-ref node)
               (swap! re-render inc)))))]

    (mf/use-effect
     (mf/deps thumbnail?)
     (fn []
       (when (and (some? @parent-ref) (some? @node-ref) @rendered? thumbnail?)
         (.removeChild @parent-ref @node-ref)
         (reset! in-memory? true))

       (when (and (some? @node-ref) @in-memory? (not thumbnail?))
         (.appendChild @parent-ref @node-ref)
         (reset! in-memory? false))))

    on-frame-load))
