package repositories

import models.User
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import repository.UserRepository
import services.{ConfigService, FlywayService, PostgresService}
import com.github.t3hnar.bcrypt._

/**
  *
  */
class UserRepositorySpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val userRepository = new UserRepository(databaseService)

  val user = User(None, "Name", "email", "password")

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
  }

  override def afterAll: Unit = {
    flywayService.dropDatabase
  }

  "A UserRepository" must {
    "be empty at the beginning" in {
      userRepository.all map { cs ⇒ cs.size mustBe 0 }
    }

    "create valid users" in {
      for {
        u ← userRepository.create(user)
        _ ← userRepository.delete(u.id.get)
      }
        yield {
          u.id mustBe defined
          user.password.isBcrypted(u.password) mustBe true
        }
    }
    "not find a category by email if it doesn't exist" in {
      userRepository.findByEmail("test@test.com") map { c ⇒ c must not be defined }
    }

    "find a user by id if it exists" in {
      for {
        u ← userRepository.create(user)
        Some(foundUser) ← userRepository.findById(u.id.get)
        _ ← userRepository.delete(u.id.get)
      } yield {
        u.id mustBe foundUser.id
      }
    }

    "delete a user by id if it exists" in {
      for {
        u ← userRepository.create(user)
        _ ← userRepository.delete(u.id.get)
        users ← userRepository.all
      } yield {
        users mustBe empty
      }
    }
  }

}
