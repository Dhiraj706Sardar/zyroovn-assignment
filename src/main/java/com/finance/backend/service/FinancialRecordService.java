package com.finance.backend.service;

import com.finance.backend.dto.request.FinancialRecordRequest;
import com.finance.backend.dto.response.FinancialRecordResponse;
import com.finance.backend.dto.response.PageResponse;
import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.entity.User;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.exception.ConflictException;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.exception.ValidationException;
import com.finance.backend.repository.FinancialRecordRepository;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final UserRepository userRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");
    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList("transactionDate", "amount", "createdAt");

    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordRequest request, Long userId) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Comprehensive validation
        validateFinancialRecord(request);

        // Trim string fields
        String category = request.getCategory() != null ? request.getCategory().trim() : null;
        String description = request.getDescription() != null ? request.getDescription().trim() : null;

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(category)
                .transactionDate(request.getTransactionDate())
                .description(description)
                .notes(request.getNotes())
                .user(user)
                .build();

        record = financialRecordRepository.save(record);
        log.info("Financial record created successfully: id={}, amount={}, type={}", 
                 record.getId(), record.getAmount(), record.getType());
        return mapToResponse(record);
    }

    @Transactional
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request, Long userId, String userRole) {
        FinancialRecord record = financialRecordRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));

        // VIEWER can only update their own records, ADMIN can update any record
        if ("VIEWER".equals(userRole) && !record.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Financial record not found with id: " + id);
        }

        // Validate updated fields
        if (request.getAmount() != null) {
            validateAmount(request.getAmount());
            record.setAmount(request.getAmount());
        }
        
        if (request.getType() != null) {
            record.setType(request.getType());
        }
        
        if (request.getCategory() != null) {
            String category = request.getCategory().trim();
            if (category.isEmpty() || category.length() > 50) {
                throw new ValidationException("Category must be between 1 and 50 characters");
            }
            record.setCategory(category);
        }
        
        if (request.getTransactionDate() != null) {
            if (request.getTransactionDate().isAfter(LocalDate.now())) {
                throw new ValidationException("Transaction date cannot be in the future");
            }
            record.setTransactionDate(request.getTransactionDate());
        }
        
        if (request.getDescription() != null) {
            String description = request.getDescription().trim();
            if (description.length() > 255) {
                throw new ValidationException("Description must not exceed 255 characters");
            }
            record.setDescription(description);
        }
        
        if (request.getNotes() != null) {
            if (request.getNotes().length() > 1000) {
                throw new ValidationException("Notes must not exceed 1000 characters");
            }
            record.setNotes(request.getNotes());
        }

        try {
            record = financialRecordRepository.save(record);
            log.info("Financial record updated successfully: id={}", id);
            return mapToResponse(record);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while updating financial record: {}", id);
            throw new ConflictException("Record was modified by another transaction. Please refresh and try again.");
        }
    }

    @Transactional
    public void deleteRecord(Long id, Long userId, String userRole) {
        FinancialRecord record = financialRecordRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));

        // VIEWER can only delete their own records, ADMIN can delete any record
        if ("VIEWER".equals(userRole) && !record.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Financial record not found with id: " + id);
        }

        record.setDeletedAt(LocalDateTime.now());
        
        try {
            financialRecordRepository.save(record);
            log.info("Financial record soft-deleted successfully: id={}", id);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while deleting financial record: {}", id);
            throw new ConflictException("Record was modified by another transaction. Please refresh and try again.");
        }
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id, Long currentUserId, String currentUserRole) {
        FinancialRecord record = financialRecordRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));

        // VIEWER can only see their own records
        if ("VIEWER".equals(currentUserRole) && !record.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Financial record not found with id: " + id);
        }

        return mapToResponse(record);
    }

    @Transactional(readOnly = true)
    public PageResponse<FinancialRecordResponse> getRecords(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            Integer page,
            Integer size,
            String sortBy,
            String direction,
            Long currentUserId,
            String currentUserRole) {

        // Validate pagination parameters
        int pageNumber = validatePageNumber(page);
        int pageSize = validatePageSize(size);
        
        // Validate date range
        validateDateRange(startDate, endDate);
        
        // Validate and create sort
        Sort sort = createSort(sortBy, direction);
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // VIEWER can only see their own records, ANALYST and ADMIN see all
        Long userIdFilter = "VIEWER".equals(currentUserRole) ? currentUserId : null;

        Page<FinancialRecord> recordPage = financialRecordRepository.findByFilters(
                userIdFilter, type, category, startDate, endDate, pageable);

        return PageResponse.<FinancialRecordResponse>builder()
                .content(recordPage.getContent().stream()
                        .map(this::mapToResponse)
                        .toList())
                .pageNumber(recordPage.getNumber())
                .pageSize(recordPage.getSize())
                .totalElements(recordPage.getTotalElements())
                .totalPages(recordPage.getTotalPages())
                .last(recordPage.isLast())
                .build();
    }

    /**
     * Validates all fields of a financial record
     */
    private void validateFinancialRecord(FinancialRecordRequest request) {
        // Validate amount
        validateAmount(request.getAmount());
        
        // Validate category
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new ValidationException("Category is required");
        }
        String category = request.getCategory().trim();
        if (category.length() > 50) {
            throw new ValidationException("Category must not exceed 50 characters");
        }
        
        // Validate transaction date
        if (request.getTransactionDate() == null) {
            throw new ValidationException("Transaction date is required");
        }
        if (request.getTransactionDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Transaction date cannot be in the future");
        }
        
        // Validate description length
        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new ValidationException("Description must not exceed 255 characters");
        }
        
        // Validate notes length
        if (request.getNotes() != null && request.getNotes().length() > 1000) {
            throw new ValidationException("Notes must not exceed 1000 characters");
        }
    }

    /**
     * Validates amount field
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("Amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Amount must not exceed 999,999,999,999.99");
        }
        // Check decimal places
        if (amount.scale() > 2) {
            throw new ValidationException("Amount must have at most 2 decimal places");
        }
    }

    /**
     * Validates page number
     */
    private int validatePageNumber(Integer page) {
        if (page == null) {
            return 0;
        }
        if (page < 0) {
            throw new ValidationException("Page number must be greater than or equal to 0");
        }
        return page;
    }

    /**
     * Validates and caps page size
     */
    private int validatePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (size < 1) {
            throw new ValidationException("Page size must be at least 1");
        }
        if (size > MAX_PAGE_SIZE) {
            log.info("Page size {} exceeds maximum {}, capping to maximum", size, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }
        return size;
    }

    /**
     * Validates date range
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before or equal to end date");
        }
    }

    /**
     * Creates sort object with validation
     */
    private Sort createSort(String sortBy, String direction) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "transactionDate");
        }
        
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ValidationException(
                "Invalid sort field. Allowed fields: " + String.join(", ", ALLOWED_SORT_FIELDS)
            );
        }
        
        Sort.Direction sortDirection = Sort.Direction.DESC;
        if (direction != null) {
            try {
                sortDirection = Sort.Direction.fromString(direction);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid sort direction. Use 'asc' or 'desc'");
            }
        }
        
        return Sort.by(sortDirection, sortBy);
    }

    private FinancialRecordResponse mapToResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .transactionDate(record.getTransactionDate())
                .description(record.getDescription())
                .notes(record.getNotes())
                .userId(record.getUser().getId())
                .username(record.getUser().getUsername())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
