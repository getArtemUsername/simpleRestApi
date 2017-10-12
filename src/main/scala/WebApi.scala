import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

/**
  *
  */
trait WebApi {
  implicit val system: ActorSystem

  implicit val materializer: ActorMaterializer

  implicit val executor: ExecutionContext
}
