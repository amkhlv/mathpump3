#lang racket/gui

(require racket/cmdline racket/class racket/match json rsvg)

(define wintitle
  (command-line #:args (ttl) ttl))



(define window
  (new frame%
       [label wintitle]
       [width 800]
       [height 600]))

(define canvas
  (new canvas%
       [parent window]))

(define listener
  (thread (lambda ()
            (let loop ()
              (match (read-json)
                [(hash-table ('svgfile f))
                 (define svg (load-svg-from-file f))
                 (define dc (send canvas get-dc))
                 (send dc clear)
                 (send dc draw-bitmap svg 0 0)
                 ])
              (sleep 1.0)
              (loop)))))


(send window show #t)
