(ns app.tutorial
  (:require
   [clojure.string :as string]
   [sci.lang :refer [Var]]
   [com.widdindustries.tempo :as t]))

(def tutorial*
  "Collection of map steps."
  [{:title "Introduction to Tempo"
    :content "Tempo is a zero-dependency Clojure(Script) API to <a href=\"https://docs.oracle.com/javase/tutorial/datetime/iso/overview.html\">java.time</a> 
    on the JVM and <a href=\"https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Temporal\">Temporal</a> on JS runtimes (like this browser)

 <span id=\"location-of-editor\">Here on the right</span>
you have a **REPL**, the same as <a href=\"https://tryclojure.org\">Try Clojure</a> except that
functions from <a href=\"https://github.com/henryw374/tempo\">Tempo</a> are included under the alias 't'

Try to type some expressions as `(t/date-parse \"2020-02-02\")` or click
on code to auto insert. You can type `(help)` for more commands.
   
"}
   {:title "Entities & Naming"
    :content "
<img src=\"https://tc39.es/proposal-temporal/docs/object-model.svg\"/>

The above graph shows the entities in Temporal. If you know java.time and you squint a bit, it will look familiar to
you. The Tempo API finds common ground between Temporal and java.time 

The java.time 'Local' prefix and the Temporal 'Plain' prefix have been removed, so e.g. PlainDate/LocalDate is just date

as in `(t/date? (t/date-parse \"2020-02-02\"))`.

ZonedDateTime is called 'zdt' to keep it short. 
    
js/Date and java.util.Date are called 'legacydate'

Otherwise, the naming of entities in Tempo should mostly be self-explanatory.
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

build from parts
`(t/date-from {:year 2020 :month 2 :day-of-month 2})`

 the -from functions accept a map of components which is sufficient to build the entity

`(t/datetime-from {:date (t/date-parse \"2020-02-02\") :time (t/time-now clock)})`

or equivalently

`(t/datetime-from {:year 2020 :month 2 :day-of-month 2 :time (t/time-now clock)})`

with -from, you can use smaller or larger components. 

larger ones take precedence. below, the :year is ignored, because the :date took precedence (being larger) 

`(t/datetime-from {:year 2021 :date (t/date-parse \"2020-02-02\") :time (t/time-now clock)})`

'add' a field to an object to create a different type

`(t/yearmonth+day (t/yearmonth-parse \"2020-02\") 1)` ; => a date

`(t/yearmonth+day-at-end-of-month (t/yearmonth-parse \"2020-02\"))` ; => a date

`(t/datetime+timezone_id (t/datetime-parse \"2020-02-02T02:02\") \"Pacific/Honolulu\")` 

to get parts of an entity, the function name will start with the type of the entity, then add -> then put the target type. For example:

`(t/date->yearmonth (t/date-parse \"2020-02-02\"))`

`(t/date->month (t/date-parse \"2020-02-02\"))`

`(t/zdt->nanosecond (t/zdt-parse \"2024-02-22T00:00:00.1Z[Europe/London]\"))`

`(t/instant->epochmillisecond an-instant)`

`(t/epochmilli->instant 123)`

`(t/legacydate->instant (js/Date.))`
    "}
   ;; Clocks
   {:title "Reified Clocks"
    :content
    "> Best practice for applications is to pass an Clock into any method that requires the current instant. - Javadoc of java.time.InstantSource
    
  A Clock is something you can use to ask questions like 'what is your current time?' 
    and 'what timezone are you in?'
    
In both java.time and Temporal it is possible to use the ambient Clock by calling a zero-arity now function, 
for example `(js/Temporal.Now.instant)`, but this is not good functional programming practice and so has no equivalent in Tempo.    
    
  Create a Clock that is will return the current browser's time in the current timezone with 
  `(def clock (t/clock-system-default-zone))` or ...
  
 a ticking clock in specified place
`(def clock (t/clock-with-timezone_id \"Pacific/Honolulu\"))`

 a clock fixed in time and place
`(def clock (t/clock-fixed (t/instant-parse \"2020-02-02T00:00:00Z\") \"Europe/Paris\"))`

 offset existing clock by specified millis
`(def clock (t/clock-offset clock -5))`

create a mutable, non-ticking clock - simply change the value in the atom as required
`(def zdt-atom (atom (t/zdt-parse \"2024-02-22T00:00:00Z[Europe/London]\")))`
`(def clock (t/clock-zdt-atom zdt-atom))`

To use a clock, pass it to a -now function. for example `(t/date-now clock)`
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
   {:title "" :content ""}
  
  ])


(def tutorial 
  (->> tutorial* 
       (into [] (map-indexed (fn [idx item]
                               (assoc item :index idx))))))