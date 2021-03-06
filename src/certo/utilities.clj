(ns certo.utilities
  (:import (java.io PushbackReader))
  (:require
   [clojure.edn :as edn]   
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   [clojure.java.jdbc :as jdbc]
   [clojure.pprint :as pprint]   
   [clojure.string :as str]
   [cemerick.friend :as friend]
   [java-time :as jt]))


(defn authenticated-username [req]
  (-> req friend/current-authentication :identity))


(defn system-config-filename [system-name]
  (let [system-name (str/upper-case system-name)
        system-config-filename
        (or (System/getenv (format "%s_CONFIG" system-name)) "resources/config.clj")]
    (assert (.exists (io/as-file system-config-filename))
            (format "Environment variable %s_HOME is not set and resources/config.clj does not exist" system-name))
    system-config-filename))


(defn config [system-name]
  (let [system-config-filename (system-config-filename system-name)]
    (merge {:system-name system-name}
           {:system-config-filename system-config-filename}
           (edn/read-string (slurp system-config-filename)))))


(defn date-now []
  (jt/local-date))


(defn parse-integer
  ([x label]
   (parse-integer x label identity))
  ([x label predicate]
   (try
     ;; return nil if x is "" or nil
     (if (str/blank? x)
       nil
       (let [x (Long/parseLong x)]
         (if (predicate x)
           x
           (throw (NumberFormatException.)))))
     (catch NumberFormatException e (throw (Exception. (format "%s field is invalid" label)))))))


