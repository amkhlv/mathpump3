#lang racket/gui

(require racket/cmdline
         racket/class
         racket/path
         racket/date
         racket/format
         racket/match
         json
         rsvg)

(define wintitle (command-line #:args (ttl) ttl))

(define svgfile #f)

(define bitmap #f)

(define (snapshot f)
  (let* ([snap-dir (build-path (path-only f) "snapshots")]
         [base-name (file-name-from-path f)]
         [d (current-date)]
         )
    (copy-file
     f
     (build-path snap-dir (string->path (format "~a-~a-~a_~a:~a:~a.svg"
                                                (date-year d)
                                                (~r (date-month d) #:min-width 2 #:pad-string "0")
                                                (~r (date-day d) #:min-width 2 #:pad-string "0")
                                                (~r (date-hour d) #:min-width 2 #:pad-string "0")
                                                (~r (date-minute d) #:min-width 2 #:pad-string "0")
                                                (~r (date-second d) #:min-width 2 #:pad-string "0"))))
     )))

(define window
  (new frame%
       [label wintitle]
       [width 400]
       [height 300]))

(define vert
  (new vertical-pane%
       [parent window]
       ))

(define canvas
  (new canvas%
       [parent vert]
       [paint-callback
        (lambda (can dc)
          (when bitmap (send dc draw-bitmap bitmap 0 0))
          )]))

(define buttons
  (new horizontal-pane%
       [parent vert]
       ))

(define snapshot-button
  (new button%
       [label "shapshot"]
       [parent buttons]
       [callback (lambda (btr ev) (snapshot svgfile))]
       ))

(define exit-button
  (new button%
       [label "exit all"]
       [parent buttons]
       [callback (lambda (btn ev) (displayln "exit") (flush-output))]
       ))

(define listener
  (thread (lambda ()
            (let loop ()
              (match (read-json)
                [(hash-table ('svgfile f))
                 (set! svgfile f)
                 (set! bitmap (load-svg-from-file f))
                 (send canvas min-client-width (send bitmap get-width))
                 (send canvas min-client-height (send bitmap get-height))
                 (send window refresh)
                 (loop)
                 ]
                [(hash-table ('command "exit"))
                 (exit)]
                [eof #t]
                )))))

(send window show #t)
