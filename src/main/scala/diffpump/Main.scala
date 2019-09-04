package diffpump

import java.io.{BufferedReader, InputStream, InputStreamReader, OutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

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
    @tailrec def setupOut(s: OutputStream) : Unit = {
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
      if (Await.result(mustContinue, 24 hours)) { setupOut(s) } else { s.close() }
    }
    def setupIn(s: InputStream) : Unit = {
      val reader : BufferedReader = new BufferedReader(new InputStreamReader(s))
      var cmd : String = "";
      while ({cmd = reader.readLine(); cmd != null}) {
        cmd match {
          case "exit" => {
            logger.info("user requested shutdown")
            val stopFileOrig = Paths.get(s"tmp/stop/$stopWatcherFileName");
            val stopFileDest = Paths.get(outDirName, stopWatcherFileName) ;
            Files.move(stopFileOrig, stopFileDest)
          }
          case x => logger.error(s"unknown command $x on whiteboard stdout")
        }
      }
    }
    def printStream(s: InputStream): Unit = for (ln <- Source.fromInputStream(s).getLines()) println(ln)
    def receiveErr(stderr: InputStream): Unit = {printStream(stderr); stderr.close()}
    val r = racket.run(new ProcessIO(setupOut,   setupIn ,   receiveErr))
    (u -> r)
  }

  new Watcher(dispatcher).run

  //shutdown :
  dispatcher ! PoisonPill
  patcher ! PoisonPill
  beeper ! PoisonPill
  system.terminate().onComplete {
    case Success(_) => ()
    case Failure(_) => println("error when terminating actor system")
  }
  delivery.close()
  
}
