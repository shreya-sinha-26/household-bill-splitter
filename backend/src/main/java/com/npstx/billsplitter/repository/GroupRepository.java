package com.npstx.billsplitter.repository;

import com.npstx.billsplitter.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("""
            SELECT g FROM Group g
            WHERE :search IS NULL OR :search = ''
               OR LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Group> search(@Param("search") String search, Pageable pageable);
}
