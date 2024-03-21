package domain.repositories

import cats.data.OptionT
import domain.{CustomerDetails, CustomerId}

trait CustomerDetailsRepository[F[_]] {
  def getDetails(id: String): OptionT[F, CustomerDetails]
}
