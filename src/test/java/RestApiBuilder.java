import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

public class RestApiBuilder {
    private final String username = "admin";
    private final String psw = "secret123";
    private final String baseUri = "http://localhost:8080";


    RequestSpecification requestSpecification = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setAuth(RestAssured.basic(username, psw))
                .log(LogDetail.ALL)
                .build();
}
