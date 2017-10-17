package repository

import models.{User, UserTable}
import services.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class UserRepository(val databaseService: DatabaseService)(implicit executor: ExecutionContext) extends UserTable {

  import databaseService._
  import databaseService.driver.api._
  import com.github.t3hnar.bcrypt._

  def all: Future[Seq[User]] = db.run(users.result)

  def create(user: User): Future[User] = {
    val secureUser = user.copy(password = user.password.bcrypt)
    db.run(users returning users += secureUser)
  }

  def findById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  def findByEmail(email: String): Future[Option[User]] = db.run(users.filter(_.email === email).result.headOption)

  def delete(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)
}
