package diffpump

import scala.sys.process.{Process, ProcessIO, _}
import org.apache.log4j.{Logger, PropertyConfigurator}
import akka.actor.Actor

class Beeper extends Actor {

  val logger: Logger = Logger.getLogger("BEEPER")
  def play (x: String) : Unit = {
    if (Seq("sh", "-c", player.replaceAllLiterally("%", x)).! != 0) {logger.error(s"could not run: $player $x")} else {logger.info(s"playing sound: $x")}
    ()
  }
  lazy val sounds = config.getConfig("sounds")

  override def receive: Receive = if (silent) {
    case _ => ()
  } else {
    case BeepFileOut => play(sounds.getString("fileOut"))
    case BeepPatchOut => play(sounds.getString("patchOut"))
    case BeepReceipt => play(sounds.getString("receipt"))
    case BeepError => play(sounds.getString("error"))
  }

}
