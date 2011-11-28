package models

import annotation.target.field
import com.google.code.morphia.annotations.{PrePersist, Property, Entity, Indexed}

@Entity(noClassnameStored = true, value = "players")
case class Player(@(Indexed @field)(unique=true) name:String,
                  team:String,
                  var teamGames:Int,
                  var games:Int,
                  var goals:Int,
                  var assists:Int) extends MongoModel[Player] {

  private def this() = this("", "", 0, 0, 0, 0)  // needed by morphia
  def this(name: String, team: String, teamGames: Int) = this(name, team, 0, 0, 0, 0)

  @Property("points")
  private var _points:Int = points
  @Property("projectedPoints")
  private var _projectedPoints:Int = points
  def points = {updatePoints; _points}
  def projectedPoints = {updatePoints; _projectedPoints}

  @PrePersist def updatePoints { _points = goals + assists; _projectedPoints = _points + ((_points.toFloat / games) * (Team.GamesPerSeason - teamGames)).toInt }

  def pointsPerGame = points.toDouble / games
}

object Player extends MongoObject[Player] {
  val CrosbyName = "Sidney Crosby"
  def findCrosby : Player = findByName(CrosbyName).getOrElse(new Player(CrosbyName, "PIT", Team.findPittsburg.games))
  def findByName(name: String) : Option[Player] = Option(createQuery.field("name").equal(name).get)
  def findNameStartsWith(prefix: String) : Option[Player] = Option(createQuery.field("name").startsWith(prefix).get)
  def findAllOrderByPoints = asList(createQuery.order("-points"))
  def findAllOrderByProjectedPoints = asList(createQuery.order("-projectedPoints"))
}