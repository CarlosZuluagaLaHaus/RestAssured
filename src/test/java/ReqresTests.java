import static io.restassured.RestAssured.filters;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;

import Models.post.RequestRegisterUser;
import Models.post.ResponsePostCreate;
import Models.post.ResponseRegisterUser;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class ReqresTests {

  @Before
  public void setup() {
    RestAssured.baseURI = "https://reqres.in";
    RestAssured.basePath = "/api";
    filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new ErrorLoggingFilter(), new AllureRestAssured());
    RestAssured.requestSpecification = new RequestSpecBuilder().setContentType(ContentType.JSON)
        .build();
  }


  @Test
  public void postLoguinTest() {
    given()
        .body("{\n"
            + "    \"email\": \"eve.holt@reqres.in\",\n"
            + "    \"password\": \"cityslicka\"\n"
            + "}")
        .post("login")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("token", notNullValue())
        .body("token", equalTo("QpwL5tke4Pnpja7X4"));

  }

  @Test
  public void GetSingleUserTest() {
    given()
        .get("users/2")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("data.id", equalTo(2))
        .body("data.first_name", equalToIgnoringCase("janet"))
        .body("support.text", equalToIgnoringCase(
            "To keep ReqRes free, contributions towards server costs are appreciated!"));
  }

  @Test
  public void postCreateTest() {
    given()
        .body("{\n"
            + "    \"name\": \"Carlos\",\n"
            + "    \"job\": \"Automatizador\"\n"
            + "}")
        .post("users")
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body("name", equalTo("Carlos"))
        .body("job", equalTo("Automatizador"));
  }

  @Test
  public void deleteUserTest() {
    given()
        .delete("users/2")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void patchUserTest() {
    String nombreActualizado = given()
        .when()
        .body("{\n"
            + "    \"name\": \"Carlos\",\n"
            + "    \"job\": \"Automatizador\"\n"
            + "}")
        .patch("users/2")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract().jsonPath().getString("name");

    assertThat(nombreActualizado, equalTo("Carlos"));
  }

  @Test
  public void putUserTest() {
    String trabajoActualizado = given()
        .when()
        .body("{\n"
            + "    \"name\": \"Carlos\",\n"
            + "    \"job\": \"Automatizador\"\n"
            + "}")
        .put("users/2")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract().jsonPath().getString("job");

    assertThat(trabajoActualizado, equalTo("Automatizador"));
  }

  @Test
  public void getListUsers() {
    Response response = given()
        .get("users?page=2");

    Headers headers = response.getHeaders();
    int statusCode = response.statusCode();
    String body = response.getBody().asString();
    String contentType = response.getContentType();

    assertThat(statusCode, equalTo(HttpStatus.SC_OK));
    System.out.println("Body: " + body);
    System.out.println("ContentType: " + contentType);
    System.out.println("Headers: " + headers.toString());

    System.out.println(headers.get("Content-Type"));

  }

  @Test
  public void getAllUsers() {

    String response = given()
        .when()
        .get("users?page=2")
        .then()
        .extract()
        .body()
        .asString();

    int page = from(response).get("page");
    int totalPages = from(response).get("total_pages");
    int idFirstUser = from(response).get("data[0].id");

    System.out.println("Page: " + page);
    System.out.println("Total_Pages: " + totalPages);
    System.out.println("Id First User" + idFirstUser);

    List<Map> usersWhitIdGreaterThan10 = from(response).get("data.findAll {user -> user.id > 10}");
    String emailFirstUserId = usersWhitIdGreaterThan10.get(0).get("email").toString();

    System.out.println("email: " + emailFirstUserId);

    List<Map> user = from(response)
        .get("data.findAll { user -> user.id > 10 && user.last_name == 'Howell'}");
    int id = Integer.valueOf(user.get(0).get("id").toString());

    System.out.println("Id: " + id);
  }

  @Test
  public void postCreateWhitPojoTest() {
    String response =
        given()
            .when()
            .body("{\n"
                + "    \"name\": \"Carlos\",\n"
                + "    \"job\": \"Automatizador\"\n"
                + "}")
            .post("users")
            .then().extract().body().asString();

    ResponsePostCreate responsePostCreate = from(response).getObject("", ResponsePostCreate.class);
    System.out.println(responsePostCreate.getId());
    System.out.println(responsePostCreate.getName());
    System.out.println(responsePostCreate.getCreatedAt());
  }


  @Test
  public void postRegisterUser() {
    RequestRegisterUser requestRegisterUser = new RequestRegisterUser();
    requestRegisterUser.setEmail("eve.holt@reqres.in");
    requestRegisterUser.setPassword("pistol");

    ResponseRegisterUser responseRegisterUser =
        given()
        .when()
        .body(requestRegisterUser)
        .post("register")
        .then()
        .extract()
        .response()
        .as(ResponseRegisterUser.class);

    assertThat(responseRegisterUser.getId(), equalTo(4));
    assertThat(responseRegisterUser.getToken(), equalToIgnoringCase("qpwl5tke4pnpja7x4"));

  }

}


