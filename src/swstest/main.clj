(ns swstest.main
  (:use swstest.tasks)
  (:import [com.amazonaws.auth AWSCredentials PropertiesCredentials]
           [com.amazonaws.services.simpleworkflow AmazonSimpleWorkflowClient]
           [com.amazonaws AmazonServiceException ClientConfiguration Protocol]
           [com.amazonaws.services.simpleworkflow.model PollForActivityTaskRequest PollForDecisionTaskRequest TaskList RespondDecisionTaskCompletedRequest Decision DecisionType ScheduleActivityTaskDecisionAttributes ActivityType])
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
  (let [req (doto (PollForDecisionTaskRequest.) (.withDomain domain) (.withIdentity identity) (.withTaskList (doto (TaskList.) (.withName tasklist))) (.withReverseOrder (Boolean. "true")))]
    (. client (pollForDecisionTask req))))

(defn worker [id]
  (while true
    (let [task (poll-for-activity "Messaging" id "mainTaskList")]
      (cond
       (= "sendmail" (.getActivityType task)) (sendmail client task)
       (= "sendsms" (.getActivityType task)) (sendsms client task)))))

(defn decider [id]
  (while true
    (let [decision (poll-for-decision "Messaging" id "mainTaskList")
          events (.getEvents decision)
          last-event (first events)]
      (prn "EVENTS " events)
      (cond
       (= (.getEventType last-event) "WorkflowExecutionStarted") (prn "Started")
       (= (.getEventType last-event) "DecisionTaskScheduled") (prn "Scheduled")
       (= (.getEventType last-event) "DecisionTaskStarted") (prn "Task started")
       (= (.getEventType last-event) "WorkflowExecutionTimedOut") (prn "Timeout"))
      (let [dec (doto (Decision.) (.withDecisionType DecisionType/ScheduleActivityTask)
                      (.withScheduleActivityTaskDecisionAttributes
                       (doto (ScheduleActivityTaskDecisionAttributes.)
                         (.withActivityType (doto (ActivityType.) (.withName "sendmail") (.withVersion "1.0")))
                         (.withActivityId "test-1")
                         (.withInput "bar")
                         (.withTaskList (doto (TaskList.) (.withName "mainTaskList"))))))
            req (doto (RespondDecisionTaskCompletedRequest.) (.withTaskToken (.getTaskToken decision)) (.withDecisions (list dec)))] 
        (. client (respondDecisionTaskCompleted req))))))

(defn -main [type id]
  (cond
   (= type "W") (worker id)
   (= type "D") (decider id)))