package com.finance.backend.entity;

import com.finance.backend.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "financial_records", indexes = {
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_transaction_date", columnList = "transactionDate"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_deleted_at", columnList = "deletedAt"),
    @Index(name = "idx_deleted_date", columnList = "deletedAt, transactionDate"),
    @Index(name = "idx_type_date", columnList = "type, transactionDate, deletedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord extends BaseEntity {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "999999999999.99", message = "Amount must not exceed 999,999,999,999.99")
    @Digits(integer = 12, fraction = 2, message = "Amount must have at most 2 decimal places")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @NotBlank(message = "Category is required")
    @Size(min = 1, max = 50, message = "Category must be between 1 and 50 characters")
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    @Column(nullable = false)
    private LocalDate transactionDate;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(length = 255)
    private String description;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    @PreUpdate
    private void trimFields() {
        if (category != null) {
            category = category.trim();
        }
        if (description != null) {
            description = description.trim();
        }
    }
}
