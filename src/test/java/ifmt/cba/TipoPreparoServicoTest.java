package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.TipoPreparoDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TipoPreparoServicoTest {
    
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
            .get("http://localhost:8080/tipopreparo/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("descricao", Matchers.is("Cozimento em agua"));
    }

    @Test
    public void testConsultarPorNomeVerificandoValores(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/tipopreparo/nome/Assado")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.greaterThan(0))
            .body("descricao[0]", Matchers.is("Assado no forno"));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){

        TipoPreparoDTO tipoPreparoDTO = new TipoPreparoDTO();
        tipoPreparoDTO.setDescricao("Incluir " + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(tipoPreparoDTO)
            .when()
                .post("http://localhost:8080/tipopreparo/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("descricao", Matchers.is("Incluir " + dataFormatada));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/tipopreparo/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Descricao invalida"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){
        
        TipoPreparoDTO tipoPreparoDTO = new TipoPreparoDTO();
        tipoPreparoDTO.setDescricao("Alterar" + dataFormatada);

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(tipoPreparoDTO)
        .when()
            .post("http://localhost:8080/tipopreparo/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        TipoPreparoDTO tipoPreparoDTO2 = gson.fromJson(response.getBody().asString(), TipoPreparoDTO.class);

        tipoPreparoDTO2.setDescricao("Alterado" + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(tipoPreparoDTO2)
            .when()
                .put("http://localhost:8080/tipopreparo/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("descricao", Matchers.is("Alterado" + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){

        TipoPreparoDTO tipoPreparoDTO = new TipoPreparoDTO();
        tipoPreparoDTO.setDescricao("Excluir " + dataFormatada);

        Response response = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(tipoPreparoDTO)
        .when()
            .post("http://localhost:8080/tipopreparo/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        TipoPreparoDTO tipoPreparoDTO2 = gson.fromJson(response.getBody().asString(), TipoPreparoDTO.class);

        var id = tipoPreparoDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/tipopreparo/" + id)
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
            .delete("http://localhost:8080/tipopreparo/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Tipo de Preparo nao existe"));
    }

    //#endregion
}
