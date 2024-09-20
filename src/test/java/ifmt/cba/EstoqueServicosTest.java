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

import ifmt.cba.dto.MovimentoEstoqueDTO;
import ifmt.cba.dto.ProdutoDTO;
import ifmt.cba.dto.RegistroEstoqueDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class EstoqueServicosTest {

    String data = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
     
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
            .get("http://localhost:8080/estoque/codigo/1")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("data", Matchers.is(Matchers.notNullValue()))
            .body("movimento", Matchers.is("COMPRA"))
            .body("quantidade", Matchers.is(100))
            .body("produto.nome", Matchers.is("Arroz Branco"));
    }

    @Test
    public void testConsultarPorMovimento(){
        RestAssured
        .given()
            .queryParam("movimento", "COMPRA")
        .when()
            .get("http://localhost:8080/estoque/movimento")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo[0]", Matchers.greaterThan(0))
            .body("movimento[0]", Matchers.is("COMPRA"));
    }

    @Test
    public void testConsultarPorEstoqueDescartado(){
        RestAssured
        .given()
            .queryParam("dataInicial", "01/09/2024")
            .queryParam("dataFinal", "30/12/2024") 
        .when()
            .get("http://localhost:8080/estoque/descartados")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(Matchers.notNullValue()))
            .body("quantidade[0]", Matchers.greaterThan(1));
    }

    @Test
    public void testConsultarPorProduto(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/estoque/produto/5")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(Matchers.notNullValue()))
            .body("produto.nome[0]", Matchers.is("Arroz Branco"));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE - OP
    @Test
    public void testInclusaoComDadosCorretos(){
       
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/produto/codigo/2");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ProdutoDTO produtoDTO = gson.fromJson(response.getBody().asString(), ProdutoDTO.class);

        RegistroEstoqueDTO registroEstoqueDTO = new RegistroEstoqueDTO();
        registroEstoqueDTO.setData(LocalDate.now());
        registroEstoqueDTO.setQuantidade(111);
        registroEstoqueDTO.setMovimento(MovimentoEstoqueDTO.COMPRA);
        registroEstoqueDTO.setProduto(produtoDTO);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(registroEstoqueDTO))
            .when()
                .post("http://localhost:8080/estoque/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("produto.nome", Matchers.is("Costela suina"))
                .body("movimento", Matchers.is("COMPRA"))
                .body("data", Matchers.is(LocalDate.now().toString()))
                .body("quantidade", Matchers.greaterThan(110));
    }

    @Test
    public void testInclusaoComDadosIncorretos(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/estoque/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Produto invalidoCusto por unidade invalidoMotivo invalidoData invalida"));
    }
    
    @Test
    public void testExclusaoComDadosCorretos(){
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/produto/codigo/2");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ProdutoDTO produtoDTO = gson.fromJson(response.getBody().asString(), ProdutoDTO.class);

        RegistroEstoqueDTO registroEstoqueDTO = new RegistroEstoqueDTO();
        registroEstoqueDTO.setData(LocalDate.now());
        registroEstoqueDTO.setQuantidade(10);
        registroEstoqueDTO.setMovimento(MovimentoEstoqueDTO.COMPRA);
        registroEstoqueDTO.setProduto(produtoDTO);

        Response response1 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(registroEstoqueDTO))
            .when()
                .post("http://localhost:8080/estoque/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        RegistroEstoqueDTO registroEstoqueDTO2 = gson.fromJson(response1.getBody().asString(), RegistroEstoqueDTO.class);

        var id = registroEstoqueDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/estoque/" + id)
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
            .delete("http://localhost:8080/estoque/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto", Matchers.is("source cannot be null"));
    }

    //#endregion

}