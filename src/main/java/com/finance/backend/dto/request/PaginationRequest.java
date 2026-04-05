package com.finance.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    
    @Builder.Default
    private Integer limit = 20;
    
    @Builder.Default
    private Integer offset = 0;
    
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String direction = "desc";
    
    /**
     * Convert to page number (for Spring Data)
     */
    public Integer getPage() {
        if (limit != null && limit > 0) {
            return offset / limit;
        }
        return 0;
    }
    
    /**
     * Get size (for Spring Data)
     */
    public Integer getSize() {
        return limit;
    }
    
    /**
     * Validate and apply defaults
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
            sortBy = "createdAt";
        }
        if (direction == null || direction.trim().isEmpty()) {
            direction = "desc";
        }
    }
}
