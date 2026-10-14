import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;

import io.restassured.specification.ResponseSpecification;

public class RestApiAssert {

    public static ResponseSpecification successResponseSpec = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .expectStatusCode(200)
            .build();

    public static ResponseSpecification errorResponseSpec = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .expectStatusCode(400)
            .build();

    public static ResponseSpecification notFoundResponseSpec = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .expectStatusCode(404)
            .build();
}
