package diffpump

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets

import akka.actor.PoisonPill

import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.io.Source
import scala.sys.process.{Process, ProcessBuilder, ProcessIO}
import com.typesafe.config.Config
import org.apache.log4j.Logger

import scala.collection.immutable.StringOps

object Main extends App {

  val logger: Logger = Logger.getLogger("MAIN")
  val runningWhiteBoards : Map[UserName, Process] = for ((u, ar) <- board) yield {
    val racket : ProcessBuilder = Process(Seq("sh", "-c", viewer.replaceAllLiterally("%", u)))
    @tailrec def setup(s: OutputStream) : Unit = {
      logger.info("polling " + u)
      val mustContinue: Future[Boolean] = ar.ask(WaitForFile)(24 hours).map {
        case x: String => {
          logger.info("sending to whiteboard: " + x)
          s.write(x.getBytes(StandardCharsets.UTF_8))
          s.flush()
          true
        }
        case Shutdown => {
          logger.info(s"whiteboard for user $u will be SHUTDOWN")
          s.write(("""{"command": "exit"}""" + "\n").getBytes(StandardCharsets.UTF_8))
          s.flush()
          false
        }
      }
      if (Await.result(mustContinue, 24 hours)) { setup(s) } else { s.close() }
    }
    def printStream(s: InputStream): Unit = for (ln <- Source.fromInputStream(s).getLines()) println(ln)
    def receiveErr(stderr: InputStream): Unit = {printStream(stderr); stderr.close()}
    val r = racket.run(new ProcessIO(setup,   _ => () ,   receiveErr))
    (u -> r)
  }

  new Watcher(dispatcher).run

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
