package ifmt.cba.prof;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public class GrupoAlimentarServicoTest1 {
    
    @Test
    public void testConsultarPorCodigo1(){
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testConsultarPorCodigo2(){

        ValidatableResponse validacao = RestAssured.get("http://localhost:8080/grupoalimentar/codigo/1").then();
        validacao.statusCode(200);
    }

    @Test
    public void testConsultarPorCodigo3(){

        RestAssured.get("http://localhost:8080/grupoalimentar/codigo/1")
        .then()
        .statusCode(200);
    }

    @Test
    public void testConsultarPorCodigo4(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/grupoalimentar/codigo/1")
        .then()
            .statusCode(200);
    }
}
