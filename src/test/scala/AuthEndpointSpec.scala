import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import controllers.AuthController
import models._
import org.scalatest.{Assertion, AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import repository.{AuthRepository, UserRepository}
import services.{ConfigService, FlywayService, PostgresService, TokenService}

import scala.concurrent.Future

/**
  *
  */
class AuthEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with CredentialsJson
  with AuthJson {
  override implicit val executor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val authRepository = new AuthRepository(databaseService)
  val userRepository = new UserRepository(databaseService)

  val tokenService = new TokenService(userRepository)

  val authController = new AuthController(authRepository, tokenService)

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
  }

  override def afterAll: Unit = {
    flywayService.dropDatabase
  }

  "A UserEndpoint" must {
    "return an `Auth` when login is successful" in {
      val user = User(None, "Name", "email@email.com", "password")
      val credentials = Credentials(user.email, user.password)

      def assert(credentials: Credentials): Future[Assertion] = {
        Post("/auth", credentials) ~> authController.routes ~> check {
          status mustBe StatusCodes.OK

          val auth = responseAs[Auth]
          auth.user.email mustBe user.email
          tokenService.isTokenValidForMember(auth.token, auth.user).map(_ mustBe true)
        }
      }

      for {
        u ← userRepository.create(user)
        result ← assert(credentials)
        _ ← userRepository.delete(u.id.get)
      } yield result
    }

    "return an `Unauthorized` status code when login fails" in {
      val user = User(None, "Name", "email@email.com", "password")
      val credentials = Credentials(user.email, user.password)

      Post("/auth", credentials) ~> authController.routes ~> check {
        status mustBe StatusCodes.Unauthorized
      }
    }
  }
}
