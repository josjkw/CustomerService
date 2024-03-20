package gateway.transformers

import domain.{Customer, CustomerDetails, CustomerId, CustomerName, CustomerWithDetails}
import gateway.models.GatewayModels.{CustomerApiInput, CustomerApiOutput, CustomerWithDetailsApiOutput}
import io.scalaland.chimney.Transformer

object CustomerTransformers {

  implicit val customerToCustomerApiOutput: Transformer[Customer, CustomerApiOutput] = (customer: Customer) =>
    CustomerApiOutput(customer.id.value, customer.name.value)

  implicit val customerWithDetailsToCustomerWithDetailsApiOutput
      : Transformer[CustomerWithDetails, CustomerWithDetailsApiOutput] = (cwd: CustomerWithDetails) =>
    CustomerWithDetailsApiOutput(cwd.id.value, cwd.name.value, cwd.data.data)

  implicit val customerApiInputToCustomer: Transformer[CustomerApiInput, Customer] =
    (customerApiInput: CustomerApiInput) =>
      Customer(CustomerId(customerApiInput.id), CustomerName(customerApiInput.name))

}
