package models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import slick.driver.PostgresDriver.api._

/**
  *
  */
case class User(id: Option[Long] = None, name: String, email: String, password: String)

trait UserJson extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User.apply)
}

trait UserTable {

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def email = column[String]("email")

    def password = column[String]("password")


    def * = (id, name, email, password) <> ((User.apply _).tupled, User.unapply)
  }
  
  protected val users = TableQuery[Users]
}
