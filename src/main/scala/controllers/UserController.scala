package controllers

import akka.http.scaladsl.model.StatusCodes
import models.{User, UserJson}
import repository.UserRepository
import akka.http.scaladsl.server.Directives._

/**
  *
  */
class UserController(val userRepository: UserRepository) extends UserJson {

  val routes = pathPrefix("users") {
    pathEndOrSingleSlash {
      post {
        decodeRequest {
          entity(as[User]) { user ⇒
            onSuccess(userRepository.findByEmail(user.email)) {
              case Some(_) ⇒ complete(StatusCodes.BadRequest, "Email already exists.")
              case None ⇒ complete(StatusCodes.Created, userRepository.create(user))
            }
          }
        }
      }
    } ~ pathPrefix(IntNumber) {
      id ⇒
        pathEndOrSingleSlash {
          get {
            onSuccess(userRepository.findById(id)) {
              case Some(user) ⇒ complete(user)
              case _ ⇒ complete(StatusCodes.NotFound)
            }
          }
        }
    }
  }

}
