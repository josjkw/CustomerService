package domain.services

import cats.MonadThrow
import cats.data.OptionT
import cats.syntax.all._
import domain.repositories.CustomerRepository.CustomerRepository
import domain.{Customer, CustomerDetails, CustomerId, CustomerWithDetails}

class CustomerService[F[_]: MonadThrow](repository: CustomerRepository[F])(
    customerDetailsServices: CustomerDetailsService[F]*
) {
  def create(customer: Customer): F[Unit]            = repository.add(customer)
  def createBatch(customers: Set[Customer]): F[Unit] = repository.addBatch(customers)

  // not really sure of the pattern but the idea is to be be able to unplug the legacy customer details services by
  // just not injecting it
  // Also not very sure of how to deal with OptionT when you actually need to match for the value inside
  def get(id: CustomerId): OptionT[F, CustomerWithDetails] = for {
    customer <- repository.get(id)
    customerWithDetails <- OptionT.liftF(
      getCustomerDetails(id.value, customerDetailsServices.toList.sortBy(_.getExecutionPriority)).value.map {
        case Some(customerDetails) => CustomerWithDetails(customer.id, customer.name, Some(customerDetails))
        case None                  => CustomerWithDetails(customer.id, customer.name, None)
      }
    )
  } yield customerWithDetails

  private def getCustomerDetails(
      id: String,
      customerDetailsServices: List[CustomerDetailsService[F]],
  ): OptionT[F, CustomerDetails] =
    customerDetailsServices match {
      case ::(customerDetailsService, next) =>
        for {
          res <- customerDetailsService.getCustomerDetails(id).orElse(getCustomerDetails(id, next))
        } yield res
      case Nil => OptionT.none
    }

}
