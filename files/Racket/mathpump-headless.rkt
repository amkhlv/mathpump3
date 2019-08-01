#lang racket

(require racket/match json)
(let loop ()
  (match (read-json)
    [(hash-table ('svgfile f))
     (loop)
     ]
    [(hash-table ('command "exit"))
     (display "exiting")
     (exit)]
    [eof #t]
    ))


