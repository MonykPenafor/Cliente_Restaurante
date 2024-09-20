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

import ifmt.cba.dto.GrupoAlimentarDTO;
import ifmt.cba.dto.PreparoProdutoDTO;
import ifmt.cba.dto.ProdutoDTO;
import ifmt.cba.dto.TipoPreparoDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class PreparoProdutoServicoTest {
            
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
            .get("http://localhost:8080/preparo/codigo/3")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(3))
            .body("valorPreparo", Matchers.is(3.0F))
            .body("tempoPreparo", Matchers.is(20))
            .body("produto.nome", Matchers.is("Alcatra bovina"))
            .body("nome", Matchers.is("Alcatra bovina grelhada"))
            .body("tipoPreparo.descricao", Matchers.is("Grelhado"));
    }

    @Test
    public void testConsultarPorNome(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/preparo/nome/Costela")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(2))
            .body("valorPreparo[0]", Matchers.is(4.0F))
            .body("tempoPreparo[0]", Matchers.is(60))
            .body("tipoPreparo.descricao[0]", Matchers.is("Assado no forno"))
            .body("produto.nome[0]", Matchers.is("Costela suina"))
            .body("nome[0]", Matchers.is("Costela suina no forno"));
    }

    @Test
    public void testConsultarPreparoPorProduto(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/preparo/produto/5")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(1))
            .body("valorPreparo[0]", Matchers.is(0.5F))
            .body("tempoPreparo[0]", Matchers.is(25))
            .body("tipoPreparo.descricao[0]", Matchers.is("Cozimento em agua"))
            .body("produto.nome[0]", Matchers.is("Arroz Branco"))
            .body("produto.codigo[0]", Matchers.is(5))
            .body("nome[0]", Matchers.is("Arroz Cozido"));
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE
    @Test
    public void testInclusaoComDadosCorretos(){
          
        //#region criando o produto e tipoPreparo
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        GrupoAlimentarDTO grupoAlimentarDTO = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setGrupoAlimentar(grupoAlimentarDTO);
        produtoDTO.setNome("teste: " + dataFormatada);
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

        

        TipoPreparoDTO tipoPreparoDTO = new TipoPreparoDTO();
        tipoPreparoDTO.setDescricao("Teste descrição " + dataFormatada);

        Response response10 = RestAssured
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
    
        TipoPreparoDTO tipoPreparoDTO2 = gson.fromJson(response10.getBody().asString(), TipoPreparoDTO.class);

        
        //#endregion

        PreparoProdutoDTO preparoProdutoDTO = new PreparoProdutoDTO();
        preparoProdutoDTO.setNome("Teste Incluir: " + dataFormatada);
        preparoProdutoDTO.setProduto(produtoDTO2);
        preparoProdutoDTO.setTipoPreparo(tipoPreparoDTO2);
        preparoProdutoDTO.setTempoPreparo(20);
        preparoProdutoDTO.setValorPreparo(3.0F);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProdutoDTO)
            .when()
                .post("http://localhost:8080/preparo/")
            .then()
                .log().all()
                .statusCode(200)
                .body("nome", Matchers.is("Teste Incluir: " + dataFormatada));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/preparo/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Deve existir um produto relacionadoDeve existir um tipo de preparo relacionadoTempo de preparo invalidoValor de preparo invalido"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){
         
        //#region criar produto, tipoPreparo e preparoproduto
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        GrupoAlimentarDTO grupoAlimentarDTO = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setGrupoAlimentar(grupoAlimentarDTO);
        produtoDTO.setNome("testepp: " + dataFormatada);
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
        


        TipoPreparoDTO tipoPreparoDTO = new TipoPreparoDTO();
        tipoPreparoDTO.setDescricao("testepp" + dataFormatada);

        Response response10 = RestAssured
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
    


        TipoPreparoDTO tipoPreparoDTO2 = gson.fromJson(response10.getBody().asString(), TipoPreparoDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = new PreparoProdutoDTO();
        preparoProdutoDTO.setNome("Alterar: " + dataFormatada);
        preparoProdutoDTO.setProduto(produtoDTO2);
        preparoProdutoDTO.setTipoPreparo(tipoPreparoDTO2);
        preparoProdutoDTO.setTempoPreparo(20);
        preparoProdutoDTO.setValorPreparo(3.0F);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProdutoDTO)
            .when()
                .post("http://localhost:8080/preparo/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        PreparoProdutoDTO preparoProdutoDTO2 = gson.fromJson(response3.getBody().asString(), PreparoProdutoDTO.class);

          //#endregion
        
        preparoProdutoDTO2.setNome("Alterado: " + dataFormatada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProdutoDTO2)
            .when()
                .put("http://localhost:8080/preparo/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("nome", Matchers.is("Alterado: " + dataFormatada));
    }

    @Test
    public void testExclusaoComDadosCorretos(){
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/produto/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/tipopreparo/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ProdutoDTO produtoDTO = gson.fromJson(response.getBody().asString(), ProdutoDTO.class);        
        TipoPreparoDTO tipoPreparoDTO = gson.fromJson(response2.getBody().asString(), TipoPreparoDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = new PreparoProdutoDTO();
        preparoProdutoDTO.setNome("Excluir: " + dataFormatada);
        preparoProdutoDTO.setProduto(produtoDTO);
        preparoProdutoDTO.setTipoPreparo(tipoPreparoDTO);
        preparoProdutoDTO.setTempoPreparo(20);
        preparoProdutoDTO.setValorPreparo(3.0F);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProdutoDTO)
            .when()
                .post("http://localhost:8080/preparo/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        PreparoProdutoDTO preparoProdutoDTO2 = gson.fromJson(response3.getBody().asString(), PreparoProdutoDTO.class);

        var id = preparoProdutoDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/preparo/" + id)
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
            .delete("http://localhost:8080/preparo/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nao existe esse preparo de produto"));
    }

    //#endregion
}
