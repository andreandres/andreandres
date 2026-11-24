package br.com.trabalho.estoque.dto;

import br.com.trabalho.estoque.model.Categoria;

public record CategoriaResponse(Long id, String nome) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
