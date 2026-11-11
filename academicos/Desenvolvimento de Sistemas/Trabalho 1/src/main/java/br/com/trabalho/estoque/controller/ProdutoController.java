package br.com.trabalho.estoque.controller;

import br.com.trabalho.estoque.dto.ProdutoRequest;
import br.com.trabalho.estoque.model.Produto;
import br.com.trabalho.estoque.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService service;
    public ProdutoController(ProdutoService service) { this.service = service; }

    @GetMapping
    public List<Produto> listar(@RequestParam(required = false) String nome) { return service.listar(nome); }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable @NonNull Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody ProdutoRequest dados) {
        Produto produto = service.criar(dados);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(produto.getId()).toUri()).body(produto);
    }

    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable @NonNull Long id, @Valid @RequestBody ProdutoRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @NonNull Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
