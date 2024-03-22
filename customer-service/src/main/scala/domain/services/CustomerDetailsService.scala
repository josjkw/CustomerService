package domain.services

import cats.data.OptionT
import domain.CustomerDetails
import domain.repositories.CustomerDetailsRepository

class CustomerDetailsService[F[_]](executionPriority: Int, repository: CustomerDetailsRepository[F]) {

  val getExecutionPriority: Int                                               = executionPriority
  def getCustomerDetails(id: String): OptionT[F, CustomerDetails] = repository.getDetails(id)

}
