import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import controllers.BookController
import helpers.BookSpecHelper
import models.{Book, BookJson}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import repository.{BookRepository, CategoryRepository}
import services.{ConfigService, FlywayService, PostgresService}

import scala.concurrent.ExecutionContextExecutor

/**
  *
  */
class BookEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with BookJson {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)

  val bookRepository = new BookRepository(databaseService)

  val bookSpecHelper = new BookSpecHelper(categoryRepository)(bookRepository)

  val bookController = new BookController(bookRepository)

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
    bookSpecHelper.bulkInsert
  }

  override def afterAll: Unit = {
    bookSpecHelper.bulkDelete
    flywayService.dropDatabase
  }

  "A Book Endpoint" must {
    "create a book" in {
      categoryRepository.findByTitle("Sci-Fi") flatMap { c ⇒
        Post("/books/", bookSpecHelper.book(c.flatMap(_.id).get)) ~> bookController.routes ~> check {
          status mustBe StatusCodes.Created

          val book = responseAs[Book]

          for {
            _ ← bookRepository.delete(book.id.get)
          } yield {
            book.id mustBe defined
            book.title mustBe "Murder in Ganymede"
          }
        }
      }
    }

    "return NotFound when try to delete a non existent category" in {
      Delete("/books/10/") ~> bookController.routes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "return NoContent when we delete an existent category" in {
      categoryRepository.findByTitle("Sci-Fi") flatMap { c ⇒
        bookRepository.create(bookSpecHelper.book(c.flatMap(_.id).get)) flatMap { b ⇒
          Delete(s"/books/${b.id.get}/") ~> bookController.routes ~> check {
            categoryRepository.delete(c.flatMap(_.id).get)
            status mustBe StatusCodes.NoContent
          }
        }
      }
    }

    "return all books when no query parameters are sent" in {
      Get("/books/") ~> bookController.routes ~> check {
        status mustBe StatusCodes.OK

        val books = responseAs[List[Book]]

        books must have size bookSpecHelper.bookFields.size
      }
    }

    "return all books that conform to the query paramteres sent" in {
      Get("/books?title=in&author=Ray") ~> bookController.routes ~> check {
        status mustBe StatusCodes.OK

        val books = responseAs[List[Book]]

        books must have size 1
      }

      Get("/books?title=The") ~> bookController.routes ~> check {
        status mustBe StatusCodes.OK

        val books = responseAs[List[Book]]

        books must have size 1
      }
    }
  }
}
