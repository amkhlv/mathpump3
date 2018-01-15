package diffpump

import java.nio.file.{Files, Paths}

import org.apache.log4j.Logger

import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}

case class RemoteFileState(wholeFileAckPending : Boolean, checksums : ListBuffer[CheckSum])

class Situation {
  val logger: Logger = Logger.getLogger("SITUATION")
  private var lcl : Map[FileName, Array[Byte]] = new mutable.HashMap[FileName, Array[Byte]]
  for (f <- Paths.get(outDirName).toFile.listFiles())
    lcl.put(f.getName, Files.readAllBytes(Paths.get(outDirName,f.getName)))

  private var rmt : Map[UserName, mutable.HashMap[FileName, RemoteFileState]]  =
    new mutable.HashMap[UserName, mutable.HashMap[FileName, RemoteFileState]]
  for (k <- them.keys) rmt.put(k, new mutable.HashMap[FileName, RemoteFileState])

  private def newRFS(wholeFilePending: Boolean) =
    RemoteFileState(wholeFilePending, new ListBuffer[CheckSum])

  private def allRemoteFiles(u: UserName) : mutable.HashMap[FileName, RemoteFileState] =
    rmt.getOrElseUpdate(u, new mutable.HashMap[FileName, RemoteFileState])

  private def specificRemoteFile(u: UserName, fn: FileName) : RemoteFileState =
    allRemoteFiles(u).getOrElseUpdate(fn, newRFS(false))

  def getOldLocalContents(fn: FileName) : Array[Byte] =
    lcl.getOrElseUpdate(fn, Files.readAllBytes(Paths.get(outDirName + "/" +fn)))

  def setNewLocalContents(fn: FileName, bs: Array[Byte]) : Unit = lcl.put(fn, bs)

  def setWholeFileAckPending(u: UserName, fn: FileName, wholeFileAckIsPending: Boolean) : Unit =
    rmt.get(u) match {
      case Some(xm: mutable.HashMap[FileName, RemoteFileState]) =>
        xm.put(
          fn,
          RemoteFileState(
            wholeFileAckIsPending,
            xm.getOrElse(fn, newRFS(wholeFileAckIsPending)).checksums
          )
        )
      case None =>
        if (wholeFileAckIsPending) rmt.put(u, new mutable.HashMap[FileName, RemoteFileState]().+=((fn, newRFS(false))))
        else ()
    }

  def registerRemoteFile(u: UserName, fn: FileName, cs: CheckSum) : Unit =
    allRemoteFiles(u).update(fn, RemoteFileState(false, new ListBuffer[CheckSum]().+=:(cs)))

  def registerPendingPatch(u: UserName, fn: FileName, ncs: CheckSum) : Unit = {
    specificRemoteFile(u, fn).checksums.+=:(ncs)
    ()
  }

  def registerThatPatchWasApplied(u: UserName, fn: FileName) : Unit = {
    val rfs: mutable.Map[FileName, RemoteFileState] = allRemoteFiles(u)
    val rf: RemoteFileState = specificRemoteFile(u, fn)
    rfs.update(
      fn,
      RemoteFileState(rf.wholeFileAckPending, rf.checksums.dropRight(1))
    )
  }

  def noRemoteFileYet(u: UserName, fn: FileName) : Boolean = specificRemoteFile(u,fn).checksums.length == 0

  def patchesArePending(u: UserName, fn: FileName) : Boolean = specificRemoteFile(u,fn).checksums.length >= 2

  def wholeFileAckIsPending(u: UserName, fn: FileName) : Boolean = specificRemoteFile(u,fn).wholeFileAckPending

  def receiptIsOK(u: UserName, fn: FileName, ocs: CheckSum, ncs: CheckSum) : Boolean = {
    val css : ListBuffer[CheckSum] = specificRemoteFile(u, fn).checksums
    patchesArePending(u,fn) && (css.last == ocs) && (css.dropRight(1).last == ncs)
  }

  def printout : Unit = {
    for (u <- rmt.keys) {
      logger.info(" -" + u + ":")
      val fs = allRemoteFiles(u)
      for (f <- fs.keys) {
        val fstate = fs.getOrElse(f, newRFS(false))
        if (fstate.wholeFileAckPending)
          logger.info("    -" + f + " (ACK_PENDING):")
        else
          logger.info("    -" + f + ":")
        for (cs <- fstate.checksums) {
          logger.info("       -" + cs)
        }
      }
    }
  }

}
