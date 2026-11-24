package br.com.trabalho.estoque.repository;

import br.com.trabalho.estoque.model.Produto;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    @EntityGraph(attributePaths = "categoria")
    @Query("""
        select p from Produto p
        where (:nome = '' or lower(p.nome) like lower(concat('%', :nome, '%')))
          and (:categoriaId is null or p.categoria.id = :categoriaId)
        order by p.id
        """)
    List<Produto> listar(@Param("nome") String nome, @Param("categoriaId") Long categoriaId);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, long id);
    boolean existsByCategoriaId(long categoriaId);
}
