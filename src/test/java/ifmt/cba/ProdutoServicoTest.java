package ifmt.cba;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.GrupoAlimentarDTO;
import ifmt.cba.dto.ProdutoDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class ProdutoServicoTest {
        
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
            .get("http://localhost:8080/produto/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("custoUnidade", Matchers.is(2F))
            .body("estoque", Matchers.greaterThan(1))
            .body("estoqueMinimo", Matchers.is(100))
            .body("grupoAlimentar.nome", Matchers.is("Proteinas"))
            .body("nome", Matchers.is("Alcatra bovina"))
            .body("valorEnergetico", Matchers.is(50));
    }

    @Test
    public void testConsultarPorNomeVerificandoValores(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/produto/nome/Ingle")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.greaterThan(0))
            .body("nome[0]", Matchers.is("Batata Inglesa"));
    }

    @Test
    public void testConsultarProdutosAbaixoDoEstoqueMinimo(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/produto/estoquebaixo")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(4))
            .body("nome[0]", Matchers.is("Batata Doce"))
            .body("estoqueMinimo[0]", Matchers.is(200))
            .body("estoque[0]", Matchers.is(90));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());
                
        GrupoAlimentarDTO grupoAlimentarDTO = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setGrupoAlimentar(grupoAlimentarDTO);
        produtoDTO.setNome("Teste add: " + dataFormatada);
        produtoDTO.setCustoUnidade(2.1F);
        produtoDTO.setEstoque(350);
        produtoDTO.setEstoqueMinimo(170);
        produtoDTO.setValorEnergetico(75);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoDTO)
            .when()
                .post("http://localhost:8080/produto/")
            .then()
                .log().all()
                .statusCode(200)
                .body("nome", Matchers.is("Teste add: " + dataFormatada))
                .body("grupoAlimentar.nome", Matchers.is("Carboidrato"));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/produto/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nome invalidoCusto por unidade invalidoGrupo alimentar invalido"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        GrupoAlimentarDTO grupoAlimentarDTO = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setGrupoAlimentar(grupoAlimentarDTO);
        produtoDTO.setNome("Alterar: " + dataFormatada);
        produtoDTO.setCustoUnidade(2.1F);
        produtoDTO.setEstoque(350);
        produtoDTO.setEstoqueMinimo(170);
        produtoDTO.setValorEnergetico(75);

        Response response2 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoDTO)
            .when()
                .post("http://localhost:8080/produto/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        ProdutoDTO produtoDTO2 = gson.fromJson(response2.getBody().asString(), ProdutoDTO.class);

        produtoDTO2.setNome("Alterado " + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoDTO2)
            .when()
                .put("http://localhost:8080/produto/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("nome", Matchers.is("Alterado " + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        GrupoAlimentarDTO grupoAlimentarDTO = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setGrupoAlimentar(grupoAlimentarDTO);
        produtoDTO.setNome("Excluir: " + dataFormatada);
        produtoDTO.setCustoUnidade(2.1F);
        produtoDTO.setEstoque(350);
        produtoDTO.setEstoqueMinimo(170);
        produtoDTO.setValorEnergetico(75);

        Response response2 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoDTO)
            .when()
                .post("http://localhost:8080/produto/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        ProdutoDTO produtoDTO2 = gson.fromJson(response2.getBody().asString(), ProdutoDTO.class);

        var id = produtoDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/produto/" + id)
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
            .delete("http://localhost:8080/produto/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nao existe esse produto"));
    }

    //#endregion
}
