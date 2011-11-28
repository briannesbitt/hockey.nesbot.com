package controllers

import play.api.mvc._
import models.Team
import ControllerHelper._

object MyNhl extends Controller {

  def index = Action {
    Ok(views.html.mynhl.index(Team.findAllteamNames))
  }

  def dataTable() = Action { request =>
    val teams = Team.findAll
    
    val w:Int = qsParseInt(request.queryString.get("w"), 2)
    val otw:Int = qsParseInt(request.queryString.get("otw"), 2)
    val sow:Int = qsParseInt(request.queryString.get("sow"), 2)
    val l:Int = qsParseInt(request.queryString.get("l"), 0)
    val otl:Int = qsParseInt(request.queryString.get("otl"), 1)
    val sol:Int = qsParseInt(request.queryString.get("sol"), 1)

    Ok(views.html.mynhl.dataTable(teams, qsParseString(request.queryString.get("id"), "rand"), w, otw, sow, l, otl, sol))
  }
}