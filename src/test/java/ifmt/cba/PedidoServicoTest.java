package ifmt.cba;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.ClienteDTO;
import ifmt.cba.dto.EntregadorDTO;
import ifmt.cba.dto.EstadoPedidoDTO;
import ifmt.cba.dto.ItemPedidoDTO;
import ifmt.cba.dto.PedidoDTO;
import ifmt.cba.dto.PreparoProdutoDTO;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class PedidoServicoTest {

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
            .get("http://localhost:8080/pedido/codigo/1")
        .then()
            .statusCode(200)
            .body("codigo", Matchers.is(1))
            .body("cliente.codigo", Matchers.is(1))
            .body("dataPedido", Matchers.is(Matchers.notNullValue()))
            .body("estado", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testConsultarPorDataProducao(){
        RestAssured
        .given()
            .queryParam("dataInicial", "01/01/2024")
            .queryParam("dataFinal", "29/12/2024") 
        .when()
            .get("http://localhost:8080/pedido/dataproducao")
        .then()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(Matchers.notNullValue()))
            .body("cliente.codigo[0]", Matchers.is(1))
            .body("dataPedido[0]", Matchers.is(Matchers.notNullValue()))
            .body("estado[0]", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testConsultarPorEstado(){
        RestAssured
        .given()
            .log().all()
            .queryParam("estado", "REGISTRADO")
        .when()
            .get("http://localhost:8080/pedido/estado")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(Matchers.notNullValue()))
            .body("estado[0]", Matchers.is("REGISTRADO"));
    }

    @Test
    public void testConsultarPorEstadoeData(){
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cliente/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ClienteDTO clienteDTO = gson.fromJson(response.getBody().asString(), ClienteDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemPedidoDTO itemPedidoDTO = new ItemPedidoDTO();
        itemPedidoDTO.setPreparoProduto(preparoProdutoDTO);
        itemPedidoDTO.setQuantidadePorcao(7);

        List<ItemPedidoDTO> listaItens = new ArrayList<ItemPedidoDTO>();
        listaItens.add(itemPedidoDTO);

        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setDataPedido(LocalDate.parse("2024-09-06"));
        pedidoDTO.setHoraPedido(LocalTime.now());
        pedidoDTO.setCliente(clienteDTO);
        pedidoDTO.setEstado(EstadoPedidoDTO.REGISTRADO);
        pedidoDTO.setListaItens(listaItens);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(pedidoDTO))
            .when()
                .post("http://localhost:8080/pedido/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()));
        
        RestAssured
        .given()
            .log().all()
            .queryParam("data", "06/09/2024") 
            .queryParam("estado", "REGISTRADO")
        .when()
            .get("http://localhost:8080/pedido/estadodata")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo[0]", Matchers.is(Matchers.notNullValue()))
            .body("dataPedido[0]", Matchers.is("2024-09-06"))
            .body("estado[0]", Matchers.is("REGISTRADO"));

    }

    @Test
    public void testConsultarPorCliente() {
            
       RestAssured
            .given()
            .when()
                .get("http://localhost:8080/pedido/cliente/1")
            .then()
                .statusCode(200)
                .body("codigo[0]", Matchers.is(Matchers.notNullValue()))
                .body("cliente.codigo[0]", Matchers.is(1));
    }

    @Test
    public void testConsultarTempoMedioDePedidoAPronto(){
        RestAssured
        .given()
        .when()
            .get("http://localhost:8080/pedido/tempomedioproducao")
        .then()
            .log().all()
            .statusCode(200)
            .body(Matchers.notNullValue());
    }

    @Test
    public void testConsultarTempoMedioDeProntoAFinalizado(){
        RestAssured
        .given()
        .when()
        .get("http://localhost:8080/pedido/tempomediofinalizacao")
        .then()
            .log().all()
            .statusCode(200)
            .body(Matchers.notNullValue());
    }

    //#endregion

    //#region CREATE, UPDATE, DELETE - OP
    @Test
    public void testInclusaoComDadosCorretos(){
       
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cliente/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ClienteDTO clienteDTO = gson.fromJson(response.getBody().asString(), ClienteDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemPedidoDTO itemPedidoDTO = new ItemPedidoDTO();
        itemPedidoDTO.setPreparoProduto(preparoProdutoDTO);
        itemPedidoDTO.setQuantidadePorcao(7);

        List<ItemPedidoDTO> listaItens = new ArrayList<ItemPedidoDTO>();
        listaItens.add(itemPedidoDTO);

        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setDataPedido(LocalDate.now());
        pedidoDTO.setHoraPedido(LocalTime.now());
        pedidoDTO.setCliente(clienteDTO);
        pedidoDTO.setEstado(EstadoPedidoDTO.REGISTRADO);
        pedidoDTO.setListaItens(listaItens);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(pedidoDTO))
            .when()
                .post("http://localhost:8080/pedido/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estado", Matchers.is("REGISTRADO"))
                .body("cliente.nome", Matchers.is("Cliente 01"))
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
            .post("http://localhost:8080/pedido/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Data do pedido invalidaHora do pedido invalidaCliente do pedido invalidoEstado do pedido invalidoPedido sem itens"));
    }
    
    @Test
    public void testAlteracaoComDadosCorretos(){

               
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cliente/codigo/2");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ClienteDTO clienteDTO = gson.fromJson(response.getBody().asString(), ClienteDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemPedidoDTO itemPedidoDTO = new ItemPedidoDTO();
        itemPedidoDTO.setPreparoProduto(preparoProdutoDTO);
        itemPedidoDTO.setQuantidadePorcao(7);

        List<ItemPedidoDTO> listaItens = new ArrayList<ItemPedidoDTO>();
        listaItens.add(itemPedidoDTO);

        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setDataPedido(LocalDate.now());
        pedidoDTO.setHoraPedido(LocalTime.now());
        pedidoDTO.setCliente(clienteDTO);
        pedidoDTO.setEstado(EstadoPedidoDTO.REGISTRADO);
        pedidoDTO.setListaItens(listaItens);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(pedidoDTO))
            .when()
                .post("http://localhost:8080/pedido/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        PedidoDTO pedidoDTO2 = gson.fromJson(response3.getBody().asString(), PedidoDTO.class);

        pedidoDTO2.getListaItens().getFirst().setQuantidadePorcao(3);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(pedidoDTO2))
            .when()
                .put("http://localhost:8080/pedido/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("listaItens", Matchers.is(Matchers.notNullValue()));
    }
    
    @Test
    public void testExclusaoComDadosCorretos(){
        
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cliente/codigo/2");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response2 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ClienteDTO clienteDTO = gson.fromJson(response.getBody().asString(), ClienteDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response2.getBody().asString(), PreparoProdutoDTO.class);

        ItemPedidoDTO itemPedidoDTO = new ItemPedidoDTO();
        itemPedidoDTO.setPreparoProduto(preparoProdutoDTO);
        itemPedidoDTO.setQuantidadePorcao(7);

        List<ItemPedidoDTO> listaItens = new ArrayList<ItemPedidoDTO>();
        listaItens.add(itemPedidoDTO);

        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setDataPedido(LocalDate.now());
        pedidoDTO.setHoraPedido(LocalTime.now());
        pedidoDTO.setCliente(clienteDTO);
        pedidoDTO.setEstado(EstadoPedidoDTO.REGISTRADO);
        pedidoDTO.setListaItens(listaItens);


        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(pedidoDTO))
            .when()
                .post("http://localhost:8080/pedido/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        PedidoDTO pedidoDTO2 = gson.fromJson(response3.getBody().asString(), PedidoDTO.class);

        var id = pedidoDTO2.getCodigo();

        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/pedido/" + id)
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
            .delete("http://localhost:8080/pedido/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto", Matchers.is("source cannot be null"));
    }

    @Test
    public void testMudarEstadoPedido(){
        Response response = RestAssured.request(Method.GET, "http://localhost:8080/cliente/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Response response1 = RestAssured.request(Method.GET, "http://localhost:8080/preparo/codigo/3");
        Assertions.assertEquals(200, response.getStatusCode());
                
        ClienteDTO clienteDTO = gson.fromJson(response.getBody().asString(), ClienteDTO.class);

        PreparoProdutoDTO preparoProdutoDTO = gson.fromJson(response1.getBody().asString(), PreparoProdutoDTO.class);

        ItemPedidoDTO itemPedidoDTO = new ItemPedidoDTO();
        itemPedidoDTO.setPreparoProduto(preparoProdutoDTO);
        itemPedidoDTO.setQuantidadePorcao(7);

        List<ItemPedidoDTO> listaItens = new ArrayList<ItemPedidoDTO>();
        listaItens.add(itemPedidoDTO);

        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setDataPedido(LocalDate.now());
        pedidoDTO.setHoraPedido(LocalTime.now());
        pedidoDTO.setCliente(clienteDTO);
        pedidoDTO.setEstado(EstadoPedidoDTO.REGISTRADO);
        pedidoDTO.setListaItens(listaItens);

        Response response2 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(pedidoDTO))
            .when()
                .post("http://localhost:8080/pedido/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .extract()
                .response();

        PedidoDTO pedidoDTO1 = gson.fromJson(response2.getBody().asString(), PedidoDTO.class);

        Response response3 = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(gson.toJson(pedidoDTO1))
            .when()
                .put("http://localhost:8080/pedido/producao")
            .then()
                .log().all()
                .statusCode(200)
                .body("estado", Matchers.is("PRODUCAO"))
                .extract()
                .response();;

        PedidoDTO pedidoDTO3 = gson.fromJson(response3.getBody().asString(), PedidoDTO.class);

        Response response4 = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(gson.toJson(pedidoDTO3))
        .when()
            .put("http://localhost:8080/pedido/pronto")
        .then()
            .log().all()
            .statusCode(200)
            .body("estado", Matchers.is("PRONTO"))
            .extract()
            .response();

        PedidoDTO pedidoDTO4 = gson.fromJson(response4.getBody().asString(), PedidoDTO.class);

        Response response6 = RestAssured.request(Method.GET, "http://localhost:8080/entregador/codigo/1");
        Assertions.assertEquals(200, response6.getStatusCode());

        EntregadorDTO entregadorDTO = gson.fromJson(response6.getBody().asString(), EntregadorDTO.class);

        pedidoDTO4.setEntregador(entregadorDTO);

        Response response5 = RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(gson.toJson(pedidoDTO4))
        .when()
            .put("http://localhost:8080/pedido/entrega")
        .then()
            .log().all()
            .statusCode(200)
            .body("estado", Matchers.is("ENTREGA"))
            .extract()
            .response();

        PedidoDTO pedidoDTO5 = gson.fromJson(response5.getBody().asString(), PedidoDTO.class);

        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(gson.toJson(pedidoDTO5))
        .when()
            .put("http://localhost:8080/pedido/concluido")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo", Matchers.is(pedidoDTO1.getCodigo()))
            .body("estado", Matchers.is("CONCLUIDO"));


    
    }


    //#endregion


}
