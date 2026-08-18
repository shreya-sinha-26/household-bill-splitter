package com.npstx.billsplitter.dto.response;

import java.math.BigDecimal;

public class SettlementResponse {

    private MemberResponse from;
    private MemberResponse to;
    private BigDecimal amount;

    public SettlementResponse() {
    }

    public SettlementResponse(MemberResponse from, MemberResponse to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public MemberResponse getFrom() {
        return from;
    }

    public void setFrom(MemberResponse from) {
        this.from = from;
    }

    public MemberResponse getTo() {
        return to;
    }

    public void setTo(MemberResponse to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
