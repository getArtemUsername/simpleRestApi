import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete ⇒ resultedTo}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import controllers.CategoryController
import helpers.CategorySpecHelper
import models.{Category, CategoryJson}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import repository.CategoryRepository
import services.{ConfigService, FlywayService, PostgresService}

import scala.concurrent.ExecutionContextExecutor

/**
  *
  */
class CategoryEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with CategoryJson {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)

  val categorySpecHelper = new CategorySpecHelper(categoryRepository)

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
  }

  override def afterAll: Unit = {
    flywayService.dropDatabase
  }


  val categoryController = new CategoryController(categoryRepository)

  "A CategoryEndpoint" must {
    "return an empty list at the beginning" in {

      Get("/categories/") ~> categoryController.routes ~> check {
        status mustBe StatusCodes.OK

        val categories = responseAs[List[Category]]
        categories must have size 0
      }
    }

    "return all the categories when there is at least one" in {
      categorySpecHelper.createAndDelete() { c ⇒
        Get("/categories/") ~> categoryController.routes ~> check {
          status mustBe StatusCodes.OK
          val categories = responseAs[List[Category]]
          categories must have size 1
        }
      }
    }

    "return BadRequest with repeated titles" in {
      categorySpecHelper.createAndDelete() { c ⇒
        Post("/categories/", categorySpecHelper.category) ~> categoryController.routes ~> check {
          status mustBe StatusCodes.BadRequest
        }
      }
    }

    "create a category" in {
      Post("/categories/", categorySpecHelper.category) ~> categoryController.routes ~> check {
        status mustBe StatusCodes.Created

        val category = responseAs[Category]

        categoryRepository.delete(category.id.get)
        category.id mustBe defined
        category.title mustBe categorySpecHelper.category.title
      }
    }

    "return NotFound when try to delete a non existent category" in {
      Delete("/categories/10/") ~> categoryController.routes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "return NoContent when we delete an existent category" in {
      categoryRepository.create(categorySpecHelper.category) flatMap {
        c ⇒
          Delete(s"/categories/${c.id.get}/") ~> categoryController.routes ~> check {
            status mustBe StatusCodes.NoContent
          }
      }
    }
  }
}
