package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.CardapioDTO;
import ifmt.cba.dto.PreparoProdutoDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class CardapioServicoTest {
    
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
            .get("http://localhost:8080/cardapio/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("descricao", Matchers.is("O cardapio oferece duas opcoes de carnes vermelhas acompanhado com arroz cozido"))
            .body("nome", Matchers.is("Carnes vermelhas com arroz cozido"))
            .body("listaPreparoProduto[0]", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testConsultarPorNomeVerificandoValores(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/cardapio/nome/Carnes")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("descricao", Matchers.is("O cardapio oferece duas opcoes de carnes vermelhas acompanhado com arroz cozido"))
            .body("listaPreparoProduto[0]", Matchers.is(Matchers.notNullValue()))
            .body("nome", Matchers.is("Carnes vermelhas com arroz cozido"))
        ;
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response.getBody().asString(), PreparoProdutoDTO.class);

        List<PreparoProdutoDTO> listaItens = new ArrayList<PreparoProdutoDTO>();
        listaItens.add(preparoProdutoDTO);

        CardapioDTO cardapioDTO = new CardapioDTO();
        cardapioDTO.setNome("Incluir: " + dataFormatada);
        cardapioDTO.setDescricao("Teste inclusão " + dataFormatada);
        cardapioDTO.setListaPreparoProduto(listaItens);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(cardapioDTO)
            .when()
                .post("http://localhost:8080/cardapio/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("nome", Matchers.is("Incluir: " + dataFormatada))
                .body("descricao", Matchers.is("Teste inclusão " + dataFormatada))
                .body("listaPreparoProduto[0]", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/cardapio/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nome invalidoDescricao invalidaCardapio sem itens"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response.getBody().asString(), PreparoProdutoDTO.class);

        List<PreparoProdutoDTO> listaItens = new ArrayList<PreparoProdutoDTO>();
        listaItens.add(preparoProdutoDTO);

        CardapioDTO cardapioDTO = new CardapioDTO();
        cardapioDTO.setNome("Alterar: " + dataFormatada);
        cardapioDTO.setDescricao("Teste alteração " + dataFormatada);
        cardapioDTO.setListaPreparoProduto(listaItens);

        Response response1 = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(cardapioDTO)
        .when()
            .post("http://localhost:8080/cardapio/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        CardapioDTO cardapioDTO2 = gson.fromJson(response1.getBody().asString(), CardapioDTO.class);

        cardapioDTO2.setNome("Alterado: " + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(cardapioDTO2)
            .when()
                .put("http://localhost:8080/cardapio/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(cardapioDTO2.getCodigo()))
                .body("listaPreparoProduto.codigo[0]", Matchers.is(1))
                .body("nome", Matchers.is("Alterado: " + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response.getBody().asString(), PreparoProdutoDTO.class);

        List<PreparoProdutoDTO> listaItens = new ArrayList<PreparoProdutoDTO>();
        listaItens.add(preparoProdutoDTO);

        CardapioDTO cardapioDTO = new CardapioDTO();
        cardapioDTO.setNome("Excluir: " + dataFormatada);
        cardapioDTO.setDescricao("Teste exclusão " + dataFormatada);
        cardapioDTO.setListaPreparoProduto(listaItens);

        Response response1 = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(cardapioDTO)
        .when()
            .post("http://localhost:8080/cardapio/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(Matchers.notNullValue()))
            .extract()
            .response();
    
        CardapioDTO cardapioDTO2 = gson.fromJson(response1.getBody().asString(), CardapioDTO.class);

        var id = cardapioDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/cardapio/" + id)
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
            .delete("http://localhost:8080/cardapio/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nao existe esse cardapio"));
    }

    //#endregion
}
