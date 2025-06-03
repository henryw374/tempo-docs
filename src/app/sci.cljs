(ns app.sci
  (:refer-clojure :exclude [time])
  (:require
   [sci.core :as sci]
   [app.error :as error]
   [com.widdindustries.tempo]))

(clojure.core/defmacro ^:private time
  "Evaluates expr and prints the time it took. Returns the value of expr."
  [expr]
  `(let [start# (cljs.core/system-time)
         ret# ~expr]
     (prn (cljs.core/str "Elapsed time: "
                         (.toFixed (- (system-time) start#) 6)
                         " msecs"))
     ret#))

(def ^:private clj-ns (sci/create-ns 'clojure.core nil))



(def ^:private namespaces
  (let [ens (sci/create-ns 'com.widdindustries.tempo)
        publics (ns-publics 'com.widdindustries.tempo)
        sci-ns (update-vals publics #(sci/copy-var* % ens))
        ]
    {'com.widdindustries.tempo sci-ns
     'clojure.core
     {'time        (sci/copy-var time clj-ns)
      'system-time (sci/copy-var system-time clj-ns)}}))

;; Default sci options
(defonce init-opts {:classes    {'js js/window :allow :all}
                    :namespaces namespaces})

;; Sci context inside an atom
(defonce ^:private context (atom (sci/init init-opts)))

(defn set-print-fn
  "Setup a custom `print-fn` for sci."
  [f]
  (sci/alter-var-root sci/print-fn (constantly f)))

(defn extend-ctx
  "Extend default sci context merging `opts`."
  [opts]
  (reset! context (sci/merge-opts @context opts)))

(defn eval-string
  "Evaluate `source` using sci and return the output
  or raise throw an exception in case of error."
  [source]
  (try (sci/eval-string* @context source)
       (catch :default e
         (error/error-handler e (:src context))
         (let [sci-error? (isa? (:type (ex-data e)) :sci/error)]
           (throw (if sci-error?
                    (or (ex-cause e) e)
                    e))))))
