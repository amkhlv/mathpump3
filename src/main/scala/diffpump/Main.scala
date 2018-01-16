package diffpump

import akka.actor.PoisonPill
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global

object Main extends App {
  

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
