#lang racket/gui

(define frame (new frame% [label "inkwell"]))

(define (doit button event)
  (system "xte 'keydown Alt_L'")
  (system "xte 'key Tab'")
  (system "xte 'keyup Alt_L'")
  (sleep/yield 0.3)
  (let-values ([(p stdout stdin stderr)
                (subprocess #f #f #f "/usr/bin/xdotool" "getwindowfocus" "getwindowname")])
    (let ([o (port->string stdout)] [e (port->string stderr)])
      (close-input-port stdout)
      (close-input-port stderr)
      (close-output-port stdin)
      ;(display o)
      ;(display e)
      (when (regexp-match #rx".*nkscap.*" o)
        (system "xte 'keydown Control_L'")
        (system "xte 'key s'")
        (system "xte 'keyup Control_L'")
        ;(display "Saved Inkscape Drawing")
        )
      )
    )
  )

(new button% [parent frame]
             [label "ink"]
             ; Callback procedure for a button click:
             [callback doit]
             [font (make-object font% 64 'default)]
             )

(send frame show #t)
