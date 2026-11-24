package br.com.trabalho.estoque.controller;

import br.com.trabalho.estoque.dto.*;
import br.com.trabalho.estoque.service.ProdutoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService service;
    public ProdutoController(ProdutoService service) { this.service = service; }

    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(required = false) String nome,
            @RequestParam(required = false) @Positive Long categoriaId) {
        return service.listar(nome, categoriaId);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable @Positive long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest dados) {
        ProdutoResponse produto = service.criar(dados);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(produto.id()).toUri()).body(produto);
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable @Positive long id, @Valid @RequestBody ProdutoRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
