package controllers

import java.sql.Date

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.unmarshalling.{PredefinedFromStringUnmarshallers, Unmarshaller}
import models.{Book, BookJson, BookSearch}
import repository.BookRepository

/**
  *
  *
  */
class BookController(val bookRepository: BookRepository) extends BookJson with PredefinedFromStringUnmarshallers {
  implicit val dateFromStringUnmarshaller: Unmarshaller[String, Date] =
    Unmarshaller.strict[String, Date] {
      string ⇒ Date.valueOf(string)
    }

  val routes = pathPrefix("books") {
    pathEndOrSingleSlash {
      post {
        decodeRequest {
          entity(as[Book]) { book ⇒
            complete(StatusCodes.Created, bookRepository.create(book))
          }
        }
      } ~
        get {
          parameters(('title.?, 'releaseDate.as[Date].?, 'categoryId.as[Long].?, 'author.?)).as(BookSearch) {
            bookSearch ⇒
              complete {
                bookRepository.search(bookSearch)
              }
          }
        }
    } ~ pathPrefix(IntNumber) {
      id ⇒
        pathEndOrSingleSlash {
          get {
            complete {
              bookRepository.findById(id)
            }
          } ~
            delete {
              onSuccess(bookRepository.delete(id)) {
                case n if n > 0 ⇒ complete(StatusCodes.NoContent)
                case _ ⇒ complete(StatusCodes.NotFound)
              }
            }
        }
    }
  }
}
