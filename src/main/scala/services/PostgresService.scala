package services

import slick.driver.PostgresDriver

/**
  *
  */
class PostgresService(jdbcUrl: String, dbUser: String, dbPassword: String) extends DatabaseService {

  override val driver = PostgresDriver

  override val db = PostgresDriver.api.Database.forURL(jdbcUrl, dbUser, dbPassword)

  db.createSession()

}
