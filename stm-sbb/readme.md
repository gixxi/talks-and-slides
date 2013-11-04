#Software Transactional Memory @ SBB#

##Was ist eine Transaktion##

Ist der koordinierte Zugriff auf n Inhalte durch p Prozesse, wobei n,p E N+ unter Sicherstellung bestimmter
Kriterien für Sichtbarkeit, Abgrenzung und Dauerhaftigkeit.

##Was ist ACID##

* Atomar - Alles oder nichts
* Consistent - Daten gehen nicht kaputt, sind nach der Transaktion in einem konsistenten, hier sinnvollen, Zustand 
* Isolated - Zwischenergebnisse sind für unbeteiligte nicht resp. nur auf bestimmte Arten sichtbar
* Durable - Das Ergebnis einer Transaktion ist dauerhaft sichtbar auch bei einem der Transaktion nachgelagerten Crash

##Was ist Software Transactional Memory##

STM is ACID ohne D. Also ACI. Wofür brauchen wir das: *für Concurrency Controll* von mehreren Prozessen welche auf n Inhalte in einem 
Shared Memory Zugreifen. Z.b. Java Threads auf eine JVM Heap.
"Concurrency Controll" ist notwendig, seit es Prozesse, Threads oder auch nur Interupts gibt. Wo ist also der New Deal.
Es gab schon immer eine Alternative: Lock-based synchronisation, ist eine super-sache, funktioniert meistens!!!!

warum nur meistens, wer hat schon mal ISOLATION-LEVEL von nem DMBS auf serialized gestellt und nix ging mehr:
kann passieren wenn mehrkanaliges IPC und Lokale Transaktionalität gegeneinander arbeiten.

###Aber nochmal: was ist lock-based synchronisation###

Lock-based synchronisation: Alles was gefährlich ist, wird synchronisiert, Aufwand für Konfliktbewältigung ist 0, Aufwand fürs Sinnfreie Warten vor
der Gartentür in der Hoffnung, dass der Böse Hund im Garten ist dagegen gross, obwohl der Hund nur selten rauskommt.

Alternative 1: Teureren Singlecore Prozessor mit mehr Gigaherz, schnellerer I/O (einfach, aber zunehmend begrenzt)
Alternative 2: Partionieren des Systems (horizontale) und der Anfragen auf das System (einfach und gut)

vor weiteren Alternativen (vor allem der nächsten) immer 1 und 2 prüfen, ist mit wenig Kopfschmerz verbunden

Alternative 3: Identifikation parallelisierbarer Unteraufgaben und möglichst Automatische Parallisierung (z.B. Fork-Join)

Das ist recht einfach, wenn Anwendungsdesign map-reduce freundlich ist: 

ergo 1: wo nur möglich pure Funktionen (die sind dann in Java statisch, gell!?) verwenden, Algorithmen nehmen deren Schritte
dem Kommutativgesetz unterliegen: z.B. ich berechne einen Schlüssel lieber per Addition als per Subtraktion, da Addition von der Reihenfolge
der Anwendung Ihrer Summanden unabhängig ist.

ergo 2: weitgehend zustandsfrei und seiteneffektfrei programmieren. warum nur weitgehend, die Eigabe und Ausgabe eines Programms sind notwendige 
Seiteneffekte, ohne die ich mir das ganze Programm sparen könnte. Also: Seiteneffekte sind ein Notwendigkum :)

ergo 3: Anwendung von eventbasierter Programmierung prüfen

kleines beispiel für eventbasiertes programmieren in clojure:

(1) wir wollen eine Zahl + 5 rechnen und das ergebnis dieser addition dann durch 7, also brauchen
wir eine Aktivität der Eingabe, eine Aktivität für das + 5, eine aktivität für durch 7 und eine für das ausgeben von diesem ergebnis

wie machen wir dies nun in clojure: start 15:13 -> 15:25

<pre>
(def plus (agent {}))
(def durch (agent {}))
(def gibaus (agent {}))
(add-watch plus :plus (fn [a b c d]
                        (send durch assoc :a (+ 5 (:a d)))))
(add-watch durch :durch (fn [a b c d]
                          (send gibaus assoc :a (/ (:a d) 7))))
(add-watch gibaus :yepp (fn [a b c d]
                          (println (:a d))))

z.b.
(send plus assoc :a 1)
z.b.
(doseq [r 100]
	(send plus assoc :a r))
</pre>
    

Alternative 4: Aufteilen einer Aufgabe in parallelisierbare Unteraufgaben und manuelles parallelisieren

Das ist reduzierbar auf das rucksack problem, dieses ist np-vollständig: warum weshalb.

es geht um das thema conflict-serialisierbarkeit und view-serialisierbarkeit von zeitplänen. ein programm ist
eine art zeitplan, wann (transitiv oder referenziel gemeint) passiert was.

* view serialisierbar: zwei zeitpläne A,B sind view serialisiert, gdw. die zeitpläne so verzahnt sind, dass eine Aktivität aus A niemals "gleichzeitig" mit Aktivität aus B ein Datum X schreibt und liesst.
* conflict serialisiert: zwei zeitpläne A,B sind conflict serialisiert, gdw. die zeitpläne so verzahn sind, dass die Aktivitäten aus A und B welche in "Konflikt" mit einander stehen, zeitlich streng monoton geordnet sind.
 
Man könnte auch sagen, conflict serialisierung ist ein spezialfall von view serialisierung oder 

conflict serialisierung = "coarse grained" view serialisierung

jeder view-serialisierte Zeitplan ist auch conflikt-serialisiert, aber nicht umgekehrt.

noch ein wichtiger satz zu diesem thema:
