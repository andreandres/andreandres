package br.com.trabalho.estoque.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProdutoRequest(
    @NotBlank(message = "O nome é obrigatório") @Size(max = 120) String nome,
    @NotNull(message = "O preço é obrigatório") @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal preco,
    @NotNull(message = "A quantidade é obrigatória") @Min(0) Integer quantidadeEstoque,
    @Size(max = 80) String categoria
) {}
