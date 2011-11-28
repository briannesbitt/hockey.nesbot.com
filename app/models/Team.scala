package models

import com.google.code.morphia.annotations.{Indexed, Entity}
import annotation.target.field
import java.lang.IllegalStateException
import collection.immutable.{TreeMap, SortedMap, HashMap}

@Entity(noClassnameStored = true, value = "teams")
case class Team (@(Indexed @field)(unique=true) name:String,
                 division:String,
                 var w:Int,
                 var l:Int,
                 var otw:Int,
                 var otl:Int,
                 var sow:Int,
                 var sol:Int) extends MongoModel[Team] {
  private def this() = this("", "", 0, 0, 0, 0, 0, 0)  // needed by morphia
  def this(name: String, division:String) = this(name, division, 0, 0, 0, 0, 0, 0)

  def points(wPts: Int = 2, lPts: Int = 0, otwPts: Int = 2, otlPts: Int = 1, sowPts: Int = 2, solPts: Int = 1) = (w * wPts) + (l * lPts) + (otw * otwPts) + (otl * otlPts) + (sow * sowPts) + (sol * solPts)
  def games = w + l + otw + otl + sow + sol
}

object Team extends MongoObject[Team] {
  val GamesPerSeason = 82
  val TeamsInLeague = 30

  def cleanName(name: String) = name.trim.stripPrefix(160.toChar.toString).replace('é', 'e').toLowerCase.split(" ").map(_.capitalize).mkString(" ").replace("Ny","NY")
  def existsByName(name: String) : Boolean = createQuery.field("name").equal(name).countAll() == 1
  def findByName(name: String) : Option[Team] = Option(createQuery.field("name").equal(name).get)
  def findPittsburg : Team = findByName("PIT").getOrElse(throw new IllegalStateException("Pittsburgh team needs to exist!!"))

  val TeamNameToUnique:Map[String,String] = HashMap(
  "Ottawa" -> "OTT","Toronto" -> "TOR","Buffalo" -> "BUF","Montreal" -> "MTL","Boston" -> "BOS",
  "Pittsburgh" -> "PIT","Philadelphia" -> "PHI","NY Rangers" -> "NYR","NY Islanders" -> "NYI","New Jersey" -> "NJD",
  "Florida" -> "FLA","Washington" -> "WSH","Tampa Bay" -> "TBL","Winnipeg" -> "WPG","Carolina" -> "CAR",
  "Chicago" -> "CHI","Detroit" -> "DET","St Louis" -> "STL","Nashville" -> "NSH","Columbus" -> "CBJ",
  "Vancouver" -> "VAN","Minnesota" -> "MIN","Edmonton" -> "EDM","Colorado" -> "COL","Calgary" -> "CGY",
  "Los Angeles" -> "LAK","San Jose" -> "SJS","Dallas" -> "DAL","Phoenix" -> "PHX","Anaheim" -> "ANA"
  )

  var SortedTeamNames = new TreeMap[String,String]
  TeamNameToUnique.map { case(k,v) =>
    SortedTeamNames += ((v, k))
  }

  def nameToUnique(name: String) = {
    TeamNameToUnique.getOrElse(Team.cleanName(name),throw new MatchError("nameToUnqiue... name not found (%s)".format(Team.cleanName(name))))
  }
  def findAllteamNames:SortedMap[String,String] = SortedTeamNames
}