package infrastructure.repositories

import cats.data.OptionT
import cats.{Monad, MonadThrow}
import cats.implicits.{catsSyntaxApplicativeError, toFlatMapOps, toTraverseOps}
import domain.repositories.CustomerRepository.CustomerRepository
import domain.{Customer, CustomerId}

class InMemoryCustomerRepository[F[_]: MonadThrow](var inMemoryRepo: Map[CustomerId, Customer])
    extends CustomerRepository[F] {

  override def add(customer: Customer): F[Unit] = {
    if (inMemoryRepo.contains(customer.id)) {
      MonadThrow[F].raiseError(new Exception(s"Customer ${customer.name} already exists"))
    } else {
      inMemoryRepo = inMemoryRepo + (customer.id -> customer)
      MonadThrow[F].unit
    }

  }

  override def get(customerId: CustomerId): OptionT[F, Customer] = OptionT.fromOption(inMemoryRepo.get(customerId))

  override def addBatch(customers: Set[Customer]): F[Unit] = {
    customers.toList.traverse { add(_).attempt }.flatMap { results =>
      results.collect { case Left(error) => error } match {
        case Nil    => MonadThrow[F].unit
        case errors => MonadThrow[F].raiseError(new Exception(errors.map(_.getMessage).mkString("; ")))
      }
    }
  }

}
