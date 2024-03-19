package infrastructure.repositories

import cats.MonadThrow
import cats.implicits.{catsSyntaxApplicativeError, toFlatMapOps, toTraverseOps}
import domain.CustomerRepository.CustomerRepository
import domain.{Customer, CustomerId}

class InMemoryCustomerRepository[F[_]: MonadThrow] extends CustomerRepository[F] {

  private var inMemoryRepo: Map[CustomerId, Customer] = Map()

  override def add(customer: Customer): F[Unit] = {
    if (inMemoryRepo.contains(customer.id)) {
      MonadThrow[F].raiseError(new Exception(s"Customer ${customer.name} already exists"))
    } else {
      inMemoryRepo = inMemoryRepo + (customer.id -> customer)
      MonadThrow[F].unit
    }

  }

  override def get(customerId: CustomerId): F[Option[Customer]] = MonadThrow[F].pure(inMemoryRepo.get(customerId))

  override def addBatch(customers: Set[Customer]): F[Unit] = {
    customers.toList.traverse { add(_).attempt }.flatMap { results =>
      results.collect { case Left(error) => error } match {
        case Nil    => MonadThrow[F].unit
        case errors => MonadThrow[F].raiseError(new Exception(errors.map(_.getMessage).mkString("; ")))
      }
    }
  }

}
