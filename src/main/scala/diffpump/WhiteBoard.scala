package diffpump

import java.io._

import akka.actor.{Actor, ActorRef}
import org.apache.log4j.{Logger, PropertyConfigurator}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

class WhiteBoard(person: UserName) extends Actor {

  val personConf = them.get(person) match {case Some(pc) => pc}
  val logger: Logger = Logger.getLogger("WHITEBOARD for " + personConf.dir)
  PropertyConfigurator.configure("log4j.properties")

  override def receive: Receive = receiveInit

  def receiveInit: Receive = {
    case Start =>
      logger.info("becoming Activated")
      context.become(receiveActivated(sender))
    case filename : String => logger.info("too fast, skipping WhiteBoard update")
  }

  def receiveActivated(sndr: ActorRef): Receive = {
    case filename : String => {
      logger.info("got filename : " + filename)
      val mainDir = new File(personConf.dir)
      val tempDir = new File(personConf.dir + "/tmp/")
      for (f <- tempDir.listFiles()) {
        if (f.isFile) {f.delete()}
      }
      val dt : DateTime = new DateTime()
      val dtfmts : DateTimeFormatter = DateTimeFormat.forPattern("y-M-d--H-m-SSS")
      val tmpFileName : String = dtfmts.print(dt) + ".svg"
      val newFile : File = new File(personConf.dir + "/tmp/" + tmpFileName)
      val oldFile : File = new File(personConf.dir + "/" + filename)
      //copy to tmpfile:
      new FileOutputStream(newFile).getChannel.transferFrom(new FileInputStream(oldFile).getChannel, 0 , Long.MaxValue)
      val tmpFP : String = newFile.getAbsolutePath
      val cmd = """{"svgfile":"""" + tmpFP + """"}""" + "\n"
      println("sending-->" + cmd + "<--")
      sndr ! cmd
      context.become(receiveInit)
    }
    case Start => ()  //TODO will this ever happen?
  }


}
