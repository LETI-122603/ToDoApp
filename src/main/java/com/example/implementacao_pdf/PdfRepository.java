package com.example.implementacao_pdf;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositório Spring Data JPA para a entidade {@link Pdf}.
 * Permite operações CRUD, paginação e Specifications.
 */
interface PdfRepository extends JpaRepository<Pdf, Long>, JpaSpecificationExecutor<Pdf> {

    /**
     * Lista "simples" paginada (sem filtros). Mantida por compatibilidade.
     */
    Slice<Pdf> findAllBy(Pageable pageable);
}