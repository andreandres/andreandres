package br.com.trabalho.estoque;

import br.com.trabalho.estoque.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:testes;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProdutoApiTest {
    @Autowired TestRestTemplate http;
    @Autowired ProdutoRepository repository;

    @BeforeEach
    void limpar() { repository.deleteAll(); }

    @NonNull
    private JsonNode corpo(ResponseEntity<JsonNode> resposta) {
        JsonNode corpo = resposta.getBody();
        if (corpo == null) {
            throw new AssertionError("A resposta deve conter um corpo JSON");
        }
        return corpo;
    }

    private HttpEntity<Map<String, Object>> requisicao(Map<String, Object> dados) {
        if (dados == null) {
            throw new AssertionError("A requisicao deve conter dados");
        }
        return new HttpEntity<>(dados);
    }

    private Map<String, Object> dados(String nome) {
        return Map.of("nome", nome, "preco", 49.90, "quantidadeEstoque", 10, "categoria", "Informática");
    }

    @Test
    void deveExecutarCrudCompletoPorHttp() {
        var criado = http.postForEntity("/api/produtos", dados("Mouse"), JsonNode.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(criado.getHeaders().getLocation()).isNotNull();
        String rota = "/api/produtos/" + corpo(criado).get("id").asLong();
        assertThat(corpo(criado).get("dataCriacao").asText()).isNotBlank();
        assertThat(http.getForEntity(rota, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(http.getForObject("/api/produtos?nome=OUS", JsonNode.class).size()).isEqualTo(1);
        assertThat(http.getForObject("/api/produtos?nome=teclado", JsonNode.class).size()).isZero();

        var atualizado = http.exchange(rota, HttpMethod.PUT, requisicao(dados("Teclado")), JsonNode.class);
        assertThat(atualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(corpo(atualizado).get("nome").asText()).isEqualTo("Teclado");
        assertThat(corpo(atualizado).get("dataCriacao")).isEqualTo(corpo(criado).get("dataCriacao"));

        var excluido = http.exchange(rota, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        assertThat(excluido.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(excluido.getBody()).isNull();
        assertThat(http.getForEntity(rota, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.getForObject("/api/produtos", JsonNode.class).size()).isZero();
    }

    @Test
    void deveValidarCamposEJson() {
        var invalido = http.postForEntity("/api/produtos", Map.of(
            "nome", " ", "preco", -1, "quantidadeEstoque", -1), JsonNode.class);
        assertThat(invalido.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(corpo(invalido).get("campos").size()).isEqualTo(3);
        assertThat(http.postForEntity("/api/produtos", Map.of(), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        assertThat(http.postForEntity("/api/produtos", new HttpEntity<>("{", headers), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.getForEntity("/api/produtos/abc", JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.postForEntity("/api/produtos", new HttpEntity<>(
            "{\"nome\":\"Mouse\",\"preco\":1,\"quantidadeEstoque\":1.5}", headers), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.postForEntity("/api/produtos", new HttpEntity<>(
            "{\"nome\":\"Mouse\",\"preco\":1,\"quantidadeEstoque\":1,\"id\":2}", headers), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deveRetornar404EmTodasAsOperacoesComIdInexistente() {
        String rota = "/api/produtos/999999";
        assertThat(http.getForEntity(rota, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(rota, HttpMethod.PUT, requisicao(dados("Mouse")), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(rota, HttpMethod.DELETE, HttpEntity.EMPTY, JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveImpedirNomeDuplicadoNaCriacaoEAtualizacao() {
        http.postForEntity("/api/produtos", dados("Mouse"), JsonNode.class);
        assertThat(http.postForEntity("/api/produtos", dados(" mouse "), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
        var outro = http.postForEntity("/api/produtos", dados("Teclado"), JsonNode.class);
        String rota = "/api/produtos/" + corpo(outro).get("id").asLong();
        assertThat(http.exchange(rota, HttpMethod.PUT, requisicao(dados("Mouse")), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(http.getForObject(rota, JsonNode.class).get("nome").asText()).isEqualTo("Teclado");
    }

    @Test
    void putDeveExigirTodosOsCamposObrigatoriosELimparCategoriaOmitida() {
        var criado = http.postForEntity("/api/produtos", dados("Mouse"), JsonNode.class);
        String rota = "/api/produtos/" + corpo(criado).get("id").asLong();
        assertThat(http.exchange(rota, HttpMethod.PUT, requisicao(Map.of("nome", "Novo")), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var atualizado = http.exchange(rota, HttpMethod.PUT,
            requisicao(Map.of("nome", "Novo", "preco", 0, "quantidadeEstoque", 0)), JsonNode.class);
        assertThat(atualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(corpo(atualizado).get("categoria").isNull()).isTrue();
    }
}
