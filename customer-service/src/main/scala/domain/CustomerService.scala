package domain

import cats.{MonadThrow, Monoid}
import domain.CustomerRepository.CustomerRepository
import cats.syntax.all._

class CustomerService[F[_]: MonadThrow](repository: CustomerRepository[F], client: HttpClient[F]) {
  def create(customer: Customer): F[Unit]            = repository.add(customer)
  def createBatch(customers: Set[Customer]): F[Unit] = repository.addBatch(customers)

  def get(id: CustomerId): F[Option[CustomerWithDetails]] = for {
    customer <- repository.get(id)
    customerWithDetails <- customer.traverse(c =>
      client.getClientDetails(id.value).map(cd => CustomerWithDetails(c.id, c.name, cd))
    )
  } yield customerWithDetails

}
