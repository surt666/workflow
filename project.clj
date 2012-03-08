(defproject swstest "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [http.async.client "0.4.3"]
                 [com.amazonaws/aws-java-sdk "1.3.3"]
                 [org.slf4j/slf4j-simple "1.6.1"]
                 [log4j "1.2.16" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]]
  :dev-dependencies [[swank-clojure "1.4.0"]]
  :main swstest.main)