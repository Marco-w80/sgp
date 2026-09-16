package com.sgp.repository.licitacao;
import com.sgp.model.licitacao.FornecedorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface FornecedorItemRepository extends JpaRepository<FornecedorItem,Long>{Optional<FornecedorItem> findByLegadoId(Long legadoId);long deleteByFornecedorId(Long fornecedorId);}
