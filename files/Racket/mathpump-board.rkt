#lang racket/gui

(require racket/cmdline racket/class racket/match json rsvg)

(define wintitle (command-line #:args (ttl) ttl))

(define bitmap #f)

(define window
  (new frame%
       [label wintitle]
       [width 400]
       [height 300]))

(define canvas
  (new canvas%
       [parent window]
       [paint-callback
        (lambda (can dc)
          (when bitmap (send dc draw-bitmap bitmap 0 0))
          )]))

(define listener
  (thread (lambda ()
            (let loop ()
              (match (read-json)
                [(hash-table ('svgfile f))
                 (set! bitmap (load-svg-from-file f))
                 (send canvas min-client-width (send bitmap get-width))
                 (send canvas min-client-height (send bitmap get-height))
                 (send window refresh)
                 ])
              (loop)))))

(send window show #t)
