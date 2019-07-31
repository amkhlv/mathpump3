package diffpump

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets

import akka.actor.PoisonPill
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.io.Source
import scala.sys.process.{Process, ProcessBuilder, ProcessIO}

object Main extends App {

  for ((u, ar) <- board) {
    val racket : ProcessBuilder = Process(Seq(s"$home/.local/lib/mathpump/mathpump-board", u))
    @tailrec def setup(s: OutputStream) : Unit = {
      logger.info("polling " + u)
      var willContinue = true
      val polling = ar.ask(WaitForFile)(24 hours).andThen {
        case Success(x: String) =>
          logger.info("sending to ioqml stdin: " + x)
          s.write(x.getBytes(StandardCharsets.UTF_8))
          s.flush()
        case Failure(e) =>
          println("ERROR ---> " + e.getMessage)
          runningWhiteBoards.get(u) match {
            case Some(y) => y.destroy()
            case None => ()
          }
          willContinue = false
        case _ =>
          println("=== ERROR in IOQML stdout thread ===")
          willContinue = false
      }
      try {
        Await.result(polling, 24 hours)
      } catch {
        case e: java.lang.InterruptedException => println("MathPump is stopping")
      }
      if (willContinue) setup(s)
    }
    def printStream(s: InputStream): Unit = for (ln <- Source.fromInputStream(s).getLines()) println(ln)
    def receiveErr(stderr: InputStream) = {printStream(stderr); stderr.close()}
    val r = racket.run(new ProcessIO(setup,   _ => () ,   receiveErr))
    runningWhiteBoards.put(u, r)
  }

  new Watcher(dispatcher).run match {
    case WatcherRequestsShutdown => ()
  }

  //shutdown :
  dispatcher ! PoisonPill
  patcher ! PoisonPill
  beeper ! PoisonPill
  system.terminate().onComplete {
    case Success(_) => println("terminated actor system")
    case Failure(_) => println("error when terminating actor system")
  }
  delivery.close()
  
}
