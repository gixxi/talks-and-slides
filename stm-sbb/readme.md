<pre>
Notizen zum Vortrag Software Transaction Memory (STM) am 26.11.2013 @ SBB
</pre>

#Was haben wir heute vor: ERFA-Livehack.#

Das klingt ein bischen hochgestochen.

Wir vom Java:Core Team haben überlegt, welche Formate geeignet sind, um den Austausch und die Vernetzung unter den Software Entwicklern bei der SBB zu stärken.

Da gibts das etablierte Format ERFA, das ist sehr powerpointlastig. Dann gibts am anderen Ende der Skala das Code Dojo. Da kann jeder (max. 6 Leute auf einmal) hinkommen und man lernt voneinander, niemand braucht themenspezifisches vorwissen, gemeinsames erarbeiten
von wissen steht im vordergrund. Wir haben wir hier also keinen ERFA, aber auch kein Code Dojo. Ein neues Format zu etablieren ist, aber ein formaler oder administrativer Akt. Ergo heute unter dem ERFA Hut.
ERFAs sind powerpoint lastig. Wir arbeiten aber mit unseren Programmiersprachen und Compilern, mit unseren IDEs und sonstigen Hilfsmitteln. Wir wollen den Livehack dazu nutzen, Arbeitstechniken und Werkzeug vorzustellen, 
den Arbeitsstil zu diskutieren, ihr seit eingeladen, einfach reinzurufen, "Ja, aber" oder "Ja, genau" oder auch sonstwas zu sagen und so mitzuhelfen.

#RAM is Disk, Disk is Tape, Tape is ...#

Jim Gray, Turing Award Winner for seminal contributions to databases and transaction processing.

Ich sage mal voraus, dass wir für bestimmte Anwendungsbereiche langfristig nur noch flüchtige speicher anwenden resp. der medienbruch zwischen transient (im Sinne der Anwendung) und persistent wegfallen wird. 
SAP HANA, In-Memory Datenbanken, Caches als Beispiele ...
und seien wir mal ehrlich, lasst uns an allen Kisten die Stecker ziehen, alles wieder hochfahren, was geht dann noch ... für lange zeit nichts mehr -> bis die cache kohärenz wieder hergestellt ist, unsere DMBMS, unsere Webserver, unsere Nameserver leben von caches und in-memory datenhaltung.

dieses D (Durability) lassen wir heute mal weg und schauen, wie können wir transaktional transient arbeiten.

Anwendungsbereiche fallen mir da sehr viele ein:

Soziale Plattformen (Eventual Consistency bei Twitter)
Online-Games
Chat-Plattformen

Oder allgemein: Konkurrierende Zugriff auf mehr als eine Speicherzelle "anwendungsübergreifend"|"allgemein" zu implementieren.

#Software Transactional Memory @ SBB#

##Setup##

JDK

java version "1.7.0_17"
Java(TM) SE Runtime Environment (build 1.7.0_17-b02)
Java HotSpot(TM) 64-Bit Server VM (build 23.7-b01, mixed mode)

Clojure

Clojure 1.5.1

Build

Leiningen 2.3.1

Build #2

Clojure Maven Plugin
<pre>
		<plugins>
			<plugin>
				<groupId>com.theoryinpractise</groupId>
				<artifactId>clojure-maven-plugin</artifactId>
				<version>1.3.13</version>
				<configuration>
					<sourceDirectories>
						<sourceDirectory>src/main/clojure</sourceDirectory>
					</sourceDirectories>
					<testSourceDirectories>
						<testSourceDirectory>
							src/test/clojure</testSourceDirectory>
					</testSourceDirectories>
				</configuration>
				<executions>
					<execution>
						<id>compile-clojure</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
</pre>

... und dann einfach mvn clojure:nrepl

IDE

vim mit fireplace, vim-classpath und vim-clojure-static
eclipse-all-in-one
lighttable

###Zusammenarbeit VIM nRepl###

1. nrepl starten in projektverzeichnis: lein repl
2. vim starten in projektverzeichnis: vim
3. eines der clojure files editieren/öffnen
3. vim mit nrepl connecten: :Connect nrepl://127.0.0.1:55849

:Require für ganzen file in repl compilieren und importieren (clojure import)
:Eval für evaluation des aktuellen lexigrafischen scopes

