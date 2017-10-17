package repositories

import models.{Credentials, User}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import repository.{AuthRepository, UserRepository}
import services.{ConfigService, FlywayService, PostgresService}
import com.github.t3hnar.bcrypt._

/**
  *
  */
class AuthRepositorySpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val userRepository = new UserRepository(databaseService)
  val authRepository = new AuthRepository(databaseService)

  val user = User(None, "Name", "email", "password")

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
  }

  override def afterAll: Unit = {
    flywayService.dropDatabase
  }

  "An AuthRepository" must {
    "return `None` when there are no users" in {
      for {
        foundUser ← authRepository.findByCredentials(Credentials(user.email, user.password))
      } yield {
        foundUser must not be defined
      }
    }

    "return `None` when the `Credentials` does not match correctly" in {
      for {
        u ← userRepository.create(user)
        foundUser ← authRepository.findByCredentials(Credentials(user.email, "wrong password"))
        _ ← userRepository.delete(u.id.get)
      } yield {
        foundUser must not be defined
      }
    }

    "return `None` where `Credentials` does matches correctly" in {
      for {
        _ ← userRepository.create(user)
        Some(foundUser) ← authRepository.findByCredentials(Credentials(user.email, user.password))
        _ ← userRepository.delete(foundUser.id.get)
      } yield {
        foundUser.email mustBe user.email
        user.password.isBcrypted(foundUser.password) mustBe true
      }
    }
  }

}
