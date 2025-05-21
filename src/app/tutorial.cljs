(ns app.tutorial
  (:require
   [clojure.string :as string]
   [sci.lang :refer [Var]]
   [com.widdindustries.tempo :as t]))

(def tutorial
  "Collection of map steps."
  [{:title "Entities & Naming"
    :content "
<img src=\"https://tc39.es/proposal-temporal/docs/object-model.svg\"/>

The above graph shows the entities in Temporal. If you know java.time and you squint a bit, it will look familiar to
you. The Tempo API finds common ground between Temporal and java.time 

The java.time 'Local' prefix and the Temporal 'Plain' prefix have been removed, so e.g. PlainDate/LocalDate is just date

as in `(t/date? (t/date-parse \"2020-02-02\"))`.

ZonedDateTime is called 'zdt' to keep it short. 
    
js/Date and java.util.Date are called 'legacydate'

Otherwise, the naming of entities in Tempo should mostly be self-explanatory.

Please move on by clicking `(next-step)`
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

Please move on by clicking `(next-step)` or go back with `(prev-step)`
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
  
  Please move on by clicking `(next-step)` or go back with `(prev-step)`
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
    :content "
    As you may have noticed, weekdays and months are not reified entities in Tempo (same as Temporal)
    
    Weekdays are represented by numbers 1-7, with Monday being 1.
    
    Months are represented by numbers 1-12, with January being 1.
    
    However, to avoid magical numbers, Tempo provides vars that provide suitable names for these
   
   `(get t/weekday-name->weekday t/weekday-tuesday)`
   
   "}
   {:title "" :content ""}
  
  ])

#_ [ {:title "Math is (fun)"
      :content
      "In Clojure mathematical operators are like normal functions. 
       As you already know, you have to include them in parentheses `(...)`.
    
    So instead of `4 + 2` you will do `(+ 4 2)`. Try to type a numerical operation with `+-/*`."
      :test #(number? %)}
    ;; Functions
    {:title "Clojure is functional"
     :content
     "> Lisp is functional. And the future is looking very functional to me. - Robert C. Martin
   
   The first argument of a list needs to be a **function**. The *rest*, 
      are the arguments to that function. In the expression `(not true)`,
      *not* is the negation function and *true* is the argument. 
      
   Try to use the function `(my-name)` followed by your name as a \"string\", as `(my-name \"Elia\")`."
     :test #(and (map? %) (contains? % :user-name) (string? (:user-name %)))}
    ;; Keywords
    {:title "Don't forget the keys"
     :content
     "> These days, the problem isn't how to innovate; it's how to get society to adopt the good ideas that already exist. - Douglas Engelbart
      
   Hi there, **[[user-name]]**! Nice to meet you.
     
   In the REPL you are getting back the evaluation of the expression that you typed.
     As you can see, `:user-name` is in a special form; it's called a *keyword*.
     You have to prepend `:` to a word to create one. 
      
   Use the keyword `:next` to continue."
     :test #(= % :next)}
    ;; Exercise - 01
    {:title "A function for everything"
     :content
     "Let's tweak our interface!
      
   You already know how to invoke functions, how to use keywords and strings.
      What if I tell you that you can change the prompt?
 
   You can call functions with keywords parameters, as `(create-dog :name \"Zeus\" :breed \"Beagle\")`
      
   Use the function `(set-prompt)` and set a color. It accepts optionally `:color` and `:text` as strings. 
      Or click on `(set-prompt :color \"red\")`"
     :test #(contains? % :prompt-color)}
    ;; 
    {:title "Functional practicioner!"
     :content
     "Congratulations, you have called a function and changed the state of the application! 
      And the entire command was… a list!
                                                                                      
   Clojure offers multiple functions to work with lists, such as `reverse`. It reverses a collection.
      So if you pass a string, it will use it as a collection of characters.
      
   Type `(reverse \"a-long-string\")` to advance at the next step."
     :test #(= % (reverse "a-long-string"))}
    ;; Vectors
    {:title "We have vectors"
     :content
     "> Lisp is worth learning for the profound enlightenment experience you will have when you finally get it. - Eric Raymond
      
   **Vectors** (aka arrays) contain sequential elements and they have a faster access compared to lists.
      
   To create a vector you need to include the items into squared brackets `[]` without any separator.
      
   Create a vector of elements, like your favorite names for cats `[\"luna\" \"milu\" \"boris\"]`. "
     :test #(vector? %)}
    ;; Variables
    {:title "Def your variables"
     :content
     "> Good programmers don't just write programs. They build a working vocabulary. - Guy Steele
                                                                                      
   **Global** variables are defined using `def`. Their value could be anything.
      
   Create a global variable called `foo` with a value. E.g. `(def foo \"bar\")`"
     :test #(and (instance? Var %) (= "foo" (-> (.-meta %) :name str)))}
    ;; Let
    {:title "Let it be local"
     :content
     "> Lisp is so great not because of some magic quality visible only to devotees, but because it is simply the most powerful language available. - Paul Graham
      
   **Local** variables could be defined using `let`. They will be available only
      inside the lexical context of the `let`. In the expression `(let [x 1] x)`
      you can refer to x only inside the `body` part delimited by `()`.
      
   Create numeric variables and multiply them like `(let [a 2 b 3] (* a b))`."
     :test #(number? %)}
    ;; Maps
    {:title "Maps are dictionaries"
     :content
     "> Any fool can write code that a computer can understand. Good programmers write code that humans can understand. - Martin Fowler
      
   Maps are collections that map *keys* to *values*. They're wrapped into `{}`. 
      You can use everything as a key but Clojure programmers mostly use keywords.
      
   Create a map with a key `:country` and your country as a string. 
      Like `{:country \"Australia\"}`."
     :test #(and (map? %) (contains? % :country) (string? (:country %)))}
    ;; F-list
    {:title "First of list"
     :content
     "Clojure offers some functions to extract content from the list. For example, 
      `first` returns the first element.
      
   Type `(first '(\"alpha\" \"bravo\" \"charlie\"))` to get the first element."
     :test #(and (string? %) (= "alpha" %))}
    ;; Range
    {:title "Range of N"
     :content
     "The Clojure function `range` creates a list of numbers from 0 to `n` (excluded). 
      So `(range 5)` will return numbers from 0 to 4. Use `(doc range)` to print
     the documentation. 
      
   Create a range from 0 to 99 or click on `(range 100)` :)."
     :test #(= % (range 100))}
    ;; Filter
    {:title "Filter a list"
     :content
     "We can apply functions to a list. For example, using `filter` we can remove
      all the elements that are not respecting our condition.
      
   Try to remove all the *even* numbers from 0 to 50. Psst, `(filter odd? (range 50))`"
     :test #(= % (filter odd? (range 50)))}
    ;; Map
    {:title "Apply functions on lists"
     :content
     "If we want a list of multiples of 11 less than 100, the process to find them
      is to take each number from 1 to 9, multiply it by 10 and add it to the number, as
      `5 * 10 + 5 = 55`. We can do the same thing with Clojure using `map`.
 
   `map` simply applies a function to every element of a list. 
      
   So use `(map (fn [n] (+ n (* n 10))) (range 1 10))` to do it."
     :test #(= % (map (fn [n] (+ n (* n 10))) (range 1 10)))}
    ;; Inline functions
    {:title "Inline functions"
     :content
     "In the previous step, we wrote an inline function and passed it as argument
      to `map`. I'm referring to `(fn [n] (+ n (* n 10)))`. This technique is useful to create
      functions as *generic utilities* and not write them for a specific use case.
      
  Now create a function that takes `l` and `b` and returns the perimeter of a rectangle:
      
  `(fn [l b] (* (+ l b) 2))`."
     :test #(= (apply % [2 3]) 10)}
    ;; REPL
    {:title "REPL driven development"
     :content
     "> The only way to learn a new programming language is by writing programs in it. - Kernighan and Ritchie
      
   You’re currently solving a list of problems by typing code into the REPL and testing it. 
      That’s exactly what a Clojurist does! It's faster to test your code while typing than compiling and debugging it later!
      
   But Clojure is much more than this. Type `(more)` to go to the last step."
     :test #(true? %)}
    {:title "It's time to learn Clojure!"
     :content
     "> In the beginner’s mind there are many possibilities, but in the expert’s there are few - Zen Mind, Beginner's Mind
      
  Clojure is **not** as difficult as it seems. Parentheses, functions, immutable data structures and the REPL
      will become your friends. Just keep a beginner's mind!
      
  Some good resources to start are [Clojure koans](http://clojurekoans.com/), [4Clojure](https://4clojure.oxal.org/) or [exercism](https://exercism.org/tracks/clojure).

  Ask the community for support, and good luck!"
     :test #(true? false)}]
