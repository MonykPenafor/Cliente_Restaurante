package ifmt.cba;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.EntregadorDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class EntregadorServicoTest {

    String dataFormatada = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
    .create();

    //#region CONSULTAS

    @Test
    public void testConsultarPorCodigoVerificandoValores(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/entregador/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("nome", Matchers.is("Entregador 01"))
            .body("telefone", Matchers.is("65 99999-7070"))
            .body("CPF", Matchers.is("234.432.567-12"))
            .body("RG", Matchers.is("456789-1"));
    }

    @Test
    public void testConsultarPorNomeVerificandoValores(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/entregador/nome/Entre")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.greaterThan(0))
            .body("nome[0]", Matchers.is("Entregador 01"))
            .body("nome[1]", Matchers.is("Entregador 02"));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){
        
        EntregadorDTO entregadorDTO = new EntregadorDTO();
        entregadorDTO.setNome("Teste add: " + dataFormatada);
        entregadorDTO.setCPF("Teste add CPF" + dataFormatada);
        entregadorDTO.setRG("Teste add RG" + dataFormatada);
        entregadorDTO.setTelefone("Teste add tel");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(entregadorDTO)
            .when()
                .post("http://localhost:8080/entregador/")
            .then()
                .log().all()
                .statusCode(200)
                .body("nome", Matchers.is("Teste add: " + dataFormatada))
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("CPF", Matchers.is("Teste add CPF" + dataFormatada))
                .body("RG", Matchers.is("Teste add RG" + dataFormatada))
                .body("telefone", Matchers.is("Teste add tel"));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/entregador/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nome invalidoRG invalidoCPF invalidoTelefone invalido"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){
                
        EntregadorDTO entregadorDTO = new EntregadorDTO();
        entregadorDTO.setNome("Alterar: " + dataFormatada);
        entregadorDTO.setCPF("Alterar CPF " + dataFormatada);
        entregadorDTO.setRG("Alterar RG " + dataFormatada);
        entregadorDTO.setTelefone("Teste add tel");

        Response response1 = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(entregadorDTO)
        .when()
            .post("http://localhost:8080/entregador/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        EntregadorDTO entregadorDTO2 = gson.fromJson(response1.getBody().asString(), EntregadorDTO.class);

        entregadorDTO2.setNome("Alterado: " + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(entregadorDTO2)
            .when()
                .put("http://localhost:8080/entregador/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("nome", Matchers.is("Alterado: " + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){
                
        EntregadorDTO entregadorDTO = new EntregadorDTO();
        entregadorDTO.setNome("Excluir: " + dataFormatada);
        entregadorDTO.setCPF("Excluir CPF " + dataFormatada);
        entregadorDTO.setRG("Excluir RG " + dataFormatada);
        entregadorDTO.setTelefone("Teste add tel");

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(entregadorDTO)
        .when()
            .post("http://localhost:8080/entregador/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        EntregadorDTO entregadorDTO2 = gson.fromJson(response.getBody().asString(), EntregadorDTO.class);

        var id = entregadorDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/entregador/" + id)
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
            .delete("http://localhost:8080/entregador/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nao existe esse entregador"));
    }

    //#endregion
}
