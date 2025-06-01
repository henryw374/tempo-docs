(ns app.tutorial)

(def tutorial*
  "Collection of map steps."
  [{:title "Introduction to Tempo"
    :content "<a href=\"https://github.com/henryw374/tempo\">Tempo</a> is a zero-dependency Clojure(Script) API to <a href=\"https://docs.oracle.com/javase/tutorial/datetime/iso/overview.html\">java.time</a> 
    on the JVM and <a href=\"https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Temporal\">Temporal</a> on JS runtimes (like this browser)

 <span id=\"location-of-editor\">Here on the right</span>
you have a **REPL**.
Functions from the main tempo ns are included under the alias 't'

Click on this expression: `(t/date-parse \"2020-02-02\")` to auto insert, or type into the REPL directly. 

You can type `(help)` for more commands.
   
"}
   {:title "Entities & Naming"
    :content "
<img src=\"https://tc39.es/proposal-temporal/docs/object-model.svg\"/>

The above graph shows the entities in `Temporal`. If you know `java.time` and you squint a bit, it will look familiar to
you. The Tempo API aims to find common ground between Temporal and java.time - sufficient to satisfy the majority of
use cases.

Regarding names, the java.time 'Local' prefix and the Temporal 'Plain' prefix have been removed, 
so e.g. PlainDate/LocalDate are just `date`

as in `(t/date? (t/date-parse \"2020-02-02\"))`.

ZonedDateTime is called 'zdt' to keep it short. 
    
js/Date and java.util.Date are called 'legacydate'

Otherwise, the naming of entities in `Tempo` should mostly be self-explanatory.
    "
    ;:test (constantly true)
    }
   {:title "Construction and Access"
    :content "
The naming of construction and access functions is based on mnemonics

The first word in the function is the entity name of the subject of the operation

`(t/date-now clock)`

`(t/date-parse \"2020-02-02\")` ;iso strings only

`(t/zdt-now clock)`

`(t/zdt-parse \"2024-02-22T00:00:00Z[Europe/London]\")` ;iso strings only

For the remainder of this section, it might be useful to refer to the entity graph in the previous page.

As well as parsing, one can build from parts
`(t/date-from {:year 2020 :month 2 :day-of-month 2})`

 the `-from` functions accept a map of components which must be sufficient to build the entity

`(t/datetime-from {:date (t/date-parse \"2020-02-02\") :time (t/time-now clock)})`

or equivalently

`(t/datetime-from {:year 2020 :month 2 :day-of-month 2 :time (t/time-now clock)})`

with `-from`, you can use smaller or larger components (size here is referring to number of fields). 

Larger entities take precedence. Below, the `:year` is ignored, because the `:date` took precedence (being larger) 

`(t/datetime-from {:year 2021 :date (t/date-parse \"2020-02-02\") :time (t/time-now clock)})`

One can 'add' a field to an object to create a different type. 

`(t/yearmonth+day-of-month (t/yearmonth-parse \"2020-02\") 1)` ; => a date

`(t/yearmonth+day-at-end-of-month (t/yearmonth-parse \"2020-02\"))` ; => a date

`(t/datetime+timezone_id (t/datetime-parse \"2020-02-02T02:02\") \"Pacific/Honolulu\")` 

To get a part of an entity, the function name will start with the type of the entity, followed by `->` then 
the target type. For example:

`(t/date->yearmonth (t/date-parse \"2020-02-02\"))`

`(t/date->month (t/date-parse \"2020-02-02\"))`

`(t/zdt->nanosecond (t/zdt-parse \"2024-02-22T00:00:00.1Z[Europe/London]\"))`

`(t/instant->epochmilli (t/instant-now clock))`

`(t/epochmilli->instant 123)`

`(t/legacydate->instant (js/Date.))`
    "}
   ;; Clocks
   {:title "Reified Clocks"
    :content
    "> Best practice for applications is to pass a Clock into any method that requires the current instant. 
- from the Javadoc of java.time.InstantSource
    
A Clock is something you can use to ask questions like 'what is your current time?' 
    and 'what timezone are you in?'
    
In both java.time and Temporal it is possible to use the ambient Clock by calling a zero-arity 'now' function, 
for example `(js/Temporal.Now.instant)`, but this impedes testing and so has no equivalent in Tempo.    
    
Create a Clock that is will return the current browser's time in the current timezone with 
  `(def clock (t/clock-system-default-zone))` or ...
  
A ticking clock in specified place
`(def clock (t/clock-with-timezone_id \"Pacific/Honolulu\"))`

 A clock fixed in time and place
`(def clock (t/clock-fixed (t/instant-parse \"2020-02-02T00:00:00Z\") \"Europe/Paris\"))`

Offset existing clock by specified millis
`(def clock (t/clock-offset clock -5))`

Create a mutable, non-ticking clock - simply change the value in the atom as required
`(def zdt-atom (atom (t/zdt-parse \"2024-02-22T00:00:00Z[Europe/London]\")))`
`(def clock (t/clock-zdt-atom zdt-atom))`

To use a clock, pass it to a '-now' function. for example `(t/date-now clock)`

mutable, non-ticking clock - simply change the value in the atom as required

`(def zdt-atom (atom (t/zdt-parse \"2024-02-22T00:00:00Z[Europe/London]\")))`

`(def clock-zdt-atom (t/clock-zdt-atom zdt-atom))` 

if you have other requirements for a clock, it is easy to create your own(t/clock  (fn get-instant [] do-whatever)  (fn get-zone [] do-whatever))
  "
    ;:test (constantly true)
    }
   {:title "Properties"
    :content "
Vars such as `t/hours-property` exist in Tempo. These combine the concepts (from the underlying date APIs) of units and fields,
so for example

`(t/until x y t/days-property)` ; how much time in unit days?
`(t/with (t/date-now clock) 11 t/days-property)` ; set the day of month field to 11

Combining the concept of unit and field is a simplification. 

In some cases it may be an over-simplification, for example `t/days-property` corresponds to the `day of month` field, 
so if `day of year` was required a new property would have to be created in user space. 

However, as per the stated aim of Tempo to just cover everyday use cases, 
hopefully the property concept has sufficient benefit to outweigh the cost. 

    "}
   {:title "Manipulation" 
    :content "
Manipulation: aka construction a new temporal from one of the same type

move date forward 3 days

`(t/>> (t/date-now clock) 3 t/days-property)`

move forward by some amount

`(t/>> a-date a-temporal-amount)`

 move date to next-or-same tuesday
`(t/date-next-or-same-weekday (t/date-now clock) 2)`

move date to prev-or-same sunday
`(t/date-prev-or-same-weekday (t/date-now clock) 7)` 

;; set a particular field
`(t/with (t/yearmonth-now clock) 3030 t/years-property)`

; set fields smaller than days (ie hours, mins etc) to zero
`(t/truncate (t/instant-now clock) t/hours-property)`

   "}
   
   {:title "Weekdays and Months" 
    :content "As you may have noticed, weekdays and months are not reified entities in Tempo (same as Temporal)
    
Weekdays are represented by numbers 1-7, with Monday being 1.

Months are represented by numbers 1-12, with January being 1.

However, to avoid magical numbers, Tempo provides vars that provide suitable names for these

`(->> t/weekday-tuesday (get t/weekday->weekday-name))`

and if you need to know the 

`(->> t/weekday-tuesday-name (get t/weekday-name->weekday))`
   
   "}
   {:title "Time zones" :content "
(t/timezone_id-now clock)
```

Timezone identifiers in `tempo` are just strings.

(t/zdt->timezone_id zdt)
(t/zdt-from {:datetime datetime :timezone_id timezone_id})

   
   "}
   {:title "Properties"
    :content "
    
\n\nVars such as `t/hours-property` exist in Tempo. These combine the concept of `units` and `fields`, so for example\n\n```clojure\n(t/until x y t/days-property) ; how much time in unit days\n(t/with x 11 t/days-property) ; set the day of month field to 11\n```\n\nCombining the concept of unit and field is a simplification. In some cases it may be an over-simplification, for example `t/days-property` corresponds to the `day of month` field, so if `day of year` was required a new property would have to be created in user space. \n\nHowever, as per the stated aim of Tempo to just cover everyday use cases, hopefully the property concept has sufficient benefit to outweigh the cost. \n    "
    }
   {:title "Manipulation" :content "
   
aka construction a new temporal from one of the same type\n\n```clojure\n\n;; move date forward 3 days\n(t/>> a-date 3 t/days-property)\n;; move forward by some amount\n(t/>> a-date a-temporal-amount)\n\n(t/date-next-or-same-weekday a-date 2) ; move date to next-or-same tuesday\n(t/date-prev-or-same-weekday a-date 7) ; move date to prev-or-same sunday\n\n;; set a particular field\n(t/with a-yearmonth 3030 t/years-property)\n\n; set fields smaller than days (ie hours, mins etc) to zero\n(t/truncate x t/days-property)\n\n```\n
   "}
   {:title "Guardrails" :content "
Consider the following:\n\n```clojure\n(let [start (t/date-parse \"2020-01-31\")]\n  (-> start \n      (t/>> 1 t/months-property)\n      (t/<< 1 t/months-property)))\n```\n\nIf you shift a date forward by an amount, then back by the same amount then one might think that the output would be equal to the\ninput. In some cases that would happen, but not in the case shown above.\n\nHere's a similar example:\n\n```clojure\n(let [start (t/date-parse \"2020-02-29\")]\n  (-> start\n      (t/with 2021 t/years-property)\n      (t/with 2020 t/years-property)))\n```\n\nWe increment the year, then decrement it, but the output is not the same as the input.\n\nBoth java.time and Temporal work this way and in my experience it is a source of bugs. For this reason, shifting `>>/<<`\nand `with` do not work in Tempo if the property is years or months and the subject is not a year-month.\n\nAs a safer alternative, I suggest getting the year-month from a temporal first, doing whatever with/shift operations you\nlike then setting the remaining fields.\n\nIf you do not wish to have this guardrail, set `t/*block-non-commutative-operations*` to false\n   
   "}
   {:title "Comparison" :content "
only entities of the same type can be compared\n\n(t/>= a b)\n\n(t/max a b c)\n\n; you must specify property\n(t/until a b t/minutes-property)\n\n```\n   
   "}
   {:title "Predicates" :content "
(t/date? x)   
   "}
   {:title "Temporal-amounts" :content "
   A temporal-amount is an entity representing a quantity of time, e.g. 3 hours and 5 seconds.\n\nTemporal-Amount entities are represented differently in java.time vs Temporal, but with some overlap.\n\nAn `alpha` ns (groan!) exists which contains a few functions for working with temporal-amounts.\n\nIf not sufficient, use reader conditionals in your code to construct/manipulate as appropriate.\n\n```clojure\n\n(require '[com.widdindustries.tempo.duration-alpha :as d])\n\n(d/duration-parse \"PT0.001S\")\n\n```\n
   "}
   {:title "Formatting" :content "
* formatting non-iso strings is not a feature in Tempo   "}
  
  ])


(def tutorial 
  (->> tutorial* 
       (into [] (map-indexed (fn [idx item]
                               (assoc item :index idx))))))