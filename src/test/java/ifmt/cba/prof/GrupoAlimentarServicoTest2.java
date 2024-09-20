package ifmt.cba.prof;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class GrupoAlimentarServicoTest2 {
    
    @Test
    public void testConsultarPorCodigoVerificandoValores1(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/grupoalimentar/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("nome", Matchers.is("Carboidrato"))
        ;
    }

    @Test
    public void testConsultarPorCodigoVerificandoValores2(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/grupoalimentar/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.greaterThan(0))
            .body("nome", Matchers.not(Matchers.emptyString()));
    }

    @Test
    public void testConsultarPorCodigoVerificandoValores3(){
        Response resposta = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/1");

        Assertions.assertEquals(Integer.valueOf(1), resposta.path("codigo"));
        Assertions.assertEquals("Carboidrato", resposta.path("nome"));
    }

    @Test
    public void testConsultarPorCodigoVerificandoValores4(){
        Response resposta = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/1");

        JsonPath jsonPath = new JsonPath(resposta.asString());

        Assertions.assertEquals(1, jsonPath.getInt("codigo"));
        Assertions.assertEquals("Carboidrato", jsonPath.getString("nome"));
    }

    @Test
    public void testConsultarPorNomeVerificandoValores(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/grupoalimentar/nome/Carbo")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(1))
            .body("nome[0]", Matchers.is("Carboidrato"));
    }
}
