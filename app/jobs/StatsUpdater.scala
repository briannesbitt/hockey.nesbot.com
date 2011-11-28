package jobs

import collection.mutable.HashMap
import java.lang.IllegalStateException
import models._
import org.jsoup.Jsoup
import com.nesbot.commons.datetime.Dater

object StatsUpdater extends Runnable {
  def run() {
    try {
      updateTeams
      updatePlayers
      updateCrosby
    }
    catch {
      case e: Exception => play.Logger.error("Exception caught : " + e.getMessage, e)
    }
  }

  def updateTeams {
    var doc = Jsoup.connect("http://www.nhl.com/ice/standings.htm?season=20112012&type=LEA").get
    var tds = doc.select("div.contentBlock table.data tbody tr td")

    val iName = 1
    val iDivision = 2
    val iWins = 4
    val iLoses = 5
    val iOtLoses = 6
    val iSo = 14
    var iCols = 17

    val teams = new HashMap[String, Team];

    Team.findAll.foreach { t =>
      teams += t.name -> t
    }

    if (tds.size != Team.TeamsInLeague * iCols )
    {
      throw new IllegalStateException("Wrong number of TDs for league standings (%d)".format(tds.size));
    }

    for (i <- 0 to Team.TeamsInLeague - 1) {
      val teamName = Team.nameToUnique(tds.get(iCols * i + iName).text)
      val teamDivision = tds.get(iCols * i + iDivision).text

      val team = teams.getOrElse(teamName, {val temp = new Team(teamName, teamDivision); teams += temp.name -> temp; temp;})

      team.w = tds.get(iCols * i + iWins).text.toInt
      team.l = tds.get(iCols * i + iLoses).text.toInt

      team.otl = tds.get(iCols * i + iOtLoses).text.toInt

      if (!tds.get(iCols * i + iSo).text.equals("-"))
      {
        val so = tds.get(iCols * i + iSo).text.split('-')
        team.sow = so(0).toInt
        team.sol = so(1).toInt
        team.w -= team.sow;
        team.otl -= team.sol;
      }
    }

    val iWinsOtAndSo = 9
    iCols = 21

    doc = Jsoup.connect("http://www.nhl.com/ice/teamstats.htm?fetchKey=20122ALLSAAALL&sort=otWins&viewName=overtimeRecords").get
    tds = doc.select("div.contentBlock table.data tbody tr td")

    if (tds.size != Team.TeamsInLeague * iCols )
    {
      throw new IllegalStateException("Wrong number of TDs for league OT standings (%d)".format(tds.size));
    }

    for (i <- 0 to Team.TeamsInLeague - 1) {

      val teamName = Team.nameToUnique(tds.get(iCols * i + iName).text)
      val team = teams.get(teamName).getOrElse(throw new IllegalArgumentException("Could not find team when parsing OT record (%s)".format(teamName)))

      team.otw = tds.get(iCols * i + iWinsOtAndSo).text.toInt - team.sow
      team.w -= team.otw
    }

    teams.foreach {e => e._2.save}

    play.Logger.info("StatsUpdater: updateTeams : " + Dater.now.toString)
  }

  def updatePlayers {

    val doc = Jsoup.connect("http://www.nhl.com/ice/playerstats.htm?fetchKey=20122ALLSASAll&sort=points&viewName=points").get
    val tds = doc.select("div.contentBlock table.data tbody tr td")

    val iPlayerName = 1
    val iPlayerTeam = 2
    val iPlayerGames = 4
    val iPlayerGoals = 5
    val iPlayerAssists = 6
    val iCols = 17

    for (i <- 0 to 9) {
      val teamName = tds.get(iCols * i + iPlayerTeam).text
      val team = Team.findByName(teamName).getOrElse(throw new IllegalArgumentException("Update players : Team not found : (%s)".format(teamName)))

      val name = tds.get(iCols * i + iPlayerName).text

      val player = Player.findByName(name).getOrElse(new Player(name, teamName, Team.findPittsburg.games))

      player.teamGames = team.games
      player.games = tds.get(iCols * i + iPlayerGames).text.toInt
      player.goals = tds.get(iCols * i + iPlayerGoals).text.toInt
      player.assists = tds.get(iCols * i + iPlayerAssists).text.toInt
      player.save
    }

    play.Logger.info("StatsUpdater: players Updated : " + Dater.now.toString)
  }

  def updateCrosby {
    val crosby = Player.findCrosby
    crosby.teamGames = Team.findPittsburg.games

    val doc = Jsoup.connect("http://penguins.nhl.com/club/player.htm?id=8471675").get
    var e = doc.select("#wideCol .playerpage table.data tbody tr.rwEven td.left").first().getElementsContainingText("Regular Season").first.nextElementSibling

    crosby.games = e.text.toInt
    e = e.nextElementSibling
    crosby.goals = e.text.toInt
    e = e.nextElementSibling
    crosby.assists = e.text.toInt
    crosby.save

    play.Logger.info("StatsUpdater: crosbyUpdated : " + Dater.now.toString)
  }
}