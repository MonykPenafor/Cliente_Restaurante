package ifmt.cba;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.ColaboradorDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ColaboradorServicoTest {
    
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
            .get("http://localhost:8080/colaborador/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("nome", Matchers.is("Colaborador 01"))
            .body("telefone", Matchers.is("65 99999-7070"))
            .body("CPF", Matchers.is("234.432.567-12"))
            .body("RG", Matchers.is("456789-1"));
    }

    @Test
    public void testConsultarPorNomeVerificandoValores(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/colaborador/nome/Colab")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.greaterThan(0))
            .body("nome[0]", Matchers.is("Colaborador 01"))
            .body("nome[1]", Matchers.is("Colaborador 02"));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){
        
        ColaboradorDTO colaboradorDTO = new ColaboradorDTO();
        colaboradorDTO.setNome("Teste add: " + dataFormatada);
        colaboradorDTO.setCPF("Teste add CPF" + dataFormatada);
        colaboradorDTO.setRG("Teste add RG" + dataFormatada);
        colaboradorDTO.setTelefone("Teste add tel");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(colaboradorDTO)
            .when()
                .post("http://localhost:8080/colaborador/")
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
            .post("http://localhost:8080/colaborador/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nome invalidoRG invalidoCPF invalidoTelefone invalido"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){

        ColaboradorDTO colaboradorDTO = new ColaboradorDTO();
        colaboradorDTO.setNome("Alterar: " + dataFormatada);
        colaboradorDTO.setCPF("Alterar CPF" + dataFormatada);
        colaboradorDTO.setRG("Alterar RG" + dataFormatada);
        colaboradorDTO.setTelefone("Teste add tel");

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(colaboradorDTO)
        .when()
            .post("http://localhost:8080/colaborador/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        ColaboradorDTO colaboradorDTO2 = gson.fromJson(response.getBody().asString(), ColaboradorDTO.class);

        colaboradorDTO2.setNome("Alterado: " + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(colaboradorDTO2)
            .when()
                .put("http://localhost:8080/colaborador/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("nome", Matchers.is("Alterado: " + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){

        ColaboradorDTO colaboradorDTO = new ColaboradorDTO();
        colaboradorDTO.setNome("Deletar: " + dataFormatada);
        colaboradorDTO.setCPF("Deletar CPF" + dataFormatada);
        colaboradorDTO.setRG("Deletar RG" + dataFormatada);
        colaboradorDTO.setTelefone("Teste add tel");

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(colaboradorDTO)
        .when()
            .post("http://localhost:8080/colaborador/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        ColaboradorDTO colaboradorDTO2 = gson.fromJson(response.getBody().asString(), ColaboradorDTO.class);

        var id = colaboradorDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/colaborador/" + id)
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
            .delete("http://localhost:8080/colaborador/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nao existe esse colaborador"));
    }

    //#endregion
}
