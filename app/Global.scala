import com.nesbot.commons.datetime.Dater
import java.util.concurrent.{Executors, TimeUnit}
import jobs.StatsUpdater
import models.{MongoDB}
import play.api._

object Global extends GlobalSettings {
  val executor = Executors.newSingleThreadScheduledExecutor()

  override def beforeStart(app: Application) {
    MongoDB.init("mynhl").mapPackage("models").indexes
    executor.scheduleAtFixedRate(StatsUpdater, 0, Dater.secondsPerMinute*30, TimeUnit.SECONDS)
  }
  override def onStop(app: Application) {
    executor.shutdownNow
  }
}