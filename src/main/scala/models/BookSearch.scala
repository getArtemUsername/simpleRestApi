package models

import java.sql.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  *
  */
case class BookSearch(title: Option[String] = None,
                      releaseDate: Option[Date] = None,
                      categoryId: Option[Long] = None,
                      author: Option[String] = None)

trait BookSearchJson extends SprayJsonSupport with DefaultJsonProtocol {

  import services.FormatService._

  implicit val bookSearchFormat = jsonFormat4(BookSearch.apply)
}
