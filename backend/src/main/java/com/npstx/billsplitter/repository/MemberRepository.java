package com.npstx.billsplitter.repository;

import com.npstx.billsplitter.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByGroupIdOrderByIdAsc(Long groupId);

    Optional<Member> findByIdAndGroupId(Long id, Long groupId);

    boolean existsByGroupIdAndNameIgnoreCase(Long groupId, String name);
}
