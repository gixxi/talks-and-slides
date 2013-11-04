<pre>
Notizen zum Vortrag Software Transaction Memory (STM) am 26.11.2013 @ SBB
</pre>

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

####Alternative 1: Teureren Singlecore Prozessor mit mehr Gigaherz, schnellerer I/O (einfach, aber zunehmend begrenzt)####

####Alternative 2: Partionieren des Systems (horizontale) und der Anfragen auf das System (einfach und gut)####

vor weiteren Alternativen (vor allem der nächsten) immer 1 und 2 prüfen, ist mit wenig Kopfschmerz verbunden

####Alternative 3: Identifikation parallelisierbarer Unteraufgaben und möglichst Automatische Parallisierung (z.B. Fork-Join)####

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
    

####Alternative 4: Aufteilen einer Aufgabe in parallelisierbare Unteraufgaben und manuelles parallelisieren####

Das ist reduzierbar auf das rucksack problem, dieses ist im allgemeinfall leider np-vollständig: warum weshalb.

es geht um das thema conflict-serialisierbarkeit und view-serialisierbarkeit von zeitplänen. ein programm ist
eine art zeitplan, wann (transitiv oder referenziel gemeint) passiert was.

* view serialisierbar: zwei zeitpläne A,B sind view serialisiert, gdw. die zeitpläne so verzahnt sind, dass eine Aktivität aus A niemals "gleichzeitig" mit Aktivität aus B ein Datum X schreibt und liesst.
* conflict serialisiert: zwei zeitpläne A,B sind conflict serialisiert, gdw. die zeitpläne so verzahn sind, dass die Aktivitäten aus A und B welche in "Konflikt" mit einander stehen, zeitlich streng monoton geordnet sind.
 
Man könnte auch sagen, conflict serialisierung ist ein spezialfall von view serialisierung oder 

conflict serialisierung = "coarse grained" view serialisierung

jeder view-serialisierte Zeitplan ist auch conflikt-serialisiert, aber nicht umgekehrt.

Laufzeit-Complexität der Prüfung von View-Serialisierung: NP-Complete (something const^n, where n denotes the number of activities in the schedule)
Laufzeit-Complexität der Prüfung von Conflict-Serialisierung: Linear

Für STM brauchen wir genau diese Prüfung der Conflict-Serialisierung.

Bleiben wir aber mal noch bei *Alternative 4: Aufteilen einer Aufgabe in parallelisierbare Unteraufgaben und manuelles
parallelisieren". 

Ich prüfe *nicht* zur Laufzeit, ob n (zwei...8 gekippt) Zeitpläne conflict-serialisiert sind, um dead-locks zu vermeiden, sondern ich stelle
als ich als Programmierer stelle eine Serialisierung bereit. Manchmal unterstützt mich die Technologie bereit.

Was ist das übliche Vorgehen dabi: *Intentional Locking* = *Sperranwartschaft*

Wir unterscheiden also zwischen Exclusive Locks und Intentional Locks und arbeiten auf einer Sperrdomäne, welcher jedes
Objekt unserer Fachdomäne/zu sperrenden Domäne zuordenbar ist.

Die Sperrdomäne kann als Menge von geordneten Mengen, die zueinander in echter Teilmengenbeziehung zueinander stehen, beschrieben werden

Sperrdomäne = [A,B,C,D]
A= [A1...An]
B= [A1...An]

nun muss gelten,

falls Objekt X1 in A1 ist
und Objekt X2 in A1 ist
und A1 e A2 ist, dann muss auch X1,X2 e A2 sein.

Wir bleiben mal in unserer Domäne Zug

Wir haben Preise in Preisdreiecken, Preisdreiecke gehören zu einer TU
Wir gehen davon aus, dass Funktionen niemals TU übergreifens arbeiten (also keine Reports ala "Rückgabe der Summe aller Preise über alle TUs")

Wenn ich nun einen Preis P1 innerhalb einer Aktivität AP1 lese, setze ich einen Intentionallock auf dem Preis selber, auf dem entsprechenden Preisdreieck A, auf der TU
Wenn ich nun einen Preis P2 innerhalb einer Aktivität AP2 lese, setze ich einen Intentionallock auf dem Preis selber, auf dem entsprechenden Preisdreieck, auf der TU

Diese Leseoperationen konkurrieren nie, sind also parallelisierbar.

Wenn ich nun einen Preis P2 innerhalb einer Aktivität AP2 schreibe, dann setze ich einen Write-lock auf dem Preis, auf dem Preisdreieck sowie der TU.

Der Punkt ist nun, dass Schreibsperren und Sperranwartschaften (Intentional Locks) auf einem Element der Sperrdomäne. Nicht kompatibel sind, ergo muss einer auf den anderen Warten resp. müssen die Zeitpläne dies entsprechend berücksichtigen.

Genau das gleiche machen RDBMS auf Relationenbasis ohne direkten Domänenbezug, weswegen sich das Aufteilen der Daten auf mehrere Tabellen und ab bestimmter Datengrösse
eine weitere Segmentierung der Daten lohnen kann (z.B. Index-Partionierung): Row-Locks, Page-Locks, Extend-Locks (z.B. MSSQL), Page-Locks ... Lock Escalation.

Achtung, Achtung, Achtung: niemals auf RDBMs und OR-Mapper wie Hibernate verlassen, Funktionen mittels Transaktionen zu kapseln ist kein Garant gegen Dead-Locks, vielmehr ist das unbedarfte
@Transactional, @Transactional(Propagation=REQUIRES...) der Garant für Dead-locks), da weder Hibernate noch das DBMS die genuine Intelligenz eines Menschen zur Definition eines Zeitplanes für *potentiell* konkurrierende Transaktionen.

Warum nun schon wieder potentiell: 

(1) Weil ich in der Praxis die Konflikt-Serialisierung und nicht die View Serialisierung anstrebe
(2) Weil ich mit der Bereitstellung eines Zeitplanes die Konflikt-Serialisierung a priori anstrebe (Pessimistisches Sperren)
-> anstreben ... *Anspruch und Wirklichkeit* :)

####Alternative 5 - Implizites Erzwingen der Konfliktfreiheit durch (fine-grainded) Konfliktvermeidung resp. Konfliktauflösung####

* *Strong strict 2-phase locking* Jedes Datum, welches gelesen oder geschrieben wird, wird am Anfang der Transaktion gespeert und zwingend erst am ende der transaktion freigegeben. Problem: Programming in the large - grosses team, viele leute, transaktionspropagierung, wie werden die sperren von den funktionen und unterfunktionen (innerhalb einer transaktion) eingesammelt<br/> -> design pattern: transactionprovider per code instrumentation
