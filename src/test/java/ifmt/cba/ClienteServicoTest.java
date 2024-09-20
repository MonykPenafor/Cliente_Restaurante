package ifmt.cba;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.BairroDTO;
import ifmt.cba.dto.ClienteDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class ClienteServicoTest {
     
    String dataFormatada = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
    .create();

    //#region CONSULTAS

    @Test
    public void testConsultarPorCodigo(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/cliente/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("bairro.nome", Matchers.is("Centro"))
            .body("nome", Matchers.is("Cliente 01"));
    }

    @Test
    public void testConsultarPorNome(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/cliente/nome/Cliente")
        .then()
            .statusCode(200)
            .body("nome[0]", Matchers.is("Cliente 01"))
            .body("nome[1]", Matchers.is("Cliente 02"));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/bairro/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        BairroDTO bairroDTO = gson.fromJson(response.getBody().asString(), BairroDTO.class);

        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Inlcuir: " + dataFormatada);
        clienteDTO.setBairro(bairroDTO);
        clienteDTO.setCPF("Incluir cpf: " + dataFormatada);
        clienteDTO.setLogradouro("logradouro");
        clienteDTO.setNumero("numero");
        clienteDTO.setPontoReferencia("pontoReferencia");
        clienteDTO.setRG("1111111");
        clienteDTO.setTelefone("(65) 95623-8978");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(clienteDTO)
            .when()
                .post("http://localhost:8080/cliente/")
            .then()
                .log().all()
                .statusCode(200)
                .body("nome", Matchers.is("Inlcuir: " + dataFormatada))
                .body("bairro.codigo", Matchers.is(1))
                .body("CPF", Matchers.is("Incluir cpf: " + dataFormatada))
                .body("logradouro", Matchers.is("logradouro"))
                .body("numero", Matchers.is("numero"))
                .body("pontoReferencia", Matchers.is("pontoReferencia"))
                .body("telefone", Matchers.is("(65) 95623-8978"))
                .body("RG", Matchers.is("1111111"));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/cliente/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nome invalidoRG invalidoCPF invalidoTelefone invalidoLogradouro invalidoNumero invalidoBairro invalidoPonto de referencia invalido"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){
                
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/bairro/codigo/2");
        Assertions.assertEquals(200, response.getStatusCode());

        BairroDTO bairroDTO = gson.fromJson(response.getBody().asString(), BairroDTO.class);
        
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Alterar: " + dataFormatada);
        clienteDTO.setBairro(bairroDTO);
        clienteDTO.setCPF("Alterar cpf: " + dataFormatada);
        clienteDTO.setLogradouro("logradouro");
        clienteDTO.setNumero("numero");
        clienteDTO.setPontoReferencia("pontoReferencia");
        clienteDTO.setRG("2222222");
        clienteDTO.setTelefone("(65) 95623-8978");

        Response response1 = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(clienteDTO)
        .when()
            .post("http://localhost:8080/cliente/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        ClienteDTO clienteDTO2 = gson.fromJson(response1.getBody().asString(), ClienteDTO.class);

        clienteDTO2.setNome("Alterado: " + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(clienteDTO2)
            .when()
                .put("http://localhost:8080/cliente/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("nome", Matchers.is("Alterado: " + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){
                
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/bairro/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        BairroDTO bairroDTO = gson.fromJson(response.getBody().asString(), BairroDTO.class);
        
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Excluir: " + dataFormatada);
        clienteDTO.setBairro(bairroDTO);
        clienteDTO.setCPF("Excluir cpf: " + dataFormatada);
        clienteDTO.setLogradouro("logradouro");
        clienteDTO.setNumero("numero");
        clienteDTO.setPontoReferencia("pontoReferencia");
        clienteDTO.setRG("333333");
        clienteDTO.setTelefone("(65) 95623-8978");

        Response response1 = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(clienteDTO)
        .when()
            .post("http://localhost:8080/cliente/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        ClienteDTO clienteDTO2 = gson.fromJson(response1.getBody().asString(), ClienteDTO.class);
        var id = clienteDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/cliente/" + id)
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
            .delete("http://localhost:8080/cliente/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nao existe esse cliente"));
    }

    //#endregion
}
