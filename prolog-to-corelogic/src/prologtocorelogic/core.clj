;;Prolog to clojure's core.logic
;;Prolog is the topdog in terms of logical programming
;;while core.logic is prolog-like programming within the 
;;lisp-implementation clojure. clojure is a functional programming
;;hosted within the java universe.
;;
;;christian.meichsner@informatik.tu-chemnitz.de
(ns prologtocorelogic.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:require [clojure.core.logic.pldb :as facts])
  (:require [clojure.core.logic.fd :as fd]))


;;Relational Programming in core.logic
;;

;;mann(adam).
;;mann(tobias).
;;(frank).
;;    frau(eva).
;;    frau(daniela).
;;    frau(ulrike).
;;    vater(adam,tobias).
;;    vater(tobias,frank).
;;    vater(tobias,ulrike).
;;    mutter(eva,tobias).
;;    mutter(daniela,frank).
;;    mutter(daniela,ulrike).
;;
(facts/db-rel mann n)
(facts/db-rel frau n)
(facts/db-rel vater v k)
(facts/db-rel mutter m k)

(def factbase
  (facts/db
    [mann :adam]
    [mann :tobias]
    [mann :frank]
    [frau :eva]
    [frau :daniela]
    [frau :ulrike]
    [vater :adam :tobias]
    [vater :tobias :frank]
    [vater :tobias :ulrike]
    [mutter :eva :tobias]
    [mutter :daniela :frank]
    [mutter :daniela :ulrike]))

(facts/with-db
  factbase
  (run* [q]
        (mann :tobias)
        (== q true)))

(facts/with-db
  factbase
  (run* [q]
        (mann :heinrich)
        (== q true)))

(facts/with-db
  factbase
  (run* [q]
        (frau q)))

(facts/with-db
  factbase
  (run* [q]
        (fresh [x y z]
               (vater x y)
               (vater y z)
               (== q [x z]))))


 
