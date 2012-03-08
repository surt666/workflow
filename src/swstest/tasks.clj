(ns swstest.tasks
  (:import [com.amazonaws.services.simpleworkflow.model RespondActivityTaskCompletedRequest RespondActivityTaskFailedRequest]))

(defn sendmail [client task]
  (let [_ (prn "Mail sendt")
        req (doto (RespondActivityTaskCompletedRequest.) (.withToken (.getTaskToken task) (.withResult "0")))]
    (. client (respondActivityTaskCompleted req))))

(defn sendsms [client task]
  (let [_ (prn "SMS sendt")
        req (doto (RespondActivityTaskCompletedRequest.) (.withToken (.getTaskToken task) (.withResult "0")))]
    (. client (respondActivityTaskCompleted req))))

(defn check-mail-delivery [client task]
  (prn "OK"))

(defn store-message [client task]
  (prn "STORED"))

(defn notyfy-spoc [client task]
  (prn "SPOC Notified"))