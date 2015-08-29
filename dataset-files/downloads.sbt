
resourceGenerators in Compile <+= (resourceManaged in Compile, taskTemporaryDirectory, streams) map { (dir, temp, streams) ⇒
  val cifarfiles = Map(
    url("http://www.cs.toronto.edu/~kriz/cifar-10-binary.tar.gz") →
      ("cifar-10-batches-bin", "batches.meta.txt" :: "test_batch.bin" :: List(1,2,3,4,5).map(i ⇒ s"data_batch_$i.bin")),

    url("http://www.cs.toronto.edu/~kriz/cifar-100-binary.tar.gz") →
      ("cifar-100-binary" → List("coarse_label_names.txt", "fine_label_names.txt", "train.bin", "test.bin"))
  )

  val expectedFiles = cifarfiles.map(_._2).flatMap { case (d, fs) ⇒ fs map (f ⇒ dir / d / f) }.toSeq

  if (!expectedFiles.forall(_.exists())) {
    cifarfiles foreach {
      case (url, (d, fs)) ⇒
        val tgz = temp / url.toString.split("/").last
        streams.log.info(s"Downloading $url")
        IO.download(url, tgz)
        val ua = new org.codehaus.plexus.archiver.tar.TarGZipUnArchiver
        ua.enableLogging(nullPLogger)
        ua.setSourceFile(tgz)
        ua.setDestDirectory(dir)
        dir.mkdirs()
        ua.extract()
    }
    expectedFiles.foreach { f ⇒ if (!f.exists()) streams.log.warn(s"$f not found") }
  }
  expectedFiles.toSeq

}

// todo: Find a better untgz that doesn't need this nonsense
import org.codehaus.plexus.logging.{Logger ⇒ PLogger}

lazy val nullPLogger = new PLogger {
  override def warn(message: String): Unit = ()
  override def warn(message: String, throwable: Throwable): Unit = ()
  override def isErrorEnabled: Boolean = false
  override def getName: String = ""
  override def isInfoEnabled: Boolean = false
  override def isDebugEnabled: Boolean = false
  override def error(message: String): Unit = ()
  override def error(message: String, throwable: Throwable): Unit = ()
  override def getChildLogger(name: String): PLogger = this
  override def getThreshold: Int = 0
  override def debug(message: String): Unit = ()
  override def debug(message: String, throwable: Throwable): Unit = ()
  override def fatalError(message: String): Unit = ()
  override def fatalError(message: String, throwable: Throwable): Unit = ()
  override def isWarnEnabled: Boolean = false
  override def isFatalErrorEnabled: Boolean = false
  override def info(message: String): Unit = ()
  override def info(message: String, throwable: Throwable): Unit = ()
}

def sbtPLogger(log: sbt.Logger) = new PLogger {
  override def warn(message: String): Unit = log.warn(message)
  override def warn(message: String, throwable: Throwable): Unit = log.warn(s"$message: $throwable")
  override def isErrorEnabled: Boolean = true
  override def getName: String = "sbt"
  override def isInfoEnabled: Boolean = true
  override def isDebugEnabled: Boolean = true
  override def error(message: String): Unit = log.error(message)
  override def error(message: String, throwable: Throwable): Unit = log.error(s"$message: $throwable")
  override def getChildLogger(name: String): PLogger = this
  override def getThreshold: Int = 0
  override def debug(message: String): Unit = log.debug(message)
  override def debug(message: String, throwable: Throwable): Unit = log.debug(s"$message: $throwable")
  override def fatalError(message: String): Unit = error(message)
  override def fatalError(message: String, throwable: Throwable): Unit = error(message, throwable)
  override def isWarnEnabled: Boolean = true
  override def isFatalErrorEnabled: Boolean = true
  override def info(message: String): Unit = log.info(message)
  override def info(message: String, throwable: Throwable): Unit = log.info(s"$message: $throwable")
}
