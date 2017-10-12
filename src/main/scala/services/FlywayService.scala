package services

import org.flywaydb.core.Flyway

/**
  *
  */
class FlywayService(jdbcUrl: String, dbUser: String, dbPassword: String) {
  private val flyway = new Flyway()
  flyway.setDataSource(jdbcUrl, dbUser, dbPassword)

  def migrateDatabase: FlywayService = {
    flyway.migrate()
    this
  }

  def dropDatabase: FlywayService = {
    flyway.clean()
    this
  }
}
