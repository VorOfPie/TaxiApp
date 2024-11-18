package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return 404 when passenger with ID 999 is not found"

    request {
        method GET()
        url '/api/v1/passengers/999'
    }

    response {
        status NOT_FOUND()
        headers {
            contentType(applicationJson())
        }
        body(
                status: 404,
                message: "Passenger with id 999 not found.",
                timestamp: $(regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,9}"))
        )
    }

}
