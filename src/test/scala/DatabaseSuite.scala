import org.scalatest.Sequential
import repositories.{AuthRepositorySpec, BookRepositorySpec, BookSearchSpec, CategoryRepositorySpec}

/**
  */
class DatabaseSuite extends Sequential(new CategoryRepositorySpec, new BookRepositorySpec,
  new AuthRepositorySpec, new BookSearchSpec, new BookEndpointSpec, new CategoryEndpointSpec,
  new UserEndpointSpec, new AuthEndpointSpec)
