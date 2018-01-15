package diffpump

import java.nio.file._

import akka.actor.ActorRef
import org.apache.log4j.{Logger, PropertyConfigurator}
import org.joda.time.{DateTime, Duration}

import scala.language.postfixOps

class Watcher(sndr: ActorRef) {
  val logger: Logger = Logger.getLogger("WATCHER")
  PropertyConfigurator.configure("log4j.properties")
  var happy = true
  val fsys : FileSystem = Paths.get(".").getFileSystem
  var prevTime: DateTime = new DateTime()
  def run: Signal = {
    while (happy) {
      //Thread.sleep(1000);
      logger.info("continuing")
      val watcher = fsys.newWatchService()
      Paths.get(outDirName).register(watcher,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY)
      val watchKey = watcher.take()
      watchKey.pollEvents.iterator().forEachRemaining(
        (t: WatchEvent[_]) => {
          val evPath: WatchEvent[Path] = t.asInstanceOf[WatchEvent[Path]]
          val evCont = evPath.context()
          if (evCont.toString == stopWatcherFileName) {
            // This is a hook to stop this Watcher
            // To stop the watcher, we create a file with a special name
            val signalFile = Paths.get(outDirName, stopWatcherFileName).toFile
            if (signalFile.exists()) {
              signalFile.delete()
              happy = false
              logger.info("Detected (and deleted) the signal file; sending WatcherRequestsShutdown to Commander")
              sndr ! WatcherRequestsShutdown
            }
          } else if (Utils.notIgnored(evPath.context().toString)) {
            val nowTime = new DateTime()
            val passed = new Duration(prevTime, nowTime)
            prevTime = nowTime
            val evKind = evPath.kind()
            logger.info("Detected event in context: " + evCont + " of the kind: " + evKind)
            if ((evKind == StandardWatchEventKinds.ENTRY_CREATE) || (evKind == StandardWatchEventKinds.ENTRY_MODIFY)) {
              Thread.sleep(300)
              logger.info("Sending message to CENTRAL about " + evKind.toString + " of: " + evCont.toString)
              sndr ! NotificationOfFilesystemEvent(
                evKind,
                evCont match {
                  case p: Path => p
                  case _ => throw new RuntimeException("event context not a path!")
                }
              )
            }
          }
        }
      )
      watcher.close()
      if (! happy) logger.info("--- EXITING WATCHER ---")
    }
    WatcherRequestsShutdown
  }
}
