# Cliente Restaurante - Cliente API

## Projeto: Sistema de Gestão de Vendas e Entregas de Refeições

Este projeto é um cliente que consome a API REST desenvolvida para o sistema de gestão de um restaurante. O cliente foi implementado em Java, utilizando Maven, e contém testes unitários que garantem a funcionalidade das classes de serviço do servidor.

Meu repositorio que tem os serviços é este: [Servidor_Restaurante](https://github.com/MonykPenafor/Servidor_Restaurante/tree/main).

Todos os testes podem ser encontrados [nesta pasta](src/main/java/ifmt/cba).

## Funcionalidades

- **Teste de Serviços**: Execução de testes unitários para validar os serviços da API do servidor.
- **Consumo da API REST**: Interação com os endpoints da API para gestão de produtos, cardápios e pedidos...

## Estrutura do Projeto

- **dto**: Data Transfer Objects utilizados para a comunicação com a API.
- **test**: Contém os testes unitários para as classes de serviço do servidor.
- **utils**: Classes utilitárias para facilitar a execução dos testes.

## Tecnologias Utilizadas

- **Java 19**: Linguagem de programação principal.
- **Maven**: Gerenciamento de dependências e construção do projeto.
- **RestAssured**: Para facilitar a execução de testes em APIs REST.
- **JUnit**: Para testes automatizados.
- **Gson**: Para serialização e deserialização de objetos JSON.

## Exemplo de Teste

O seguinte é um exemplo de teste para a classe `PreparoProdutoServico`, que valida a consulta e manipulação de dados no serviço de preparo de produtos:

```java
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PreparoProdutoServicoTest {

    @Test
    public void testConsultarPorCodigo() {
        given()
        .when()
            .get("http://localhost:8080/preparo/codigo/3")
        .then()
            .statusCode(200)
            .body("codigo", is(3))
            .body("valorPreparo", is(3.0F));
    }

    // Outros testes...
}
