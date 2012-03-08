(ns swstest.main
  (:use swstest.tasks)
  (:import [com.amazonaws.auth AWSCredentials PropertiesCredentials]
           [com.amazonaws.services.simpleworkflow AmazonSimpleWorkflowClient]
           [com.amazonaws AmazonServiceException ClientConfiguration Protocol]
           [com.amazonaws.services.simpleworkflow.model PollForActivityTaskRequest PollForDecisionTaskRequest TaskList])
  (:gen-class))

(defn get-client [region]
  (let [creds (PropertiesCredentials. (.getResourceAsStream (clojure.lang.RT/baseLoader) "aws.properties"))
        config (ClientConfiguration.)]
    (. config (setProtocol Protocol/HTTPS))
    (. config (setMaxErrorRetry 3))
    (. config (setSocketTimeout 70000))
    (. config (setConnectionTimeout 70000))
   ;; (. config (setProxyHost "sltarray02"))
   ;; (. config (setProxyPort 8080))
    (doto (AmazonSimpleWorkflowClient. creds config) (.setEndpoint region))))

(def client (get-client "swf.us-east-1.amazonaws.com"))

(defn poll-for-activity [domain identity tasklist]
  (let [req (doto (PollForActivityTaskRequest.) (.withDomain domain) (.withIdentity identity) (.withTaskList (doto (TaskList.) (.withName tasklist))))]
    (. client (pollForActivityTask req))))

(defn poll-for-decision [domain identity tasklist]
  (let [req (doto (PollForDecisionTaskRequest.) (.withDomain domain) (.withIdentity identity) (.withTaskList (doto (TaskList.) (.withName tasklist))))]
    (. client (pollForDecisionTask req))))

(defn worker []
  (while true
    (let [task (poll-for-activity "Messaging" "worker1" "mainTaskList")]
      (cond
       (= "sendmail" (.getActivityType task)) (sendmail client task)
       (= "sendsms" (.getActivityType task)) (sendsms client task)))))

(defn decider []
  (while true
    (let [decision (poll-for-decision "Messaging" "decider1" "mainTaskList")
          events (.getEvents decision)
          last-event (first events)]
      (prn "EVENTS " events)
      (cond
       (= (.getEventType last-event) "WorkflowExecutionStarted") (prn "Started")))))

(defn -main []
  (worker))