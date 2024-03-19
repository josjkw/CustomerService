package gateway.transformers

import domain.{Customer, CustomerId, CustomerName}
import gateway.models.CustomerApi.{CustomerApiInput, CustomerApiOutput}
import io.scalaland.chimney.Transformer

object CustomerTransformers {

  implicit val customerToCustomerApiOutput: Transformer[Customer, CustomerApiOutput] = (customer: Customer) =>
    CustomerApiOutput(customer.id.value, customer.name.value)

  implicit val customerApiInputToCustomer: Transformer[CustomerApiInput, Customer] =
    (customerApiInput: CustomerApiInput) =>
      Customer(CustomerId(customerApiInput.id), CustomerName(customerApiInput.name))

}
