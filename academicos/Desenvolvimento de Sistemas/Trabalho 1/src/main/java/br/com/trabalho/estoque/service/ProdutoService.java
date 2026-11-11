package br.com.trabalho.estoque.service;

import br.com.trabalho.estoque.dto.ProdutoRequest;
import br.com.trabalho.estoque.model.Produto;
import br.com.trabalho.estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ProdutoService {
    private final ProdutoRepository repository;
    public ProdutoService(ProdutoRepository repository) { this.repository = repository; }

    public List<Produto> listar(String nome) {
        return repository.findByNomeContainingIgnoreCaseOrderByIdAsc(nome == null ? "" : nome.strip());
    }

    @NonNull
    public Produto buscar(@NonNull Long id) {
        Produto produto = repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        return Objects.requireNonNull(produto);
    }

    @Transactional
    public Produto criar(ProdutoRequest dados) {
        verificarNome(dados.nome(), null);
        return repository.saveAndFlush(new Produto(dados.nome(), dados.preco(), dados.quantidadeEstoque(), dados.categoria()));
    }

    @Transactional
    public Produto atualizar(@NonNull Long id, ProdutoRequest dados) {
        Produto produto = buscar(id);
        verificarNome(dados.nome(), id);
        produto.atualizar(dados.nome(), dados.preco(), dados.quantidadeEstoque(), dados.categoria());
        return repository.saveAndFlush(produto);
    }

    @Transactional
    public void excluir(@NonNull Long id) { repository.delete(buscar(id)); }

    private void verificarNome(String nome, Long id) {
        boolean existe = id == null ? repository.existsByNomeIgnoreCase(nome.strip())
            : repository.existsByNomeIgnoreCaseAndIdNot(nome.strip(), id);
        if (existe) throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um produto com esse nome");
    }
}
