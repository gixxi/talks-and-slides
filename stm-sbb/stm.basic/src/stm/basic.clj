(ns stm.basic)

(defn pound-threads [n fun]
  (let [retries (atom 0)
        threads (for [x (range n)]
                  (Thread. #(fun retries)))
        ]
    (do
      (doall (map #(.start %) threads))
      (doall (map #(.join %) threads))
      (println 
        (format "Done. Retries %d" @retries)
      nil))))

(defn wire-alter [from to retries]
  (dosync
    (try
      (alter from dec)
      (alter to inc)
      (catch Throwable t
        (do
          (swap! retries inc)
          (throw t))))))

(defn wire-commute [from to retries]
  (dosync
    (try
      (commute from dec)
      (commute to inc)
      (catch Throwable t
        (do
          (swap! retries inc)
          (throw t))))))




