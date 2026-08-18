package com.npstx.billsplitter.service;

import com.npstx.billsplitter.dto.response.MemberBalanceResponse;
import com.npstx.billsplitter.entity.BillEntry;
import com.npstx.billsplitter.entity.Group;
import com.npstx.billsplitter.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitCalculatorTest {

    private SplitCalculator calculator;
    private Member alice;
    private Member bob;
    private Member charlie;
    private Group group;

    @BeforeEach
    void setUp() {
        calculator = new SplitCalculator();
        group = new Group("Room 12B");
        group.setId(1L);
        alice = member(1L, "Alice");
        bob = member(2L, "Bob");
        charlie = member(3L, "Charlie");
    }

    @Test
    void splitAmount_oneHundredAmongThree_sharesSumToAmountExactly() {
        List<Member> shuffled = List.of(charlie, alice, bob);
        BigDecimal amount = new BigDecimal("100.00");

        Map<Long, BigDecimal> shares = calculator.splitAmount(amount, shuffled);

        assertEquals(0, new BigDecimal("33.34").compareTo(shares.get(1L)));
        assertEquals(0, new BigDecimal("33.33").compareTo(shares.get(2L)));
        assertEquals(0, new BigDecimal("33.33").compareTo(shares.get(3L)));
        assertEquals(0, amount.compareTo(sum(shares)));
    }

    @Test
    void splitAmount_evenSplit_noLeftoverPaise() {
        Map<Long, BigDecimal> shares = calculator.splitAmount(
                new BigDecimal("100.00"),
                List.of(alice, bob));

        assertEquals(0, new BigDecimal("50.00").compareTo(shares.get(1L)));
        assertEquals(0, new BigDecimal("50.00").compareTo(shares.get(2L)));
        assertEquals(0, new BigDecimal("100.00").compareTo(sum(shares)));
    }

    @Test
    void calculateBalances_netBalancesSumToZero() {
        BillEntry groceries = bill(alice, new BigDecimal("100.00"));
        BillEntry electricity = bill(bob, new BigDecimal("50.00"));
        List<Member> members = List.of(alice, bob, charlie);

        List<MemberBalanceResponse> balances = calculator.calculateBalances(
                members,
                List.of(groceries, electricity));

        BigDecimal netSum = BigDecimal.ZERO;
        for (MemberBalanceResponse balance : balances) {
            netSum = netSum.add(balance.getNetBalance());
        }
        assertEquals(0, BigDecimal.ZERO.compareTo(netSum));
        assertEquals(3, balances.size());
    }

    @Test
    void calculateBalances_zeroMembers_returnsEmptyWithoutDividing() {
        BillEntry bill = bill(alice, new BigDecimal("100.00"));

        List<MemberBalanceResponse> balances = calculator.calculateBalances(
                List.of(),
                List.of(bill));

        assertTrue(balances.isEmpty());
    }

    @Test
    void calculateBalances_zeroBills_allBalancesAreZero() {
        List<MemberBalanceResponse> balances = calculator.calculateBalances(
                List.of(alice, bob, charlie),
                List.of());

        assertEquals(3, balances.size());
        for (MemberBalanceResponse balance : balances) {
            assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalPaid()));
            assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalShare()));
            assertEquals(0, BigDecimal.ZERO.compareTo(balance.getNetBalance()));
        }
    }

    private Member member(Long id, String name) {
        Member member = new Member(name, group);
        member.setId(id);
        return member;
    }

    private BillEntry bill(Member paidBy, BigDecimal amount) {
        return new BillEntry("Test bill", amount, paidBy, LocalDate.of(2026, 8, 5), group);
    }

    private BigDecimal sum(Map<Long, BigDecimal> shares) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal share : shares.values()) {
            total = total.add(share);
        }
        return total;
    }
}
