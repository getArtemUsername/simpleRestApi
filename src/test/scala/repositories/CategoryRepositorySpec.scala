package repositories

import helpers.CategorySpecHelper
import models.Category
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import repository.CategoryRepository
import services.{ConfigService, FlywayService, PostgresService}

/**
  *
  */
class CategoryRepositorySpec extends AsyncWordSpec with MustMatchers
  with BeforeAndAfterAll with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)

  val categorySpecHelper = new CategorySpecHelper(categoryRepository)

  val category = Category(None, "Test category")

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
  }

  override def afterAll: Unit = {
    flywayService.dropDatabase
  }


  "A CategoryRepository" must {
    "be empty at the beginning" in {
      categoryRepository.all map { cs ⇒ cs.size mustBe 0 }
    }
  }

  "create valid categories" in {
    categorySpecHelper.createAndDelete() {
      c ⇒
        c.id mustBe defined
        categoryRepository.all map { cs ⇒ cs.size mustBe 1 }
    }
  }

  "not find a category by title if it doesn't exist" in {
    categoryRepository.findByTitle("not a valid title") map {
      c ⇒ c must not be defined
    }
  }

  "find a category by title if it exists" in {
    categorySpecHelper.createAndDelete() {
      c ⇒ categoryRepository.findByTitle(c.title) map { c ⇒ c mustBe defined }
    }
  }

  "delete a category by id if it exists" in {
    categoryRepository.create(category) flatMap {
      c ⇒
        categoryRepository.delete(c.id.get) flatMap {
          _ ⇒ categoryRepository.all map { cs ⇒ cs.size mustBe 0 }
        }
    }
  }
}
