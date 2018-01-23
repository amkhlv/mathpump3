package diffpump

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util

import akka.actor.{Actor, ActorRef}
import org.apache.log4j.Logger
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch
class Patcher(dispatch: ActorRef) extends Actor {

  val logger = Logger.getLogger("PATCHER")

  

  override def receive: Receive = {
    case p: ParcelFile =>
      logger.info("got file")
      val filePath = Paths.get(them(p.from).dir, p.filename)
      Files.write(filePath, p.cont)
      dispatch ! FileSavedOK(p.from, p.filename, Utils.sha1byte(p.cont))
      board.get(p.from) match { case Some(x) => x ! p.filename }
    case p: ParcelPatch =>
      logger.info("got patch")
      val filePath = Paths.get(them(p.from).dir, p.filename)
      val curBytes = Files.readAllBytes(filePath)
      val curCS = Utils.sha1byte(curBytes)
      val curText =  new String(curBytes, StandardCharsets.UTF_8)
      val dmp =  new DiffMatchPatch
      val patchList = new util.LinkedList[DiffMatchPatch.Patch](
        dmp.patchFromText(p.patch)
      )
      val a = dmp.patchApply(patchList, curText)
      val newText : String = a.head match {
        case x: String => x
      }
      val diagnostics: List[Boolean] = a.tail.head match {
        case xs: Array[Boolean] => xs.toList
      }
      if (diagnostics contains false) {
        logger.error("************ PATCH ERROR => Requesting to resend the whole file ************")
        dispatch ! PatchFailed(p.from, p.filename)
      } else {
        val newBytes = newText.getBytes(StandardCharsets.UTF_8)
        Files.write(filePath, newBytes)
        dispatch ! PatchAppliedOK(p.from, p.filename, curCS, Utils.sha1byte(newBytes))
        board.get(p.from) match { case Some(x) => x ! p.filename }
      }
  }
  

}
