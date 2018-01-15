package diffpump

import java.nio.file.{Path, WatchEvent}

abstract class Signal {}

object StopDelivery extends Signal with Parcel with Serializable

object Ignore extends Signal with Parcel with Serializable

object Fix extends Signal with Parcel with Serializable

object Start extends Signal with Serializable
object Continue extends Signal
object Shutdown extends Signal

object WatcherRequestsShutdown extends Signal
object ReceiverGotStopSignal extends Signal
object SenderResigns extends Signal

case class NotificationOfFilesystemEvent[T](kind: WatchEvent.Kind[T], path: Path)

trait Parcel {}

case class ParcelPatchReceipt(from: UserName,
                              filename: FileName,
                              oldSHA1: CheckSum,
                              newSHA1: CheckSum) extends scala.Serializable with Parcel

case class ParcelPatchError(from: UserName,
                            filename: FileName) extends scala.Serializable with Parcel

case class ParcelFileReceipt(from: UserName,
                             filename: FileName,
                             SHA1: CheckSum) extends scala.Serializable with Parcel

case class ParcelFile(cont: Array[Byte], from: UserName, filename: FileName) extends scala.Serializable with Parcel

case class ParcelPatch(patch: String, from: UserName, filename: FileName) extends scala.Serializable with Parcel


case class FileSavedOK(sender: UserName,
                       filename: FileName,
                       SHA1: CheckSum) extends Signal

case class PatchAppliedOK(sender: UserName,
                          filename: FileName,
                          oldSHA1: CheckSum,
                          newSHA1: CheckSum) extends Signal

case class PatchFailed(sender: UserName,
                       filename: FileName) extends Signal

class ChannelWasClosed extends scala.Serializable with Parcel