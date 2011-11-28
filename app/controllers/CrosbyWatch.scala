package controllers

import play.api.mvc._
import models.Player

object CrosbyWatch extends Controller {

  def index = Action {
    val players = Player.findAllOrderByProjectedPoints
    val winner = players.head
    Ok(views.html.crosbywatch.index(players, winner.name.equals(Player.CrosbyName), winner.name))
  }
}