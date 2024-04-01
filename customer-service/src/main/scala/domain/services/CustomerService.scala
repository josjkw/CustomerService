package domain.services

import cats.MonadThrow
import cats.data.OptionT
import cats.syntax.all._
import domain.repositories.CustomerRepository.CustomerRepository
import domain.{Customer, CustomerDetails, CustomerId, CustomerWithDetails}

class CustomerService[F[_]: MonadThrow](repository: CustomerRepository[F])(
    customerDetailsServices: CustomerDetailsService[F]
) {
  def create(customer: Customer): F[Unit]            = repository.add(customer)
  def createBatch(customers: Set[Customer]): F[Unit] = repository.addBatch(customers)

  def get(id: CustomerId): OptionT[F, CustomerWithDetails] = for {
    customer <- repository.get(id)
    customerWithDetails <- OptionT.liftF(customerDetailsServices.getCustomerDetails(id.value).value.map {
      case Some(customerDetails) => CustomerWithDetails(customer.id, customer.name, Some(customerDetails))
      case None                  => CustomerWithDetails(customer.id, customer.name, None)
    })
  } yield customerWithDetails

}
