package com.npstx.billsplitter.service;

import com.npstx.billsplitter.dto.request.BillRequest;
import com.npstx.billsplitter.dto.response.BillResponse;
import com.npstx.billsplitter.entity.BillEntry;
import com.npstx.billsplitter.entity.Group;
import com.npstx.billsplitter.entity.Member;
import com.npstx.billsplitter.exception.BusinessRuleException;
import com.npstx.billsplitter.exception.ResourceNotFoundException;
import com.npstx.billsplitter.repository.BillEntryRepository;
import com.npstx.billsplitter.repository.GroupRepository;
import com.npstx.billsplitter.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillEntryServiceTest {

    @Mock
    private BillEntryRepository billEntryRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BillEntryService billEntryService;

    private Group group;
    private Member alice;
    private BillRequest request;

    @BeforeEach
    void setUp() {
        group = new Group("Room 12B");
        group.setId(1L);

        alice = new Member("Alice", group);
        alice.setId(10L);

        request = new BillRequest();
        request.setDescription("Weekly groceries");
        request.setAmount(new BigDecimal("100.00"));
        request.setPaidById(10L);
        request.setDate(LocalDate.of(2026, 8, 5));
    }

    @Test
    void create_groupMissing_throws404() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> billEntryService.create(1L, request));

        assertEquals("Group not found with id 1", ex.getMessage());
    }

    @Test
    void create_memberMissing_throws404() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberRepository.findById(10L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> billEntryService.create(1L, request));

        assertEquals("Member not found with id 10", ex.getMessage());
    }

    @Test
    void create_paidByInAnotherGroup_throws409() {
        Group otherGroup = new Group("Other");
        otherGroup.setId(2L);
        Member outsider = new Member("Diya", otherGroup);
        outsider.setId(10L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(outsider));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> billEntryService.create(1L, request));

        assertEquals("paidById is not a member of this group", ex.getMessage());
    }

    @Test
    void create_validPayer_savesBill() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(alice));
        when(billEntryRepository.save(any(BillEntry.class))).thenAnswer(invocation -> {
            BillEntry bill = invocation.getArgument(0);
            bill.setId(99L);
            return bill;
        });

        BillResponse response = billEntryService.create(1L, request);

        assertEquals(99L, response.getId());
        assertEquals("Weekly groceries", response.getDescription());
        assertEquals(0, new BigDecimal("100.00").compareTo(response.getAmount()));
        assertEquals(10L, response.getPaidBy().getId());
        assertEquals(1L, response.getGroupId());
        verify(billEntryRepository).save(any(BillEntry.class));
    }

    @Test
    void getById_missingBill_throws404() {
        when(billEntryRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> billEntryService.getById(99L));

        assertEquals("Bill not found with id 99", ex.getMessage());
    }
}
