package br.com.trabalho.estoque.service;

import br.com.trabalho.estoque.dto.*;
import br.com.trabalho.estoque.model.Categoria;
import br.com.trabalho.estoque.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository repository;
    private final ProdutoRepository produtos;

    public CategoriaService(CategoriaRepository repository, ProdutoRepository produtos) {
        this.repository = repository;
        this.produtos = produtos;
    }

    public List<CategoriaResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(CategoriaResponse::de).toList();
    }

    @NonNull
    public Categoria buscarEntidade(long id) {
        return Objects.requireNonNull(repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada")));
    }

    public CategoriaResponse buscar(long id) { return CategoriaResponse.de(buscarEntidade(id)); }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest dados) {
        if (repository.existsByNomeIgnoreCase(dados.nome().strip())) conflito();
        return CategoriaResponse.de(repository.saveAndFlush(new Categoria(dados.nome())));
    }

    @Transactional
    public CategoriaResponse atualizar(long id, CategoriaRequest dados) {
        Categoria categoria = buscarEntidade(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(dados.nome().strip(), id)) conflito();
        categoria.atualizar(dados.nome());
        return CategoriaResponse.de(repository.saveAndFlush(categoria));
    }

    @Transactional
    public void excluir(long id) {
        Categoria categoria = buscarEntidade(id);
        if (produtos.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Esta categoria possui produtos. Reclassifique ou exclua os produtos antes de removê-la");
        }
        repository.delete(Objects.requireNonNull(categoria));
        repository.flush();
    }

    private void conflito() {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome");
    }
}
