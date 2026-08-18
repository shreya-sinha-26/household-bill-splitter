package com.npstx.billsplitter.service;

import com.npstx.billsplitter.dto.request.BillRequest;
import com.npstx.billsplitter.dto.response.BillResponse;
import com.npstx.billsplitter.dto.response.PageResponse;
import com.npstx.billsplitter.entity.BillEntry;
import com.npstx.billsplitter.entity.Group;
import com.npstx.billsplitter.entity.Member;
import com.npstx.billsplitter.exception.BusinessRuleException;
import com.npstx.billsplitter.exception.ResourceNotFoundException;
import com.npstx.billsplitter.mapper.BillMapper;
import com.npstx.billsplitter.repository.BillEntryRepository;
import com.npstx.billsplitter.repository.GroupRepository;
import com.npstx.billsplitter.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class BillEntryService {

    private final BillEntryRepository billEntryRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public BillEntryService(
            BillEntryRepository billEntryRepository,
            GroupRepository groupRepository,
            MemberRepository memberRepository) {
        this.billEntryRepository = billEntryRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    public BillResponse create(Long groupId, BillRequest request) {
        Group group = getGroupOrThrow(groupId);
        Member paidBy = resolvePayer(request.getPaidById(), groupId);
        BillEntry bill = new BillEntry(
                request.getDescription().trim(),
                request.getAmount(),
                paidBy,
                request.getDate(),
                group);
        return BillMapper.toResponse(billEntryRepository.save(bill));
    }

    @Transactional(readOnly = true)
    public PageResponse<BillResponse> list(
            Long groupId,
            Long paidById,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {
        getGroupOrThrow(groupId);
        return BillMapper.toPage(
                billEntryRepository.findByGroupFiltered(groupId, paidById, from, to, pageable));
    }

    @Transactional(readOnly = true)
    public BillResponse getById(Long id) {
        return BillMapper.toResponse(getBillOrThrow(id));
    }

    public BillResponse update(Long id, BillRequest request) {
        BillEntry bill = getBillOrThrow(id);
        Member paidBy = resolvePayer(request.getPaidById(), bill.getGroup().getId());
        bill.setDescription(request.getDescription().trim());
        bill.setAmount(request.getAmount());
        bill.setPaidBy(paidBy);
        bill.setDate(request.getDate());
        return BillMapper.toResponse(bill);
    }

    public void delete(Long id) {
        BillEntry bill = getBillOrThrow(id);
        billEntryRepository.delete(bill);
    }

    private Group getGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id " + groupId));
    }

    private BillEntry getBillOrThrow(Long id) {
        return billEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + id));
    }

    private Member resolvePayer(Long paidById, Long groupId) {
        Member member = memberRepository.findById(paidById)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + paidById));
        if (!groupId.equals(member.getGroup().getId())) {
            throw new BusinessRuleException("paidById is not a member of this group");
        }
        return member;
    }
}
