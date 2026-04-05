package com.finance.backend.controller;

import com.finance.backend.dto.request.FinancialRecordRequest;
import com.finance.backend.dto.response.FinancialRecordResponse;
import com.finance.backend.dto.response.PageResponse;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.security.UserPrincipal;
import com.finance.backend.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "Financial record management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class FinancialRecordController {

        private final FinancialRecordService financialRecordService;

        @PostMapping
        @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN')")
        @Operation(summary = "Create financial record", description = "Create a new financial record. VIEWER can create their own records, ADMIN can create for any user.")
        public ResponseEntity<FinancialRecordResponse> createRecord(
                        @Valid @RequestBody FinancialRecordRequest request,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                return new ResponseEntity<>(
                                financialRecordService.createRecord(request, userPrincipal.getId()),
                                HttpStatus.CREATED);
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
        @Operation(summary = "Get financial records", description = "Retrieve financial records with filters and pagination. VIEWER sees only their own records, ANALYST and ADMIN see all records. "
                        +
                        "Pagination: Use limit (default 20, max 100) and offset (default 0). Or use page and size.")
        public ResponseEntity<PageResponse<FinancialRecordResponse>> getRecords(
                        @RequestParam(required = false) TransactionType type,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Integer limit,
                        @RequestParam(required = false) Integer offset,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size,
                        @RequestParam(required = false) String sortBy,
                        @RequestParam(required = false) String direction,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {

                // Support both limit/offset and page/size
                Integer finalPage = page;
                Integer finalSize = size;

                if (limit != null || offset != null) {
                        // Convert limit/offset to page/size
                        finalSize = limit;
                        finalPage = (offset != null && finalSize != null && finalSize > 0) ? offset / finalSize : 0;
                }

                return ResponseEntity.ok(
                                financialRecordService.getRecords(
                                                type, category, startDate, endDate, finalPage, finalSize, sortBy,
                                                direction,
                                                userPrincipal.getId(), userPrincipal.getRole().name()));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
        @Operation(summary = "Get financial record by ID", description = "Retrieve a financial record by ID. VIEWER can only see their own records, ANALYST and ADMIN can see all records.")
        public ResponseEntity<FinancialRecordResponse> getRecordById(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                return ResponseEntity.ok(
                                financialRecordService.getRecordById(id, userPrincipal.getId(),
                                                userPrincipal.getRole().name()));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN')")
        @Operation(summary = "Update financial record", description = "Update a financial record. VIEWER can update only their own records, ADMIN can update any record.")
        public ResponseEntity<FinancialRecordResponse> updateRecord(
                        @PathVariable Long id,
                        @Valid @RequestBody FinancialRecordRequest request,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                return ResponseEntity.ok(financialRecordService.updateRecord(id, request, userPrincipal.getId(),
                                userPrincipal.getRole().name()));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN')")
        @Operation(summary = "Delete financial record", description = "Soft delete a financial record. VIEWER can delete only their own records, ADMIN can delete any record.")
        public ResponseEntity<Void> deleteRecord(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                financialRecordService.deleteRecord(id, userPrincipal.getId(), userPrincipal.getRole().name());
                return ResponseEntity.noContent().build();
        }
}
