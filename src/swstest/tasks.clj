(ns swstest.tasks
  (:import [com.amazonaws.services.simpleworkflow.model RespondActivityTaskCompletedRequest RespondActivityTaskFailedRequest]))

(defn sendmail [client task]
  (let [req1 (doto (RespondActivityTaskCompletedRequest.) (.withTaskToken (.getTaskToken task)) (.withResult "0"))
        req2 (doto (RespondActivityTaskFailedRequest.) (.withTaskToken (.getTaskToken task)) (.withReason "1") (.withDetails "Det vare bare uheldigt"))
        r (rand-int 99)]
    (prn (str "RAND " r))
    (if (even? r) 
      (do
        (prn (str "Mail sendt " (.getInput task)))
        (. client (respondActivityTaskCompleted req1)))
      (. client (respondActivityTaskFailed req2)))))

(defn sendsms [client task]
  (let [_ (prn (str "SMS sendt " (.getInput task)))
        req (doto (RespondActivityTaskCompletedRequest.) (.withTaskToken (.getTaskToken task)) (.withResult "0"))]
    (. client (respondActivityTaskCompleted req))))

(defn check-mail-delivery [client task]
  (prn "OK"))

(defn store-message [client task]
  (prn "STORED"))

(defn notyfy-spoc [client task]
  (prn "SPOC Notified"))