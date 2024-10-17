package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return details of passenger with ID 1"

    request {
        method GET()
        url '/api/v1/passengers/1'
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
                email: "john.doe@example.com",
                phone: "+12345678901"
        )
    }
}
