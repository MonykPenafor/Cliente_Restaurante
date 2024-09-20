package ifmt.cba.prof;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.GrupoAlimentarDTO;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class GrupoAlimentarServicoTest3 {

    String dataFormatada = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    @Test
    public void testInclusaoComDadosCorretos(){

        GrupoAlimentarDTO grupoAlimentarDTO = new GrupoAlimentarDTO();
        grupoAlimentarDTO.setNome("Teste: " + dataFormatada);

        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(grupoAlimentarDTO)
        .when()
            .post("http://localhost:8080/grupoalimentar/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo",Matchers.is(Matchers.notNullValue()))
            .body("nome",Matchers.is("Teste: " + dataFormatada));
    }

    @Test
    public void testInclusaoComDadosIncorretos(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/grupoalimentar/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nome nao valido"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/6");
        Assertions.assertEquals(200, response.getStatusCode());

        Gson gson = new Gson();
        GrupoAlimentarDTO grupoAlimentarDTO = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        grupoAlimentarDTO.setNome("TesteAlteracao: " + dataFormatada);

        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(grupoAlimentarDTO)
        .when()
            .put("http://localhost:8080/grupoalimentar/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo",Matchers.is(Matchers.notNullValue()))
            .body("nome",Matchers.is("TesteAlteracao: " + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){
        GrupoAlimentarDTO grupoAlimentarDTO = new GrupoAlimentarDTO();
        grupoAlimentarDTO.setNome("Teste exclusão");

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(grupoAlimentarDTO)
        .when()
            .post("http://localhost:8080/grupoalimentar/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        Gson gson = new Gson();
        GrupoAlimentarDTO grupoAlimentarDTOTemp = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        var id = grupoAlimentarDTOTemp.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/grupoalimentar/" + id)
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
            .delete("http://localhost:8080/grupoalimentar/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Esse GrupoAlimentar nao existe"));
    }
}
