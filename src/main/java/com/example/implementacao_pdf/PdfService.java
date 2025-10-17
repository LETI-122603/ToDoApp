package com.example.implementacao_pdf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class PdfService {

    public enum DueFilter {
        ALL,           // todos
        NO_DUE_DATE,   // sem due date
        OVERDUE,       // due < hoje
        DUE_TODAY,     // due == hoje
        UPCOMING       // due > hoje
    }

    private final PdfRepository pdfRepository;

    PdfService(PdfRepository pdfRepository) {
        this.pdfRepository = pdfRepository;
    }

    /** Cria um novo registo com due date e status por defeito (PENDING). */
    @Transactional
    public void createPdf(String name, LocalDate dueDate) {
        createPdf(name, dueDate, Pdf.Status.PENDING);
    }

    /** Cria um novo registo com due date e status fornecido. */
    @Transactional
    public void createPdf(String name, LocalDate dueDate, Pdf.Status status) {
        var pdf = new Pdf(name, Instant.now(), dueDate);
        pdf.setStatus(Objects.requireNonNullElse(status, Pdf.Status.PENDING));
        pdfRepository.saveAndFlush(pdf);
    }

    /** Atualiza o status de um registo. */
    @Transactional
    public Pdf updateStatus(Long id, Pdf.Status status) {
        var pdf = pdfRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pdf not found: " + id));
        pdf.setStatus(Objects.requireNonNullElse(status, Pdf.Status.PENDING));
        return pdfRepository.save(pdf);
    }

    // ===== Specifications para pesquisa e filtros =====
    private Specification<Pdf> buildSpec(String nameQuery, DueFilter filter, Pdf.Status statusFilter) {
        return (root, cq, cb) -> {
            var preds = List.<Predicate>of();

            if (nameQuery != null && !nameQuery.isBlank()) {
                String like = "%" + nameQuery.trim().toLowerCase() + "%";
                preds.add(cb.like(cb.lower(root.get("name")), like));
            }

            // Filtro por Status (se definido)
            if (statusFilter != null) {
                preds.add(cb.equal(root.get("status"), statusFilter));
            }

            LocalDate today = LocalDate.now();

            switch (filter) {
                case NO_DUE_DATE -> preds.add(cb.isNull(root.get("dueDate")));
                case OVERDUE     -> preds.add(cb.lessThan(root.get("dueDate"), today));
                case DUE_TODAY   -> preds.add(cb.equal(root.get("dueDate"), today));
                case UPCOMING    -> preds.add(cb.greaterThan(root.get("dueDate"), today));
                case ALL         -> { /* sem restrição */ }
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    /** Lista paginada e ordenada com pesquisa/filtros. */
    @Transactional(readOnly = true)
    public Page<Pdf> list(String nameQuery, DueFilter filter, Pdf.Status statusFilter, Pageable pageable) {
        return pdfRepository.findAll(buildSpec(nameQuery, filter, statusFilter), pageable);
    }

    /** Contagem total com pesquisa/filtros. */
    @Transactional(readOnly = true)
    public long count(String nameQuery, DueFilter filter, Pdf.Status statusFilter) {
        return pdfRepository.count(buildSpec(nameQuery, filter, statusFilter));
    }
}
