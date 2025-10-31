package com.example.implementacao_pdf;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pdf")
public class Pdf {

    public enum Status {
        PENDING, PRINTED, SENT, CANCELED
    }

    /** Identificador único da impressora PDF. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "pdf_id")
    private Long id;

    /** Nome da impressora PDF. */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Data e hora de criação do registro. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Data de vencimento definida pelo usuário. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Estado do registro. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    /** Construtor protegido para uso do Hibernate. */
    protected Pdf() {
        // Hibernate needs a no-argument constructor
    }

    /**
     * Construtor principal.
     * @param name Nome da impressora
     * @param createdAt Data de criação
     * @param dueDate Data de vencimento
     */
    public Pdf(String name, Instant createdAt, LocalDate dueDate) {
        setName(name);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.dueDate = dueDate;
        this.status = Status.PENDING;
    }

    /** PrePersist para garantir campos base. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = Status.PENDING;
    }

    /** Retorna o ID da impressora PDF. */
    public @Nullable Long getId() {
        return id;
    }

    /** Retorna o nome da impressora PDF. */
    public String getName() {
        return name;
    }

    /**
     * Define o nome da impressora PDF.
     * @param name Nome
     */
    public void setName(String name) {
        this.name = (name == null || name.isEmpty()) ? "" : name.substring(0, Math.min(name.length(), 100));
    }

    /** Retorna a data de criação. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Retorna a data de vencimento. */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /** Define a data de vencimento. */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /** Retorna o status. */
    public Status getStatus() {
        return status;
    }

    /** Define o status. */
    public void setStatus(Status status) {
        this.status = status != null ? status : Status.PENDING;
    }

    /** Igualdade por ID. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pdf other = (Pdf) obj;
        return Objects.equals(id, other.id);
    }

    /** Hashcode baseado no ID. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
