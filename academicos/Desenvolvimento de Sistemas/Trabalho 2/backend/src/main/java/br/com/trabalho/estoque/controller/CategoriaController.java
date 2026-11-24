package br.com.trabalho.estoque.controller;

import br.com.trabalho.estoque.dto.*;
import br.com.trabalho.estoque.service.CategoriaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service) { this.service = service; }

    @GetMapping
    public List<CategoriaResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable @Positive long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest dados) {
        CategoriaResponse categoria = service.criar(dados);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(categoria.id()).toUri()).body(categoria);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable @Positive long id, @Valid @RequestBody CategoriaRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
