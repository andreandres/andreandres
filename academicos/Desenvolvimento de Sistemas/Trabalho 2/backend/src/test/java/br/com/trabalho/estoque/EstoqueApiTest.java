package br.com.trabalho.estoque;

import br.com.trabalho.estoque.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:integracao;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EstoqueApiTest {
    @Autowired TestRestTemplate http;
    @Autowired ProdutoRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limpar() { produtos.deleteAll(); categorias.deleteAll(); }

    @NonNull
    private JsonNode corpo(ResponseEntity<JsonNode> resposta) {
        JsonNode corpo = resposta.getBody();
        assertThat(corpo).as("A resposta deve conter JSON").isNotNull();
        return Objects.requireNonNull(corpo);
    }

    private long categoria(String nome) {
        var resposta = http.postForEntity("/api/categorias", Map.of("nome", nome), JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation()).isNotNull();
        return corpo(resposta).path("id").asLong();
    }

    private Map<String, Object> dados(String nome, long categoriaId) {
        return Map.of("nome", nome, "preco", 49.90, "quantidadeEstoque", 10, "categoriaId", categoriaId);
    }

    private HttpEntity<Map<String, Object>> requisicao(Map<String, Object> dados) {
        return new HttpEntity<>(Objects.requireNonNull(dados));
    }

    private HttpEntity<String> json(String texto) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(texto, headers);
    }

    @Test
    void crudDeProdutosDeveRetornarCategoriaAninhadaEPreservarData() {
        long categoriaId = categoria("Informática");
        var criado = http.postForEntity("/api/produtos", dados(" Mouse ", categoriaId), JsonNode.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(criado.getHeaders().getLocation()).isNotNull();
        String rota = "/api/produtos/" + corpo(criado).path("id").asLong();
        assertThat(corpo(criado).path("nome").asText()).isEqualTo("Mouse");
        assertThat(corpo(criado).path("categoria").path("nome").asText()).isEqualTo("Informática");
        assertThat(corpo(criado).path("categoria").path("id").asLong()).isEqualTo(categoriaId);
        assertThat(corpo(http.getForEntity(rota, JsonNode.class))).isEqualTo(corpo(criado));
        assertThat(corpo(http.getForEntity("/api/produtos", JsonNode.class)).get(0).path("categoria").path("nome").asText())
            .isEqualTo("Informática");

        long outraCategoria = categoria("Acessórios");
        var atualizado = http.exchange(rota, HttpMethod.PUT, requisicao(dados("Teclado", outraCategoria)), JsonNode.class);
        assertThat(atualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(corpo(atualizado).path("nome").asText()).isEqualTo("Teclado");
        assertThat(corpo(atualizado).path("categoria").path("id").asLong()).isEqualTo(outraCategoria);
        assertThat(corpo(atualizado).path("dataCriacao")).isEqualTo(corpo(criado).path("dataCriacao"));
        var excluido = http.exchange(rota, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        assertThat(excluido.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(excluido.getBody()).isNull();
        assertThat(http.getForEntity(rota, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void crudDeCategoriasDeveFuncionarSemProdutos() {
        long id = categoria(" Papelaria ");
        String rota = "/api/categorias/" + id;
        assertThat(corpo(http.getForEntity(rota, JsonNode.class)).path("nome").asText()).isEqualTo("Papelaria");
        var atualizado = http.exchange(rota, HttpMethod.PUT, requisicao(Map.of("nome", "Escritório")), JsonNode.class);
        assertThat(atualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(corpo(atualizado).path("nome").asText()).isEqualTo("Escritório");
        assertThat(http.exchange(rota, HttpMethod.DELETE, HttpEntity.EMPTY, String.class).getStatusCode())
            .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(http.getForEntity(rota, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void categoriaComProdutoNaoPodeSerExcluida() {
        long id = categoria("Informática");
        http.postForEntity("/api/produtos", dados("Mouse", id), JsonNode.class);
        var resposta = http.exchange("/api/categorias/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(corpo(resposta).path("detail").asText()).contains("possui produtos");
        assertThat(categorias.count()).isEqualTo(1);
        assertThat(produtos.count()).isEqualTo(1);
    }

    @Test
    void deveRecusarCategoriaInexistenteSemAlterarProduto() {
        assertThat(http.postForEntity("/api/produtos", dados("Mouse", 999999), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        long id = categoria("Informática");
        var criado = http.postForEntity("/api/produtos", dados("Mouse", id), JsonNode.class);
        String rota = "/api/produtos/" + corpo(criado).path("id").asLong();
        assertThat(http.exchange(rota, HttpMethod.PUT, requisicao(dados("Outro", 999999)), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(corpo(http.getForEntity(rota, JsonNode.class)).path("nome").asText()).isEqualTo("Mouse");
    }

    @Test
    void validacaoDeveInformarOsQuatroCamposObrigatorios() {
        var resposta = http.postForEntity("/api/produtos", Map.of(), JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(corpo(resposta).path("campos").size()).isEqualTo(4);
        assertThat(corpo(resposta).path("campos").path("categoriaId").asText()).isEqualTo("Selecione uma categoria");
        assertThat(http.postForEntity("/api/categorias", Map.of("nome", " "), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deveValidarLimitesETipos() {
        for (String dados : List.of(
            "{\"nome\":\" \",\"preco\":-1,\"quantidadeEstoque\":-1,\"categoriaId\":0}",
            "{\"nome\":\"Mouse\",\"preco\":1.001,\"quantidadeEstoque\":1,\"categoriaId\":1}",
            "{\"nome\":\"Mouse\",\"preco\":10000000000,\"quantidadeEstoque\":1,\"categoriaId\":1}",
            "{\"nome\":\"Mouse\",\"preco\":1,\"quantidadeEstoque\":1.5,\"categoriaId\":1}",
            "{\"nome\":\"Mouse\",\"preco\":1,\"quantidadeEstoque\":2147483648,\"categoriaId\":1}")) {
            assertThat(http.postForEntity("/api/produtos", json(dados), JsonNode.class).getStatusCode())
                .as(dados).isEqualTo(HttpStatus.BAD_REQUEST);
        }
        assertThat(http.postForEntity("/api/categorias", Map.of("nome", "a".repeat(81)), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void jsonMalformadoCamposDesconhecidosEConteudoExtraDevemSerRecusados() {
        for (String dados : List.of("{", "{} {}", "{\"id\":123}", "{\"categoria\":\"texto antigo\"}")) {
            assertThat(http.postForEntity("/api/produtos", json(dados), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void deveRetornar404E400ParaIdsInexistentesEInvalidos() {
        long id = categoria("Teste");
        for (String recurso : List.of("produtos", "categorias")) {
            String rota = "/api/" + recurso + "/999999";
            assertThat(http.getForEntity(rota, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            Map<String, Object> payload = recurso.equals("produtos") ? dados("Teste", id) : Map.of("nome", "Nova");
            assertThat(http.exchange(rota, HttpMethod.PUT, requisicao(payload), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(http.exchange(rota, HttpMethod.DELETE, HttpEntity.EMPTY, JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(http.getForEntity("/api/" + recurso + "/abc", JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(http.getForEntity("/api/" + recurso + "/-1", JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void deveFiltrarPorNomeECategoria() {
        long informatica = categoria("Informática"), papelaria = categoria("Papelaria");
        http.postForEntity("/api/produtos", dados("Mouse", informatica), JsonNode.class);
        http.postForEntity("/api/produtos", dados("Caderno", papelaria), JsonNode.class);
        var lista = corpo(http.getForEntity("/api/produtos?nome=OUS&categoriaId=" + informatica, JsonNode.class));
        assertThat(lista.size()).isEqualTo(1);
        assertThat(lista.get(0).path("nome").asText()).isEqualTo("Mouse");
        assertThat(corpo(http.getForEntity("/api/produtos?nome=mouse&categoriaId=" + papelaria, JsonNode.class)).size()).isZero();
        assertThat(http.getForEntity("/api/produtos?categoriaId=-1", JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deveRecusarDuplicadosNaCriacaoEAtualizacao() {
        long primeira = categoria("Informática"), segunda = categoria("Papelaria");
        assertThat(http.postForEntity("/api/categorias", Map.of("nome", " informática "), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
        assertThat(http.exchange("/api/categorias/" + segunda, HttpMethod.PUT, requisicao(Map.of("nome", "Informática")), JsonNode.class)
            .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        http.postForEntity("/api/produtos", dados("Mouse", primeira), JsonNode.class);
        assertThat(http.postForEntity("/api/produtos", dados(" mouse ", primeira), JsonNode.class).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
        var outro = http.postForEntity("/api/produtos", dados("Teclado", primeira), JsonNode.class);
        assertThat(http.exchange("/api/produtos/" + corpo(outro).path("id").asLong(), HttpMethod.PUT,
            requisicao(dados("Mouse", primeira)), JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void chaveEstrangeiraDeveSerImpostaPeloBanco() {
        assertThatThrownBy(() -> jdbc.update("insert into produtos (nome, preco, quantidade_estoque, categoria_id, data_criacao) values (?, ?, ?, ?, CURRENT_TIMESTAMP)",
            "Produto órfão", 10, 1, 999999)).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(produtos.count()).isZero();
    }

    @Test
    void unicidadeDeveSerGarantidaMesmoEmRequisicoesConcorrentes() throws Exception {
        long id = categoria("Informática");
        var executor = Executors.newFixedThreadPool(2);
        var inicio = new CountDownLatch(1);
        try {
            var primeira = executor.submit(() -> { inicio.await(); return http.postForEntity("/api/produtos", dados("Mouse concorrente", id), JsonNode.class).getStatusCode().value(); });
            var segunda = executor.submit(() -> { inicio.await(); return http.postForEntity("/api/produtos", dados("MOUSE CONCORRENTE", id), JsonNode.class).getStatusCode().value(); });
            inicio.countDown();
            assertThat(List.of(primeira.get(15, TimeUnit.SECONDS), segunda.get(15, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(201, 409);
            assertThat(produtos.count()).isEqualTo(1);
        } finally { executor.shutdownNow(); }
    }

    @Test
    void deveServirInterfaceERecursosNaMesmaOrigem() {
        var pagina = http.getForEntity("/", String.class);
        assertThat(pagina.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pagina.getBody()).contains("Seus produtos", "product-category");
        assertThat(http.getForEntity("/app.js", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(http.getForEntity("/styles.css", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
