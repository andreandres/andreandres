package com.suaapi.controller;

import com.suaapi.model.Livro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final List<Livro> livros;

    public LivroController() {
        livros = new ArrayList<>();
        livros.add(new Livro(1L, "Dom Casmurro", "Machado de Assis"));
        livros.add(new Livro(2L, "O Cortico", "Aluisio Azevedo"));
    }

    @GetMapping
    public ResponseEntity<List<Livro>> listarLivros() {
        return ResponseEntity.ok(livros);
    }

    @PostMapping
    public ResponseEntity<Livro> cadastrarLivro(@RequestBody Livro livro) {
        livros.add(livro);
        return ResponseEntity.status(HttpStatus.CREATED).body(livro);
    }
}