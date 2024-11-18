package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return details of driver with ID 1"

    request {
        method GET()
        url '/api/v1/drivers/1'
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                id: 1,
                firstName: "John",
                lastName: "Doe",
                phone: "123-456-7890",
                gender: "Male",
                cars: [
                        [
                                id: 1,
                                brand: "Toyota",
                                color: "Red",
                                licensePlate: "ABC123"
                        ]
                ]
        )
    }
}
