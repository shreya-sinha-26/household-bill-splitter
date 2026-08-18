package com.npstx.billsplitter.dto.response;

import java.math.BigDecimal;

public class MemberBalanceResponse {

    private Long memberId;
    private String memberName;
    private BigDecimal totalPaid;
    private BigDecimal totalShare;
    private BigDecimal netBalance;

    public MemberBalanceResponse() {
    }

    public MemberBalanceResponse(
            Long memberId,
            String memberName,
            BigDecimal totalPaid,
            BigDecimal totalShare,
            BigDecimal netBalance) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.totalPaid = totalPaid;
        this.totalShare = totalShare;
        this.netBalance = netBalance;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalShare() {
        return totalShare;
    }

    public void setTotalShare(BigDecimal totalShare) {
        this.totalShare = totalShare;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
}
