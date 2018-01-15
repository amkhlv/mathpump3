package diffpump

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardWatchEventKinds}

import akka.actor.Actor
import org.apache.log4j.{Logger, PropertyConfigurator}
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch
import org.joda.time.{DateTime, Duration}

class Central(delivery: PostOffice, situation: Situation) extends Actor {

  val logger: Logger = Logger.getLogger("CENTRAL")
  PropertyConfigurator.configure("log4j.properties")


  def sendWholeFile(fname: String): Unit = {
    val bts : Array[Byte] = Files.readAllBytes(Paths.get(outDirName,fname))
    logger.info(
      "I, " + myName + ", am about to send " + bts.length.toString + " bytes read from " + fname +
        " to " + them.keys.mkString(" and "))
    delivery.broadcast(them.keys.toList, ParcelFile(bts, myName, fname))
    for (towhom <- them.keys) {
      situation.registerRemoteFile(towhom, fname, Utils.sha1byte(bts))
      situation.setWholeFileAckPending(towhom, fname, wholeFileAckIsPending = true)
      situation.setNewLocalContents(fname, bts)
    }

  }

  def sendPatch(fname: String, ptch: String, towhom: String, ncs: CheckSum): Unit = {
    delivery.broadcast(List(towhom), ParcelPatch(patch = ptch, from = myName, filename = fname))
    situation.registerPendingPatch(towhom, fname, ncs)
  }

  override def receive: Receive = {
    case Start => ()
    case x: NotificationOfFilesystemEvent[_] =>
      val nowTime = new DateTime()
      val passed = new Duration(prevWatcherEventTime, nowTime)
      prevWatcherEventTime = nowTime
      if (passed.isLongerThan(new Duration(500))) {
        val basename = x.path.toFile.getName
        x.kind match {
          case StandardWatchEventKinds.ENTRY_MODIFY =>
            val bts = Files.readAllBytes(Paths.get(outDirName, basename))
            val ncs = Utils.sha1byte(bts)
            if (them.keys.exists(situation.noRemoteFileYet(_, basename))) {
              logger.info("  === sending whole file because NoRemoteFileYet ===  ")
              sendWholeFile(basename)
            }
            else {
              val dmp =  new DiffMatchPatch
              val ptch = dmp.patchToText(
                dmp.patchMake(
                  new String(situation.getOldLocalContents(basename), StandardCharsets.UTF_8),
                  new String(bts, StandardCharsets.UTF_8)
                )
              )
              them.keys.foreach(sendPatch(basename, ptch, _, ncs))
            }
            situation.setNewLocalContents(basename, bts)
            situation.printout
          case StandardWatchEventKinds.ENTRY_CREATE => ()
          case StandardWatchEventKinds.ENTRY_DELETE => ()
        }
      }
    case x: ParcelPatchError =>
      if (! situation.wholeFileAckIsPending(x.from, x.filename)) {
        logger.info("  === sending whole file because got ParcelPatchingError ===  ")
        logger.info("  === this means that the DMP of -->" + x.from + "<-- was unable to apply patch to -->" +
          x.filename + "<--")
        sendWholeFile(x.filename)
        situation.printout
      }
    case x: ParcelPatchReceipt =>
      if (! situation.wholeFileAckIsPending(x.from, x.filename))
        if (situation.patchesArePending(x.from, x.filename))
          if (situation.receiptIsOK(x.from, x.filename, x.oldSHA1, x.newSHA1)) {
            situation.registerThatPatchWasApplied(x.from, x.filename)
            situation.printout
          } else {
            logger.info("  === sending whole file because receipt is NOK ===  ")
            logger.info("  === oldSHA1: " + x.oldSHA1)
            logger.info("  === newSHA1: " + x.newSHA1)
            sendWholeFile(x.filename)
            situation.printout
          }
    case x: ParcelFileReceipt => {
      situation.setWholeFileAckPending(x.from, x.filename, wholeFileAckIsPending = false)
      situation.printout
    }
    case x: FileSavedOK => delivery.broadcast(List(x.sender), ParcelFileReceipt(myName, x.filename, x.SHA1))
    case x: PatchFailed => delivery.broadcast(List(x.sender), ParcelPatchError(myName, x.filename))
    case x: PatchAppliedOK =>
      delivery.broadcast(List(x.sender), ParcelPatchReceipt(myName, x.filename,x.oldSHA1,x.newSHA1))
  }

}
