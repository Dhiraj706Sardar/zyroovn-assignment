package com.finance.backend.service;

import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.FinancialRecordResponse;
import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository financialRecordRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(LocalDate startDate, LocalDate endDate, Long userId, String userRole) {
        // VIEWER sees only their own data, ANALYST and ADMIN see all data
        Long userIdFilter = "VIEWER".equals(userRole) ? userId : null;

        BigDecimal totalIncome = financialRecordRepository.sumByTypeAndDateRange(
                userIdFilter, TransactionType.INCOME, startDate, endDate);

        BigDecimal totalExpense = financialRecordRepository.sumByTypeAndDateRange(
                userIdFilter, TransactionType.EXPENSE, startDate, endDate);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> categoryTotals = getCategoryWiseTotals(startDate, endDate, userId, userRole);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .categoryTotals(categoryTotals)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryWiseTotals(LocalDate startDate, LocalDate endDate, Long userId, String userRole) {
        // VIEWER sees only their own data, ANALYST and ADMIN see all data
        Long userIdFilter = "VIEWER".equals(userRole) ? userId : null;

        List<Object[]> results = financialRecordRepository.getCategoryWiseTotals(userIdFilter, startDate, endDate);

        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        for (Object[] result : results) {
            String category = (String) result[0];
            TransactionType type = (TransactionType) result[1];
            BigDecimal total = (BigDecimal) result[2];
            
            // Create a key that includes both category and type
            String key = category + " (" + type.name() + ")";
            categoryTotals.put(key, total);
        }

        return categoryTotals;
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> getRecentActivity(int limit, Long userId, String userRole) {
        // VIEWER sees only their own data, ANALYST and ADMIN see all data
        Long userIdFilter = "VIEWER".equals(userRole) ? userId : null;

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("transactionDate").descending());
        List<FinancialRecord> records = financialRecordRepository.findRecentActivity(userIdFilter, pageRequest).getContent();

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, BigDecimal>> getMonthlyTrends(int months, Long userId, String userRole) {
        // VIEWER cannot access this (enforced at controller level)
        // ANALYST and ADMIN see all data
        Long userIdFilter = "VIEWER".equals(userRole) ? userId : null;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        List<FinancialRecord> records = financialRecordRepository.findByFilters(
                userIdFilter, null, null, startDate, endDate,
                PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();

        Map<String, Map<String, BigDecimal>> monthlyTrends = new HashMap<>();

        for (FinancialRecord record : records) {
            String monthKey = record.getTransactionDate().getYear() + "-" +
                    String.format("%02d", record.getTransactionDate().getMonthValue());

            monthlyTrends.putIfAbsent(monthKey, new HashMap<>());
            Map<String, BigDecimal> monthData = monthlyTrends.get(monthKey);

            String typeKey = record.getType().name().toLowerCase();
            monthData.put(typeKey, monthData.getOrDefault(typeKey, BigDecimal.ZERO).add(record.getAmount()));
        }

        // Calculate balance for each month
        for (Map<String, BigDecimal> monthData : monthlyTrends.values()) {
            BigDecimal income = monthData.getOrDefault("income", BigDecimal.ZERO);
            BigDecimal expense = monthData.getOrDefault("expense", BigDecimal.ZERO);
            monthData.put("balance", income.subtract(expense));
        }

        return monthlyTrends;
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
