package ifmt.cba;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.BairroDTO;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public class BairroServicoTest {
    
    String dataFormatada = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    
    Gson gson = new Gson();

    //#region CONSULTAS

    @Test
    public void testConsultarPorCodigo1(){
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/bairro/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testConsultarPorCodigo2(){

        ValidatableResponse validacao = RestAssured.get("http://localhost:8080/bairro/codigo/1").then();
        validacao.statusCode(200);
    }

    @Test
    public void testConsultarPorCodigo3(){

        RestAssured.get("http://localhost:8080/bairro/codigo/1")
        .then()
        .statusCode(200);
    }

    @Test
    public void testConsultarPorCodigo4(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/bairro/codigo/1")
        .then()
            .statusCode(200);
    }

    @Test
    public void testConsultarPorNomeVerificandoValoresEspecificos(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/bairro/nome/Centro")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(1))
            .body("custoEntrega[0]", Matchers.is(7.0F))
            .body("nome[0]", Matchers.is("Centro"))
        ;
    }

    @Test
    public void testConsultarPorCodigoVerificandoValoresEspecificos(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/bairro/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("custoEntrega", Matchers.is(7.0F))
            .body("nome", Matchers.is("Centro"))
        ;
    }

    @Test
    public void testConsultarPorCodigoVerificandoValores2(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/bairro/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.greaterThan(0))
            .body("custoEntrega", Matchers.greaterThan(0.0F))
            .body("nome", Matchers.not(Matchers.emptyString()));
    }

    @Test
    public void testConsultarPorCodigoVerificandoValores3(){
        Response resposta = RestAssured.request(Method.GET, "http://localhost:8080/bairro/codigo/1");

        Assertions.assertEquals(Integer.valueOf(1), resposta.path("codigo"));
        Assertions.assertEquals(Float.valueOf(7.0F), resposta.path("custoEntrega"));
        Assertions.assertEquals("Centro", resposta.path("nome"));
    }

    @Test
    public void testConsultarPorCodigoVerificandoValores4(){
        Response resposta = RestAssured.request(Method.GET, "http://localhost:8080/bairro/codigo/1");

        JsonPath jsonPath = new JsonPath(resposta.asString());

        Assertions.assertEquals(1, jsonPath.getInt("codigo"));
        Assertions.assertEquals(7.0F, jsonPath.getFloat("custoEntrega"));
        Assertions.assertEquals("Centro", jsonPath.getString("nome"));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){

        BairroDTO bairroDTO = new BairroDTO();
        bairroDTO.setNome("Inclusão: " + dataFormatada);
        bairroDTO.setCustoEntrega(5f);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(bairroDTO)
            .when()
                .post("http://localhost:8080/bairro/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("custoEntrega", Matchers.is(5.0f))
                .body("nome", Matchers.is("Inclusão: " + dataFormatada));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/bairro/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto", Matchers.is("Nome invalidoCusto entrega deve ser maior que zero"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){
        
        BairroDTO bairroDTO = new BairroDTO();
        bairroDTO.setNome("Alteração: " + dataFormatada);
        bairroDTO.setCustoEntrega(8.0f);

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(bairroDTO)
        .when()
            .post("http://localhost:8080/bairro/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        BairroDTO bairroDTO2 = gson.fromJson(response.getBody().asString(), BairroDTO.class);

        bairroDTO2.setCustoEntrega(5.0F);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(bairroDTO2)
            .when()
                .put("http://localhost:8080/bairro/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("nome", Matchers.is("Alteração: " + dataFormatada))
                .body("custoEntrega", Matchers.is(5.0f));
    }

    @Test
    public void testExclusaoComDadosCorretos(){
        
        BairroDTO bairroDTO = new BairroDTO();
        bairroDTO.setNome("Bairro teste exclusão");
        bairroDTO.setCustoEntrega(8.0f);

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(bairroDTO)
        .when()
            .post("http://localhost:8080/bairro/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        BairroDTO bairroDTO2 = gson.fromJson(response.getBody().asString(), BairroDTO.class);

        var id = bairroDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/bairro/" + id)
        .then()
            .log().all()
            .statusCode(204);
    }

    @Test
    public void testExclusaoComDadosInCorretos(){
        
        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/bairro/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nao existe esse bairro"));
    }

    //#endregion
}
