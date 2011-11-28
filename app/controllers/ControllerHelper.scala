package controllers

object ControllerHelper {

  def qsParseString(vals:Option[Seq[String]]):Option[String] = vals.flatMap(_.headOption)
  def qsParseString(vals:Option[Seq[String]], default:String):String = qsParseString(vals).getOrElse(default)

  def qsParseInt(vals:Option[Seq[String]]):Option[Int] = {
    vals.flatMap(_.headOption).isEmpty

    val s = vals.flatMap(_.headOption)

    try
    {
      if (s.isEmpty) None else Some(s.get.toInt)
    }
    catch {
      case e: NumberFormatException => None
    }
  }
  def qsParseInt(vals:Option[Seq[String]], default:Int):Int = qsParseInt(vals).getOrElse(default)
}