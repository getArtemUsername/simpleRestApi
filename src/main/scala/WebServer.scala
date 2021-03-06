import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import repository.{BookRepository, CategoryRepository, UserRepository}
import services._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

/**
  *
  * Description...
  *
  */
object WebServer extends App
  with ConfigService
  with WebApi {
  override implicit val system: ActorSystem = ActorSystem("Akka_HTTP_Bookstore")
  override implicit val executor: ExecutionContext = system.dispatcher
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabase

  val databaseService: DatabaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)
  val bookRepository = new BookRepository(databaseService)

  val userRepository = new UserRepository(databaseService)
  val tokenService = new TokenService(userRepository)

  val apiService = new ApiService(categoryRepository, bookRepository, tokenService)

  val bindingFuture = Http().bindAndHandle(apiService.routes, httpHost, httpPort)

  println(s"Server online at $httpHost:$httpPort/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())
}
