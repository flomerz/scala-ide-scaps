package scaps.eclipse.util

import com.typesafe.scalalogging.StrictLogging

object Util extends StrictLogging {

  def logTime(logger: String => Any, message: String, function: => Any): Unit = {
    val time = System.currentTimeMillis
    function
    val took = System.currentTimeMillis - time
    val minutes = took / (1000 * 60)
    val seconds = (took / 1000) - (minutes * 60)
    val miliseconds = took - (seconds * 1000) - (minutes * 60)
    logger(message + " took: %sm %ss %sms".format(minutes, seconds, miliseconds))
  }

}
