package models

import java.sql.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.driver.PostgresDriver.api._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  *
  *
  */
case class Book(
                 id: Option[Long] = None,
                 title: String,
                 releaseDate: Date,
                 categoryId: Long,
                 quantity: Int,
                 author: String
               )

trait BookJson extends SprayJsonSupport with DefaultJsonProtocol {

  import services.FormatService._

  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat6(Book.apply)
}

trait BookTable {

  class Books(tag: Tag) extends Table[Book](tag, "books") {
    
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def releaseDate = column[Date]("release_date")

    def categoryId = column[Long]("category_id")

    def quantity = column[Int]("quantity")

    def author = column[String]("author")

    def * = (id, title, releaseDate, categoryId, quantity, author) <> ((Book.apply _).tupled, Book.unapply)
  }

  protected val books = TableQuery[Books]
}
