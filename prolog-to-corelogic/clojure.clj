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
