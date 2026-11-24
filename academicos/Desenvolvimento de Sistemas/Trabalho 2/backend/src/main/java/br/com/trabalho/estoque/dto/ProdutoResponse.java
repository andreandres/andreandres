package br.com.trabalho.estoque.dto;

import br.com.trabalho.estoque.model.Produto;
import java.math.BigDecimal;
import java.time.Instant;

public record ProdutoResponse(Long id, String nome, BigDecimal preco, Integer quantidadeEstoque,
        CategoriaResponse categoria, Instant dataCriacao) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getPreco(),
            produto.getQuantidadeEstoque(), CategoriaResponse.de(produto.getCategoria()), produto.getDataCriacao());
    }
}
