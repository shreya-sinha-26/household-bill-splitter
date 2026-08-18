package com.npstx.billsplitter.service;

import com.npstx.billsplitter.dto.response.MemberBalanceResponse;
import com.npstx.billsplitter.dto.response.MemberResponse;
import com.npstx.billsplitter.dto.response.SettlementResponse;
import com.npstx.billsplitter.entity.BillEntry;
import com.npstx.billsplitter.entity.Member;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SplitCalculator {

    private static final int PAISE_SCALE = 2;
    private static final BigDecimal ONE_PAISA = new BigDecimal("0.01");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(PAISE_SCALE);

    public Map<Long, BigDecimal> splitAmount(BigDecimal amount, List<Member> members) {
        Map<Long, BigDecimal> shares = new LinkedHashMap<>();
        if (members == null || members.isEmpty() || amount == null) {
            return shares;
        }

        List<Member> ordered = new ArrayList<>(members);
        ordered.sort(Comparator.comparing(Member::getId));

        int memberCount = ordered.size();
        BigDecimal baseShare = amount.divide(BigDecimal.valueOf(memberCount), PAISE_SCALE, RoundingMode.DOWN);
        BigDecimal leftover = amount.subtract(baseShare.multiply(BigDecimal.valueOf(memberCount)));
        int leftoverPaise = leftover.movePointRight(PAISE_SCALE).intValueExact();

        for (int i = 0; i < memberCount; i++) {
            BigDecimal share = baseShare;
            if (i < leftoverPaise) {
                share = share.add(ONE_PAISA);
            }
            shares.put(ordered.get(i).getId(), share);
        }
        return shares;
    }

    public List<MemberBalanceResponse> calculateBalances(List<Member> members, List<BillEntry> bills) {
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }

        List<Member> ordered = new ArrayList<>(members);
        ordered.sort(Comparator.comparing(Member::getId));

        Map<Long, BigDecimal> totalPaid = new LinkedHashMap<>();
        Map<Long, BigDecimal> totalShare = new LinkedHashMap<>();
        for (Member member : ordered) {
            totalPaid.put(member.getId(), ZERO);
            totalShare.put(member.getId(), ZERO);
        }

        if (bills != null) {
            for (BillEntry bill : bills) {
                Map<Long, BigDecimal> shares = splitAmount(bill.getAmount(), ordered);
                for (Member member : ordered) {
                    totalShare.put(
                            member.getId(),
                            totalShare.get(member.getId()).add(shares.get(member.getId())));
                    if (bill.getPaidBy() != null && member.getId().equals(bill.getPaidBy().getId())) {
                        totalPaid.put(
                                member.getId(),
                                totalPaid.get(member.getId()).add(bill.getAmount()));
                    }
                }
            }
        }

        List<MemberBalanceResponse> balances = new ArrayList<>();
        for (Member member : ordered) {
            BigDecimal paid = totalPaid.get(member.getId());
            BigDecimal share = totalShare.get(member.getId());
            BigDecimal netBalance = paid.subtract(share);
            balances.add(new MemberBalanceResponse(
                    member.getId(),
                    member.getName(),
                    paid,
                    share,
                    netBalance));
        }
        return balances;
    }

    public List<SettlementResponse> calculateSettlements(List<MemberBalanceResponse> balances) {
        List<OpenBalance> debtors = new ArrayList<>();
        List<OpenBalance> creditors = new ArrayList<>();
        if (balances != null) {
            for (MemberBalanceResponse balance : balances) {
                int sign = balance.getNetBalance().compareTo(ZERO);
                if (sign < 0) {
                    debtors.add(new OpenBalance(
                            balance.getMemberId(),
                            balance.getMemberName(),
                            balance.getNetBalance()));
                } else if (sign > 0) {
                    creditors.add(new OpenBalance(
                            balance.getMemberId(),
                            balance.getMemberName(),
                            balance.getNetBalance()));
                }
            }
        }

        List<SettlementResponse> settlements = new ArrayList<>();
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            debtors.sort(Comparator.comparing(OpenBalance::getRemaining));
            creditors.sort(Comparator.comparing(OpenBalance::getRemaining).reversed());

            OpenBalance debtor = debtors.get(0);
            OpenBalance creditor = creditors.get(0);
            BigDecimal transfer = debtor.getRemaining().abs().min(creditor.getRemaining());

            settlements.add(new SettlementResponse(
                    new MemberResponse(debtor.getMemberId(), debtor.getMemberName()),
                    new MemberResponse(creditor.getMemberId(), creditor.getMemberName()),
                    transfer));

            debtor.setRemaining(debtor.getRemaining().add(transfer));
            creditor.setRemaining(creditor.getRemaining().subtract(transfer));

            if (debtor.getRemaining().compareTo(ZERO) == 0) {
                debtors.remove(0);
            }
            if (creditor.getRemaining().compareTo(ZERO) == 0) {
                creditors.remove(0);
            }
        }
        return settlements;
    }

    private static final class OpenBalance {

        private final Long memberId;
        private final String memberName;
        private BigDecimal remaining;

        private OpenBalance(Long memberId, String memberName, BigDecimal remaining) {
            this.memberId = memberId;
            this.memberName = memberName;
            this.remaining = remaining;
        }

        private Long getMemberId() {
            return memberId;
        }

        private String getMemberName() {
            return memberName;
        }

        private BigDecimal getRemaining() {
            return remaining;
        }

        private void setRemaining(BigDecimal remaining) {
            this.remaining = remaining;
        }
    }
}
