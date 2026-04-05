package com.finance.backend.repository;

import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.enums.TransactionType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

        Page<FinancialRecord> findByDeletedAtIsNull(Pageable pageable);

        @Query("SELECT fr FROM FinancialRecord fr WHERE fr.id = :id AND fr.deletedAt IS NULL")
        Optional<FinancialRecord> findByIdAndNotDeleted(@Param("id") Long id);

        // Optimistic locking
        @Lock(LockModeType.OPTIMISTIC)
        @Query("SELECT fr FROM FinancialRecord fr WHERE fr.id = :id AND fr.deletedAt IS NULL")
        Optional<FinancialRecord> findByIdWithLock(@Param("id") Long id);

        // Case-insensitive category filtering with user filter
        @Query("SELECT fr FROM FinancialRecord fr WHERE fr.deletedAt IS NULL " +
                        "AND (:userId IS NULL OR fr.user.id = :userId) " +
                        "AND (:type IS NULL OR fr.type = :type) " +
                        "AND (:category IS NULL OR LOWER(fr.category) = LOWER(:category)) " +
                        "AND (:startDate IS NULL OR fr.transactionDate >= :startDate) " +
                        "AND (:endDate IS NULL OR fr.transactionDate <= :endDate)")
        Page<FinancialRecord> findByFilters(
                        @Param("userId") Long userId,
                        @Param("type") TransactionType type,
                        @Param("category") String category,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        // Null-safe aggregations with user filter
        @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FinancialRecord fr " +
                        "WHERE fr.deletedAt IS NULL AND fr.type = :type " +
                        "AND (:userId IS NULL OR fr.user.id = :userId) " +
                        "AND (:startDate IS NULL OR fr.transactionDate >= :startDate) " +
                        "AND (:endDate IS NULL OR fr.transactionDate <= :endDate)")
        BigDecimal sumByTypeAndDateRange(
                        @Param("userId") Long userId,
                        @Param("type") TransactionType type,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Category-wise totals grouped by type, sorted by amount with user filter
        @Query("SELECT fr.category, fr.type, COALESCE(SUM(fr.amount), 0) FROM FinancialRecord fr " +
                        "WHERE fr.deletedAt IS NULL " +
                        "AND (:userId IS NULL OR fr.user.id = :userId) " +
                        "AND (:startDate IS NULL OR fr.transactionDate >= :startDate) " +
                        "AND (:endDate IS NULL OR fr.transactionDate <= :endDate) " +
                        "GROUP BY fr.category, fr.type " +
                        "ORDER BY SUM(fr.amount) DESC")
        List<Object[]> getCategoryWiseTotals(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Monthly trends with year and month grouping with user filter
        @Query("SELECT YEAR(fr.transactionDate), MONTH(fr.transactionDate), fr.type, COALESCE(SUM(fr.amount), 0) " +
                        "FROM FinancialRecord fr " +
                        "WHERE fr.deletedAt IS NULL " +
                        "AND (:userId IS NULL OR fr.user.id = :userId) " +
                        "AND (:startDate IS NULL OR fr.transactionDate >= :startDate) " +
                        "AND (:endDate IS NULL OR fr.transactionDate <= :endDate) " +
                        "GROUP BY YEAR(fr.transactionDate), MONTH(fr.transactionDate), fr.type " +
                        "ORDER BY YEAR(fr.transactionDate), MONTH(fr.transactionDate)")
        List<Object[]> getMonthlyTrends(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Recent activity with user filter
        @Query("SELECT fr FROM FinancialRecord fr WHERE fr.deletedAt IS NULL " +
                        "AND (:userId IS NULL OR fr.user.id = :userId) " +
                        "ORDER BY fr.transactionDate DESC, fr.createdAt DESC")
        Page<FinancialRecord> findRecentActivity(
                        @Param("userId") Long userId,
                        Pageable pageable);
}
