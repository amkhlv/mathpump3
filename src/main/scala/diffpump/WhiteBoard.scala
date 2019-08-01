package diffpump

import java.io._

import akka.actor.{Actor, ActorRef}
import org.apache.log4j.{Logger, PropertyConfigurator}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

class WhiteBoard(person: UserName) extends Actor {

  val personConf = them.get(person).get
  val logger: Logger = Logger.getLogger("WHITEBOARD for " + personConf.dir)
  PropertyConfigurator.configure("log4j.properties")

  override def receive: Receive = receiveInit

  def receiveInit: Receive = {
    case WaitForFile =>
      logger.info("becoming Activated")
      context.become(receiveActivated(sender))
    case filename : String => logger.info("too fast, skipping WhiteBoard update")
  }

  def receiveActivated(sndr: ActorRef): Receive = {
    case ShowFile(filename)  => {
      logger.info("got filename : " + filename)
      val tempDir = new File(personConf.dir + "/tmp/")
      for (f <- tempDir.listFiles()) {
        if (f.isFile) {f.delete()}
      }
      val oldFile : File = new File(personConf.dir + "/" + filename)
      val fp: String = if (mustCopy) {
        val dt : DateTime = new DateTime()
        val dtfmts : DateTimeFormatter = DateTimeFormat.forPattern("y-M-d--H-m-SSS")
        val tmpFileName : String = dtfmts.print(dt) + ".svg"
        val newFile : File = new File(personConf.dir + "/tmp/" + tmpFileName)
        //copy to tmpfile:
        new FileOutputStream(newFile).getChannel.transferFrom(new FileInputStream(oldFile).getChannel, 0 , Long.MaxValue)
        newFile.getAbsolutePath
      } else { oldFile.getAbsolutePath }
      val cmd = s"""{"svgfile":"$fp"}""" + "\n"
      sndr ! cmd
      context.become(receiveInit)
    }
    case Shutdown => sndr ! Shutdown
    case WaitForFile => ()  //TODO will this ever happen?
  }


}
