(ns fulcro.democards.react16-cards
  (:require [devcards.core :as dc]
            [fulcro.client.dom :as dom]
            [fulcro.client.cards :refer [defcard-fulcro make-root]]
            [fulcro.client.primitives :as prim :refer [defsc defui]]
            [goog.object :as gobj]
            [fulcro.client.mutations :as m :refer [defmutation]]))

(defmutation bump-with-root-refresh [{:keys [id]}]
  (action [{:keys [state]}]
    (swap! state update-in [:counter/by-id id :n] inc))
  (refresh [env]
    [:counters]))

(defsc CounterButton [this {:keys [id n]} {:keys [onClick]}]
  {:query                     [:id :n]
   :initial-state             {:id :param/id :n 1}
   :ident                     [:counter/by-id :id]
   :initLocalState            (fn [] {:m 20})
   :componentDidUpdate        (fn [prev-props prev-state] (js/console.log :did-update :pp prev-props :cp (prim/props this) :ps prev-state :cs (prim/get-state this)))
   :componentDidMount         (fn [] (js/console.log :did-mount (prim/get-ident this) :cp (prim/props this) :cs (prim/get-state this)))
   :componentWillUnmount      (fn [] (js/console.log :will-mount (prim/get-ident this) :cp (prim/props this) :cs (prim/get-state this)))
   :componentWillReceiveProps (fn [next-props] (js/console.log :will-rp :curr-props (prim/props this) :next-props next-props))
   :componentWillUpdate       (fn [next-props next-state] (js/console.log :will-update :cp (prim/props this) :np next-props :cs (prim/get-state this) :crs (prim/get-rendered-state this) :ns next-state))
   :componentWillMount        (fn [] (js/console.log :will-mount :cp (prim/props this) :cs (prim/get-state this)))}
  (dom/div
    (js/console.log :pmeta (meta (prim/props this)))
    (dom/button {:onClick (fn [] (prim/update-state! this update :m inc))} (str "M: " (prim/get-state this :m)))
    (dom/button {:onClick (fn []
                            #_(onClick id)
                            #_(m/set-value! this :n (inc n))
                            (prim/transact! this `[(bump-with-root-refresh {:id ~id})]))} (str "N: " n))))

(def ui-counter (prim/factory CounterButton {:keyfn :id}))

(defsc Root [this {:keys [counters]}]
  {:query          [{:counters (prim/get-query CounterButton)}]
   :initLocalState {:n 22}
   :initial-state  {:counters [{:id 1} {:id 2} {:id 3}]}}
  (let [n       (prim/get-state this :n)
        onClick #(prim/transact! this `[(bump-with-root-refresh {:id ~%})])
        ;;counters (map #(prim/computed % {:onClick onClick}) counters)
        ]
    (dom/div
      (dom/h3 "Counters")
      (dom/p (str "State of root " n))
      (dom/button {:onClick #(prim/set-state! this {:n (inc n)})} "Trigger State Update")
      (dom/ul
        (map ui-counter counters)))))

(defcard-fulcro card
  Root
  {}
  {:inspect-data true})
