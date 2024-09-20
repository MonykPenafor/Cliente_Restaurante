package ifmt.cba;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.CardapioDTO;
import ifmt.cba.dto.EstadoOrdemProducaoDTO;
import ifmt.cba.dto.ItemOrdemProducaoDTO;
import ifmt.cba.dto.OrdemProducaoDTO;
import ifmt.cba.dto.PreparoProdutoDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class OrdemProducaoServicoTest {

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
            .get("http://localhost:8080/ordemproducao/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("cardapio.nome", Matchers.is("Carnes vermelhas com arroz cozido"))
            .body("dataProducao", Matchers.is(Matchers.notNullValue()))
            .body("listaItens[0].codigo", Matchers.is(1))
            .body("listaItens", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testConsultarItemPorCodigo(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/ordemproducao/item/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("quantidadePorcao", Matchers.is(50))
            .body("preparoProduto.nome", Matchers.is("Arroz Cozido"))
            .body("preparoProduto.valorPreparo", Matchers.is(0.5F));
    }

    @Test
    public void testConsultarPorDataProducao(){
        RestAssured
        .given()
            .queryParam("dataInicial", "01/09/2024")
            .queryParam("dataFinal", "30/09/2024") 
        .when()
            .get("http://localhost:8080/ordemproducao/dataproducao")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(Matchers.notNullValue()))
            .body("listaItens[0]", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testConsultarPorEstado(){
        RestAssured
        .given()
            .queryParam("estado", "REGISTRADA")
        .when()
            .get("http://localhost:8080/ordemproducao/estado")
        .then()
            .statusCode(200)
            .body("estado[0]", Matchers.is("REGISTRADA"))
            .body("listaItens[0]", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testConsultarPorProducoesPorPeriodoComCalculoDeValores(){
        RestAssured
        .given()
            .queryParam("dataInicial", "01/09/2024")
            .queryParam("dataFinal", "30/09/2024") 
        .when()
            .get("http://localhost:8080/ordemproducao/relatorio")
        .then()
            .log().all()
            .statusCode(200)
            .body("valorTotalGeral", Matchers.greaterThan(1F))
            .body("itens", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testConsultarProdutosProduzidos() {
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cardapio/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());
        CardapioDTO cardapioDTO = gson.fromJson(response.getBody().asString(), CardapioDTO.class);

        Response response1 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response1.getBody().asString(), PreparoProdutoDTO.class);

        ItemOrdemProducaoDTO itemOrdemProducaoDTO = new ItemOrdemProducaoDTO();
        itemOrdemProducaoDTO.setPreparoProduto(preparoProdutoDTO);
        itemOrdemProducaoDTO.setQuantidadePorcao(7);

        List<ItemOrdemProducaoDTO> listaItens = new ArrayList<ItemOrdemProducaoDTO>();
        listaItens.add(itemOrdemProducaoDTO);

        OrdemProducaoDTO ordemProducaoDTO = new OrdemProducaoDTO();
        ordemProducaoDTO.setCardapio(cardapioDTO);
        ordemProducaoDTO.setDataProducao(LocalDate.now());
        ordemProducaoDTO.setEstado(EstadoOrdemProducaoDTO.REGISTRADA);
        ordemProducaoDTO.setListaItens(listaItens);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO))
            .when()
                .post("http://localhost:8080/ordemproducao/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estado", Matchers.is("REGISTRADA"))
                .extract()
                .response();

        OrdemProducaoDTO ordemProducaoDTO2 = gson.fromJson(response3.getBody().asString(), OrdemProducaoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO2))
            .when()
                .put("http://localhost:8080/ordemproducao/processar")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estado", Matchers.is("PROCESSADA"));

        Map<String, Integer> itens = RestAssured
            .given()
                .queryParam("dataInicial", "01/09/2024")
                .queryParam("dataFinal", "30/09/2024")
            .when()
                .get("http://localhost:8080/ordemproducao/itens")
            .then()
                .log().all()
                .statusCode(200)  
                .extract()
                .jsonPath()
                .getMap("");
        
            assertThat(itens, Matchers.is(Matchers.notNullValue()));
            assertThat(itens.size(), Matchers.greaterThan(0));

            String firstKey = itens.keySet().iterator().next();

            assertThat(itens.get(firstKey), Matchers.greaterThan(0));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE - OP
    @Test
    public void testInclusaoComDadosCorretos(){
       
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cardapio/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        CardapioDTO cardapioDTO = gson.fromJson(response.getBody().asString(), CardapioDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemOrdemProducaoDTO itemOrdemProducaoDTO = new ItemOrdemProducaoDTO();
        itemOrdemProducaoDTO.setPreparoProduto(preparoProdutoDTO);
        itemOrdemProducaoDTO.setQuantidadePorcao(7);

        List<ItemOrdemProducaoDTO> listaItens = new ArrayList<ItemOrdemProducaoDTO>();
        listaItens.add(itemOrdemProducaoDTO);

        OrdemProducaoDTO ordemProducaoDTO = new OrdemProducaoDTO();
        ordemProducaoDTO.setCardapio(cardapioDTO);
        ordemProducaoDTO.setDataProducao(LocalDate.now());
        ordemProducaoDTO.setEstado(EstadoOrdemProducaoDTO.REGISTRADA);
        ordemProducaoDTO.setListaItens(listaItens);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO))
            .when()
                .post("http://localhost:8080/ordemproducao/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("cardapio.nome", Matchers.is("Carnes vermelhas com arroz cozido"))
                .body("estado", Matchers.is("REGISTRADA"))
                .body("dataProducao", Matchers.is(Matchers.notNullValue()))
                .body("listaItens", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testInclusaoComDadosIncorretos(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/ordemproducao/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Ordem de Producao deve ter pelo menos um itemData de producao invalidaCardapio invalidoEstado da ordem invalido"));
    }
    
    @Test
    public void testAlteracaoComDadosCorretos(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/ordemproducao/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        OrdemProducaoDTO ordemProducaoDTO = gson.fromJson(response.getBody().asString(), OrdemProducaoDTO.class);

        ordemProducaoDTO.setDataProducao(LocalDate.now());

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO))
            .when()
                .put("http://localhost:8080/ordemproducao/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("listaItens", Matchers.is(Matchers.notNullValue()))
                .body("dataProducao", Matchers.is(LocalDate.now().toString()));
    }
    
    @Test
    public void testAlterarOPProcessada(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cardapio/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        CardapioDTO cardapioDTO = gson.fromJson(response.getBody().asString(), CardapioDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemOrdemProducaoDTO itemOrdemProducaoDTO = new ItemOrdemProducaoDTO();
        itemOrdemProducaoDTO.setPreparoProduto(preparoProdutoDTO);
        itemOrdemProducaoDTO.setQuantidadePorcao(7);

        List<ItemOrdemProducaoDTO> listaItens = new ArrayList<ItemOrdemProducaoDTO>();
        listaItens.add(itemOrdemProducaoDTO);

        OrdemProducaoDTO ordemProducaoDTO = new OrdemProducaoDTO();
        ordemProducaoDTO.setCardapio(cardapioDTO);
        ordemProducaoDTO.setDataProducao(LocalDate.now());
        ordemProducaoDTO.setEstado(EstadoOrdemProducaoDTO.REGISTRADA);
        ordemProducaoDTO.setListaItens(listaItens);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO))
            .when()
                .post("http://localhost:8080/ordemproducao/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estado", Matchers.is("REGISTRADA"))
                .extract()
                .response();

        OrdemProducaoDTO ordemProducaoDTO2 = gson.fromJson(response3.getBody().asString(), OrdemProducaoDTO.class);

        Response response4 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO2))
            .when()
                .put("http://localhost:8080/ordemproducao/processar")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estado", Matchers.is("PROCESSADA"))
                .extract()
                .response();

        OrdemProducaoDTO ordemProducaoDTO3 = gson.fromJson(response4.getBody().asString(), OrdemProducaoDTO.class);     
        ordemProducaoDTO3.setDataProducao(LocalDate.now());  

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO3))
            .when()
                .put("http://localhost:8080/ordemproducao/")
            .then()
                .log().all()
                .statusCode(400)
                .body("texto", Matchers.is("Ordem de produção já processada, não pode ser alterada"));
    }
    
    @Test
    public void testExclusaoComDadosCorretos(){
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cardapio/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());
        CardapioDTO cardapioDTO = gson.fromJson(response.getBody().asString(), CardapioDTO.class);

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemOrdemProducaoDTO itemOrdemProducaoDTO = new ItemOrdemProducaoDTO();
        itemOrdemProducaoDTO.setPreparoProduto(preparoProdutoDTO);
        itemOrdemProducaoDTO.setQuantidadePorcao(7);

        List<ItemOrdemProducaoDTO> listaItens = new ArrayList<ItemOrdemProducaoDTO>();
        listaItens.add(itemOrdemProducaoDTO);

        OrdemProducaoDTO ordemProducaoDTO = new OrdemProducaoDTO();
        ordemProducaoDTO.setCardapio(cardapioDTO);
        ordemProducaoDTO.setDataProducao(LocalDate.now());
        ordemProducaoDTO.setEstado(EstadoOrdemProducaoDTO.REGISTRADA);
        ordemProducaoDTO.setListaItens(listaItens);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO))
            .when()
                .post("http://localhost:8080/ordemproducao/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        OrdemProducaoDTO ordemProducaoDTO2 = gson.fromJson(response3.getBody().asString(), OrdemProducaoDTO.class);

        var id = ordemProducaoDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/ordemproducao/" + id)
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
            .delete("http://localhost:8080/ordemproducao/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto", Matchers.is("source cannot be null"));
    }

    @Test
    public void testProcessarOPValida(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cardapio/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        CardapioDTO cardapioDTO = gson.fromJson(response.getBody().asString(), CardapioDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemOrdemProducaoDTO itemOrdemProducaoDTO = new ItemOrdemProducaoDTO();
        itemOrdemProducaoDTO.setPreparoProduto(preparoProdutoDTO);
        itemOrdemProducaoDTO.setQuantidadePorcao(7);

        List<ItemOrdemProducaoDTO> listaItens = new ArrayList<ItemOrdemProducaoDTO>();
        listaItens.add(itemOrdemProducaoDTO);

        OrdemProducaoDTO ordemProducaoDTO = new OrdemProducaoDTO();
        ordemProducaoDTO.setCardapio(cardapioDTO);
        ordemProducaoDTO.setDataProducao(LocalDate.now());
        ordemProducaoDTO.setEstado(EstadoOrdemProducaoDTO.REGISTRADA);
        ordemProducaoDTO.setListaItens(listaItens);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO))
            .when()
                .post("http://localhost:8080/ordemproducao/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estado", Matchers.is("REGISTRADA"))
                .extract()
                .response();

        OrdemProducaoDTO ordemProducaoDTO2 = gson.fromJson(response3.getBody().asString(), OrdemProducaoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO2))
            .when()
                .put("http://localhost:8080/ordemproducao/processar")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estado", Matchers.is("PROCESSADA"));
    }

    @Test
    public void testProcessarOPInvalida(){

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/ordemproducao/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());

        OrdemProducaoDTO ordemProducaoDTO = gson.fromJson(response.getBody().asString(), OrdemProducaoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(ordemProducaoDTO))
            .when()
                .put("http://localhost:8080/ordemproducao/processar")
            .then()
                .log().all()
                .statusCode(400)
                .body("texto", Matchers.is("Ordem ja processada"));
    }

    //#endregion

}
