package domain.services

import cats.MonadThrow
import cats.data.OptionT
import cats.syntax.all._
import domain.repositories.CustomerRepository.CustomerRepository
import domain.{Customer, CustomerId, CustomerWithDetails}

class CustomerService[F[_]: MonadThrow](repository: CustomerRepository[F])(
    customerDetailsService: CustomerDetailsService[F]
) {
  def create(customer: Customer): F[Unit]            = repository.add(customer)
  def createBatch(customers: Set[Customer]): F[Unit] = repository.addBatch(customers)

  def get(id: CustomerId): OptionT[F, CustomerWithDetails] = for {
    customer        <- repository.get(id)
    customerDetails <- customerDetailsService.getCustomerDetails(id.value)
  } yield CustomerWithDetails(customer.id, customer.name, customerDetails)

}