*VIM* kann zum struturierten Aufbau der Funktionalität genutzt werden mit Syntax Highlight, Copy&Paste ..., Instant-Compile
*nRepl* zum halten des aktuellen Zustandes, zur Transition des aktuellen Zustandes, zum Ausprobieren, bei Fragen zur

*Wofür ist das alles gut*

Für den Wechsel vom iterativen Entwickeln (edit-compile-run-fail-edit-...) zum inkrementellen (edit-fail-in-small-edit-fail-in-small-edit-behappy)

Beispiel 1: ich will die funktion welche für eine folge von zahlen eine folge von Tupeln zurückgibt, jedes tupel enthält quadrat und quadratwurzel der eingabe

(def input (range 1000))
(def output (map (juxt #(* % %) #(Math/sqrt %)) input))

Beispiel 2: Arbeit mit sehr grossen Datenstrukturen, manchmal hat man dann auch ein aha erlebnis, weil man sich die zwischenergebnisse bewusst macht, summe einer teilmenge der unendlichen folge von quadratzahlen, wobei ich
nicht möchte das die ersten n quadratzahlen mehrfach berechnet werden.

(def rands (repeatedly #(Math/random)))

;;test the quality of the rands
;;multiple calls do not imply multiple realisations of the sequence
(take 10 rands)
(apply + (take 10000 rands))

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
"Concurrency Control" ist notwendig, seit es Prozesse, Threads oder auch nur Interupts gibt. Wo ist also der New Deal.
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
* *Precendence Graph Cycle Elimination (2PL)* Vorgängiges Aufzeichnen des Precedence Graphen (es gibt zwischen Aktivität A und B genau dann eine Kante, wenn die Korrektheit der Durchführung von B nur gewährleistet ist, wenn A komplett durchgelaufen ist. Eliminierung der Zyklen.
* *Timestamp ordering (TO)* Zentrales Konstrukt sind global gültige, disjunkte Zeitstempel. Vereinfacht gesagt, bekommt jede Transaktion bei Begin einen Zeitstempel TS. Jedes Lesen resp. Schreiben eines Datum stellt als Nachbedingung das Setzen eines Zeitstempels (RTS=Lesen, WTS=Schreiben) sicher und stellt gewisse Vorbedingungen an die bereits gesetzten Zeitstempel. Lesen: WTS < TS (Letztes Schreiben muss vor Transaktionsbegin stattgefunden haben). Schreiben: RTS < TS (Eine Andere Aktivität bedingt bereits den alten Wert), WTS < TS (Thomas Write Rule), letzteres klingt so kompliziert, aber man kann es folgendermassen darstellen: "The rule prevents changes in the order in which the transactions are executed from creating different outputs: The outputs will always be consistent with the predefined logical order." - auf gut deutsch: die regel stellt sicher, dass das ergebnis (der zustand der DB) bei gleicher Eingabe nur von der Reihenfolge des Starts der Tx abhängt und nicht vom Zufall resp. den Unwägbarkeiten des Multithreadings. <br/> _Ist das Wirklich lockfrei_ Nun ja, kommt auf die Perspektive an: Die Bindung der Zeitstempel an die Objekte impliziert auch einen Lock, wenn auch nur für kurze Zeit, dafür aber sehr sehr viele.
* *Snapshot Isolation* Eine Transaktion hat Zugriff auf den letzten commiteten Stand der zu lesenden Werte bei Start der Transaktion. Ein Commit der Transaktion erfolgt nur, wenn alle durch die Transaktion geänderten Werte nicht mit Updates anderer Transaktionen kollidieren, welche nach dem Start dieser Transaktion durchgeführt wurden. Dieses Verfahren nutzt ebenfalls Zeitstempelinformationen auf Daten und Transaktion. Snapshot Isolation bedient sich meistens Multiversion Concurrency Control (MVCC) für die Daten(strukturen). <br/> _Ist nun alles gut_

####Zu Snapshot Isolation####

F: Ist nun alles gut. A: Nicht per se.

Folgendes Klassisches Beispiel: Konto A 100 CHF, Konto B 100 CHF, Bedingung der Bank: A+B >= 0. TxA: Von A 100 CHF abheben. TxB: Von B 100 CHF abheben.

Tx(Konto, Amount):
	A,B lesen
	Konto um Amount erleichtern
	Invarianz prüfen
	fertig.
	
Zwei konkurrierende Transaktionen die genau eins der beiden Konten erleichtern wollen, führen nicht zur Verletzung der Invarianz. Transaktionen auf verschiedenen Konten dagegen schon.

Warum: Invarianz ist eine semantische Verschränkung zweier Speicherzellen.

Also: Programmierer muss genuine Intelligenz walten lassen und die Invarianz einprogrammieren. Ist das immer der Fall? Nein!
-> Invarianzen, welche durch einen funktionalen Zusammenhang zweier Speicherzellen beschreibbar sind, muss ich explizit einprogrammieren.
-> Invarianzen, welche durch eine Mengenbeziehung zwischen zwei Speicherzellen beschreibbar sind, dagegen nicht (z.B. in clojure dosync->alle veränderungen durch alter, commute, ref-set müssen in dosync stattfinden und sind teil der transaktion

##STM in Clojure##

STM in Clojure basiert auf der Verwendung von Referenztypen, namentlich des Referenztyps ref (koordinierter, synchronisierter Zugriff auf Speicher)

###Wrap-Up: Referenztyp###

Ein Referenztyp macht die Unterscheidung zwischen Identität und Zustand möglich. In objektorientierten Sprachen wie Java habe ich Klassen und Instanzen mit Eigenschaften und Verhalten. Bereits mit dieser Definition habe 
ich das erste Problem am Hals. 

Zitat: "Mit Eigenschaften" (Plural) mit dem Plural beginnt der Käse. 

Wenn wir folgende Klasse haben:

class Animal {
	public int age; //years
	public String type;
	public Animal setAge(int age) {
		this.age = age;
		return this;
	}
	
	public Animal setType(String type) {
		this.type = type;
		return this;
	}
}

Animal firefly = new Animal().setAge(1).setType("firefly");
firefly.setAge(20);
firefly.setType("zebra");

haben wir zwei Eigenschaften aber einen semantischen Zusammenhang zwischen den beiden Eigenschaften, Tiere haben artspezifische Maximalalter.
Hier habe ich kurzfristig einen Zustand, der keinen Sinn ergibt.

Wo liegt der Fehler, bei der Objektorientierung oder bei uns. Bei uns! Was ist der Fehler:

Instanzen von Klassen haben _keine Eigenschaften_, sondern nur Verhalten. Verhalten ist rekursiv definiert und gibt im atomaren Fall ein Attribut des im aktuellen Context (zeitlich, räumlich) gültigen Zustandes zurück.

Wenn wir Anfangen, so zu programmieren, können wir auch in Java solche Seiteneffekte vermeiden:

Also:

(1) Konstruktor- oder Factorybasierte Erzeugung von Klasseninstanzen
(2) Keine Setter für Attribute, die einen inhaltlichen Zusammenhang mit anderen Attributen
(3) Pure Funktionen ohne Seiteneffekte bevorzugen (ich habe mich kürzlich mit jemanden hier in der gruppe über statische methoden unterhalten, er meinte, dass wäre ja nicht objektorientiert, so what, wenn ich von der eingabe meines programmes bis zur ausgabe keinen zustand brauche, brauche ich weder instanzvariablen, noch instanzfunktionen noch klassenvariablen, ergo nur statische funktionen, ist dies nun schlecht, nein!!!
    das kann mich in vielerlei hinsicht retten. parallelisierbarkeit, testbarkeit, idempotenz!!!
    
What is the clojure way!

Referenztypen zeigen auf Identitäten und repräsentieren diese Identität während Ihrer Lebensdauer. Diese Identität erfährt Veränderungen. Veränderungen überführen einen Zustand der Identität in den nächsten Zustand. Ein Zustand wird aber durch die
Veränderung nicht ungültig.
Veränderungen sind stets atomar und durch Funktionsanwendung erreicht. Veränderungen werden auf Instanzen immutabler Datenstrukturen durchgeführt. Deswegen funktionale Sprache.

in lighttable

<pre>
(def animal {})
(assoc animal :age 1 :type "firefly")
animal
</pre>

assoc ändert die identität von animal nicht. 

besser sichtbar mit referenztyp atom, atom repräsentiert eine identität auch über zustandswechsel hinweg (synchron, unkoordiniert)

<pre>
(def animal {})
(def firefly (assoc animal :age 1 :type :firefly))
(def zebra (assoc firefly :age 40 :type :zebra))

(def animal (atom {}))
(swap! animal assoc :age 1 :type :firefly)
(def firefly @animal)

(swap! animal assoc :age 40 :type :zebra)
(def zebra @animal)

firefly
zebra
</pre>