(defn read-csv-to-hash-map [filename separator]
  (with-open [in-file (io/reader filename)]
    (let [csv (doall (csv/read-csv in-file :separator separator))
          header (map (fn [x] (keyword (str/lower-case (str/replace x #"\s+" "-")))) (first (take 1 csv)))
          records (map #(zipmap header %) (drop 1 csv))]
      records)))


(defn hash-maps-to-db
  ([db filename f] (hash-maps-to-db db filename f nil nil))
  ([db filename f k v]
   (with-open [r (clojure.java.io/reader filename)]
     (doseq [line (line-seq r)]
       (when (and (not= (str/trim line) "") (not (str/starts-with? (str/triml line) ";")))
         (try
           (if (nil? k)
             (f db (clojure.edn/read-string line))
             (f db (assoc (clojure.edn/read-string line) k v)))
           (catch Exception e
             (println "Exception in hash-maps-to-db processing:")
             (pprint/pprint line)
             (throw e))))))))


;; Used in one time use function db-to-hash-maps
(defn db-to-hash-map [db schema table ordered-fields filename order-by]
  "Queries a database table and writes out a hash-map corresponding to
  each row in the table, and with the order of the field-value pairs
  of hash-map given by the order-fields argument."
  (let [select-statement (format "select * from %s" (str schema "." table))
        rows (jdbc/query db [(if order-by
                               (str select-statement " order by " order-by)
                               select-statement)])]
    (with-open [out (clojure.java.io/writer filename)]
      (doseq [row
              (map
               (fn [row]
                 (str
                  "{"
                  (str/join
                   " "
                   (map
                    (fn [ordered-field]
                      (let [ordered-val (get row ordered-field)
                            ordered-val
                            ;; drop reader literals
                            (if (or (instance? java.sql.Date ordered-val)
                                    (instance? java.sql.Timestamp ordered-val))
                              (str ordered-val)
                              ordered-val)]
                        (clojure.pprint/cl-format nil "~s ~s" ordered-field
                         (cond (= ordered-val "false") false
                               (= ordered-val "true") true
                               :else ordered-val))))
                    ordered-fields))
                  "}\n"))
               rows)]
        (.write out row)
        (.write out "\n")))))


;; (update-sys-fields "/home/djneu/projects/certo/resources/db/sys-fields.clj" "/tmp/sys-fields.clj")
;; (update-sys-fields "/home/djneu/projects/aether/resources/db/app-sys-fields.clj" "/tmp/app-sys-fields.clj")
;; (defn update-sys-fields [in-file out-file]
;;   (let [ks [:fields_id :type :is_id :is_id_in_new :label :control :location :in_table_view :disabled :readonly :required :text_max_length :boolean_true :boolean_false :date_min :date_max :foreign_key_query :foreign_key_size :integer_step :integer_min :integer_max :float_step :float_min :float_max :select_multiple :select_size :ot_schema_table :created_by :updated_by]
;;         sf (read-string (slurp in-file))
;;         updated-sf
;;         (map (fn [x]
;;                (-> x
;;                    (assoc
;;                     :fields_id
;;                     (str (:schema_name x) "." (:table_name x) "." (:field_name x)))
;;                    (dissoc :schema_name :table_name :field_name)))
;;              sf)
;;         updated-sf (map (fn [x] (interleave ks ((apply juxt ks) x))) updated-sf)]
;;     (doseq [x updated-sf]
;;       (spit out-file (str (pr-str x) "\n\n") :append true))))


;; (pads "The Clojure language" 10 "nbsp;" true) =>  "The Clo..."
;; (pads "The Clojure language" 10 "nbsp;" false) => "The Clojur"
;; (pads "The Clojure language" 25 "nbsp;" true) =>  "The Clojure languagenbsp;nbsp;nbsp;nbsp;nbsp;"
(defn pads [s k p ellipsis?]
  "Pad a string s with string p (optionally adding ellipsis) so that
  the length of the result is k."
  (let [k (max 3 k)
        cs (count s)
        s (str s p p)]
    (cond (> k cs)
          (str s (str/join (repeat (- k cs) p)) p)
          (< k cs)
          (if ellipsis?
            (str (subs s 0 (- k 3)) "..." p p)
            (str (subs s 0 (dec k)) p p))
          :else
          (str s p))))


(defn write-hash-maps
  ([rows filename]
   (write-hash-maps rows filename nil))
  ([rows filename keys]
   (let [f (if keys #(interleave keys ((apply juxt keys) %)) identity)]
     (with-open [out (clojure.java.io/writer filename)]
       (doseq [row rows
               :let [row (f row)]]
         (.write out "{")
         (loop [row row]
           (when row
             (.write out (pr-str (first row)))
             (when (next row)
               (.write out " ")
               (recur (next row)))))
         (.write out "}\n\n"))))))


(defn str-to-key-map [m]
  (into {} (map (fn [[k v]] (vector (keyword k) v)) m)))


(defn map-nil-vals-to-empty-str [m]
  (reduce-kv
   (fn [m k v] (assoc m k (if (= v nil) "" v)))
   {}
   m))


(defn map-vals-to-str [m]
  (reduce-kv
   (fn [m k v] (assoc m k (str v)))
   {}
   m))


(defn read-forms [file]
  (let [r (PushbackReader. (io/reader (io/file file)))]
    (loop [forms []
           number-forms 0]
      (let [form
            (try
              (read {:eof :eof} r)
              (catch Exception e
                (println (str "Exception reading " file " at form " number-forms))
                (throw e)))]
        (if (not= form :eof)
          (recur (conj forms form) (inc number-forms))
          forms)))))


(defn insert-sys-event-class-dnfs [db params]
  (jdbc/insert!
   db
   (:sys-event-class-dnfs-schema-table params)
   (assoc (dissoc params :sys-event-class-dnfs-schema-table) :created_by "root" :updated_by "root")))


(defn dnf-helper
  ([index event-classes-id depends-on-event-classes-id]
   (dnf-helper index event-classes-id depends-on-event-classes-id true {}))

  ([index event-classes-id depends-on-event-classes-id is-positive]
   (dnf-helper index event-classes-id depends-on-event-classes-id is-positive {}))

  ([index event-classes-id depends-on-event-classes-id is-positive optional]
   (merge {:event_classes_id event-classes-id :term index :depends_on_event_classes_id depends-on-event-classes-id :is_positive is-positive} optional)))


(defn dnf [[event-classes-id & terms]]
  (flatten
   (map-indexed
    (fn [index term]
      (map
       #(apply dnf-helper (concat (list index event-classes-id) %))
       term))
    terms)))


(defn dnf-action-sequence-helper
  ([event-classes-id-format action depends-on-action]
   (dnf-action-sequence-helper event-classes-id-format action depends-on-action true {}))

  ([event-classes-id-format action depends-on-action is-positive]
   (dnf-action-sequence-helper event-classes-id-format action depends-on-action is-positive {}))

  ([event-classes-id-format action depends-on-action is-positive optional]
   (merge
    {:event_classes_id
     (format event-classes-id-format action)
     :term 1
     :depends_on_event_classes_id (format event-classes-id-format depends-on-action)
     :is_positive is-positive}
    optional)))


(defn dnfs-by-action-sequence [[event-classes-id-format action-maps]]
  (flatten
   (map
    #(apply dnf-action-sequence-helper (concat (list event-classes-id-format) %))
    action-maps)))


(defn dnfs-to-db [db sys-event-class-dnfs-schema-table file]
  (let [display? false]
    (doseq [form (read-forms file)]
      (try
        (doseq [dnf
                (if (every? (fn [x] (and (string? (first x)) (string? (second x)))) (second form))
                  (do
                    (when display? (println "ACTION SEQUENCE"))
                    (dnfs-by-action-sequence form))
                  (dnf form))]
          (when display? (pprint/pprint dnf))
          (insert-sys-event-class-dnfs db (assoc dnf :sys-event-class-dnfs-schema-table sys-event-class-dnfs-schema-table)))
        (catch Exception e
          (println "form:")
          (pprint/pprint form)
          (throw e)))
      (when display? (println)))))


(defn base-url [request]
  (str
   (cond (= (:scheme request) :http) "http://"
         (= (:scheme request) :https) "https://"
         :else
         (throw (Exception. (format "Invalid scheme: %s" (:scheme request)))))
   (:server-name request)
   (:uri request)))


(defn update-url-query-parameters [request update-fn]
  (str
   (base-url request)
   "?"
   (str/join
    "&"
    (map
     (fn [[k v]] (str k "=" (ring.util.codec/url-encode v)))
     (update-fn request)))))


(defn clean-get-url? [request]
  (and (= (:request-method request) :get)
       (or
        (empty? (:query-params request))
        (not (some (fn [[k v]] (empty? v)) (:query-params request))))))


(defn clean-query-parameters [request]
  (into {} (filter (fn [[k v]] (not (empty? v))) (:query-params request))))


(defn clean-get-url [request]
  (update-url-query-parameters request clean-query-parameters))

