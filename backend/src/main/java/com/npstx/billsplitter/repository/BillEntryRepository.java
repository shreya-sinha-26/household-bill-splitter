package com.npstx.billsplitter.repository;

import com.npstx.billsplitter.entity.BillEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BillEntryRepository extends JpaRepository<BillEntry, Long> {

    @Query("""
            SELECT b FROM BillEntry b
            WHERE b.group.id = :groupId
              AND (:paidById IS NULL OR b.paidBy.id = :paidById)
              AND (:fromDate IS NULL OR b.date >= :fromDate)
              AND (:toDate IS NULL OR b.date <= :toDate)
            """)
    Page<BillEntry> findByGroupFiltered(
            @Param("groupId") Long groupId,
            @Param("paidById") Long paidById,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    List<BillEntry> findByGroupId(Long groupId);

    boolean existsByPaidById(Long paidById);
}
