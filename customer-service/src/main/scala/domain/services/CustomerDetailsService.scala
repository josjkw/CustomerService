package domain.services

import cats.data.OptionT
import domain.{CustomerDetails, CustomerId}
import domain.repositories.CustomerDetailsRepository

class CustomerDetailsService[F[_]](repository: CustomerDetailsRepository[F]) {
  def getCustomerDetails(id: String): OptionT[F, CustomerDetails] = {
    val _ = println("je suis la")
    repository.getDetails(id)
  }
}
