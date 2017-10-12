package repositories

import java.sql.Date

import helpers.BookSpecHelper
import models.BookSearch
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import repository.{BookRepository, CategoryRepository}
import services.{ConfigService, FlywayService, PostgresService}

/**
  *
  */
class BookSearchSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {
  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)

  val bookRepository = new BookRepository(databaseService)

  val bookSpecHelper = new BookSpecHelper(categoryRepository)(bookRepository)


  override def beforeAll(): Unit = {
    flywayService.migrateDatabase
  }

  override def afterAll(): Unit = {

    flywayService.dropDatabase
  }

  "Performing a BookSearch" must {
    "return an empty list if there are no matchers" in {
      bookSpecHelper.bultInsertAndDelete {
        books ⇒
          val bookSearch = BookSearch(title = Some("Non existent book"))
          bookRepository.search(bookSearch).map { books ⇒ books.size mustBe 0 }
      }
    }

    "return the matching books by title" in {
      bookSpecHelper.bultInsertAndDelete {
        books ⇒
          val bookSearch = BookSearch(title = Some("Akka"))
          bookRepository.search(bookSearch).map {
            books ⇒
              books.size mustBe 1
              books.head.title mustBe bookSpecHelper.bookFields.head._1
          }

          val bookSearchMultiple = BookSearch(title = Some("The"))
          bookRepository.search(bookSearchMultiple).map {
            books ⇒
              books.size mustBe 1
          }
      }
    }

    "return the books by release date" in {
      bookSpecHelper.bultInsertAndDelete { books ⇒
        val bookSearch = BookSearch(releaseDate = Some(Date.valueOf("1993-01-01")))
        bookRepository.search(bookSearch).map {
          books ⇒
            books.size mustBe 1
            books.head.title mustBe bookSpecHelper.bookFields(1)._1
        }
      }
    }

    "return the books by category" in {
      bookSpecHelper.bultInsertAndDelete { books ⇒
        for {
          Some(category) ← categoryRepository.findByTitle(bookSpecHelper.sciFiCategory.title)
          books ← bookRepository.search(BookSearch(categoryId = category.id))
        } yield books.size mustBe 2
      }
    }

    "return the books by author" in {
      bookSpecHelper.bultInsertAndDelete { books ⇒
        val bookSearch = BookSearch(author = Some(". We"))
        bookRepository.search(bookSearch).map {
          books ⇒ books.size mustBe 1
        }
      }
    }

    "return the books by author and category" in {
      bookSpecHelper.bultInsertAndDelete { books ⇒
        for {
          Some(category) ← categoryRepository.findByTitle(bookSpecHelper.sciFiCategory.title)
          books ← bookRepository.search(BookSearch(author = Some(". We"), categoryId = category.id))
        } yield books.size mustBe 1
      }
    }
  }
}


