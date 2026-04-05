package com.finance.backend.dto.request;

import com.finance.backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordFilterRequest {
    
    private TransactionType type;
    
    private String category;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    @Builder.Default
    private Integer limit = 20;
    
    @Builder.Default
    private Integer offset = 0;
    
    @Builder.Default
    private String sortBy = "transactionDate";
    
    @Builder.Default
    private String direction = "desc";
    
    /**
     * Get page number for Spring Data
     */
    public Integer getPage() {
        if (limit != null && limit > 0) {
            return offset / limit;
        }
        return 0;
    }
    
    /**
     * Get size for Spring Data
     */
    public Integer getSize() {
        return limit;
    }
    
    /**
     * Apply defaults and validate
     */
    public void applyDefaults() {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100; // Max limit
        }
        if (offset == null || offset < 0) {
            offset = 0;
        }
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "transactionDate";
        }
        if (direction == null || direction.trim().isEmpty()) {
            direction = "desc";
        }
    }
}
