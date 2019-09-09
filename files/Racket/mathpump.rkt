#lang racket

(require racket/format racket/path)



(provide mathpump-stop)
(provide mathpump-kill-inkscape)

(define (cur-dir-is-MathPump?) (file-exists? "mathpump.conf"))

(define (configure-MathPump-dir)
  (unless (directory-exists? "var") (make-directory "var"))
  (unless (directory-exists? "var/run") (make-directory "var/run"))
  (unless (directory-exists? "var/log") (make-directory "var/log"))
  )

(define (start-mathpump)
  (let ([j `(,(find-executable-path "java")
             "-Dconfig.file=mathpump.conf"
             "-jar"
             ,(path->string
               (build-path
                (find-system-path 'home-dir)
                ".local/lib/mathpump/mathpump-assembly.jar")))])
    (let-values ([(mp stdout stdin stderr)
                  (apply subprocess
                         (open-output-file "var/log/mathpump-uncaught.log"
                                           #:exists 'replace)
                         #f
                         'stdout
                         #f
                         j)])
      (close-output-port stdin)
      mp
      )))

(define (mathpump-stop)
  (for ([signal-file (in-directory "tmp/stop")] #:when (file-exists? signal-file))
    (rename-file-or-directory
     signal-file
     (build-path "outgoing" (file-name-from-path signal-file)))))

(define (start-inkscape)
  (let-values  ([(ink stdout stdin stderr)
                 (subprocess
                  (open-output-file "var/log/inkscape.log" #:exists 'replace)
                  #f
                  (open-output-file "var/log/inkscape.err" #:exists 'replace)
                  #f
                  (find-executable-path "inkscape")
                  "outgoing/whiteboard.svg")]
                )
    (close-output-port stdin)
    (open-output-file
     #:exists 'truncate
     (string->path (format "var/run/~a" (~r (subprocess-pid ink)))))
    ink))

(define (mathpump-kill-inkscape)
  (for ([pid (in-directory "var/run")]
        #:when (regexp-match #px"^\\d+$" (path->string (file-name-from-path pid))))
     (system (format "kill ~a" (path->string (file-name-from-path pid))))
    (delete-file pid)))

(module+ main
  
  (define do-start (make-parameter #f))
  (define do-inkscape (make-parameter #f))
  (define do-exit  (make-parameter #f))

  (command-line
   #:once-any
   [("-s" "--start") "start MathPump" (do-start #t)]
   [("-x" "--exit")  "exit MathPump"  (do-exit #t)]
   #:once-each
   [("-i" "--ink")   "also Inkscape"  (do-inkscape #t)]
   )

  (unless (cur-dir-is-MathPump?)
    (display "ERROR: we are not in a MathPump directory; exiting" (current-error-port))
    (exit))

  (configure-MathPump-dir)

  (when (do-start)
    (displayln "starting MathPump")
    (start-mathpump)
    (when (do-inkscape) (start-inkscape))
    "started MathPump"
    )


  (when (do-exit)
    (mathpump-stop)
    (when (do-inkscape) (mathpump-kill-inkscape)))

  (unless (or (do-start) (do-exit) (do-inkscape))
    ;; invoked without any flags (e.g. clicked in file manager)
    (start-mathpump)
    (start-inkscape)
    "started MathPump with Inkscape"
    )
  )

