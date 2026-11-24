package br.com.trabalho.estoque.service;

import br.com.trabalho.estoque.dto.*;
import br.com.trabalho.estoque.model.Produto;
import br.com.trabalho.estoque.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ProdutoService {
    private final ProdutoRepository repository;
    private final CategoriaService categorias;

    public ProdutoService(ProdutoRepository repository, CategoriaService categorias) {
        this.repository = repository;
        this.categorias = categorias;
    }

    public List<ProdutoResponse> listar(String nome, Long categoriaId) {
        return repository.listar(nome == null ? "" : nome.strip(), categoriaId)
            .stream().map(ProdutoResponse::de).toList();
    }

    @NonNull
    private Produto buscarEntidade(long id) {
        return Objects.requireNonNull(repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado")));
    }

    public ProdutoResponse buscar(long id) { return ProdutoResponse.de(buscarEntidade(id)); }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest dados) {
        if (repository.existsByNomeIgnoreCase(dados.nome().strip())) conflito();
        Produto produto = new Produto(dados.nome(), dados.preco(), dados.quantidadeEstoque(),
            categorias.buscarEntidade(Objects.requireNonNull(dados.categoriaId())));
        return ProdutoResponse.de(repository.saveAndFlush(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(long id, ProdutoRequest dados) {
        Produto produto = buscarEntidade(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(dados.nome().strip(), id)) conflito();
        produto.atualizar(dados.nome(), dados.preco(), dados.quantidadeEstoque(),
            categorias.buscarEntidade(Objects.requireNonNull(dados.categoriaId())));
        return ProdutoResponse.de(repository.saveAndFlush(produto));
    }

    @Transactional
    public void excluir(long id) {
        repository.delete(buscarEntidade(id));
        repository.flush();
    }

    private void conflito() {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um produto com esse nome");
    }
}
