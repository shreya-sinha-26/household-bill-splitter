package com.npstx.billsplitter.mapper;

import com.npstx.billsplitter.dto.response.BillResponse;
import com.npstx.billsplitter.dto.response.PageResponse;
import com.npstx.billsplitter.entity.BillEntry;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class BillMapper {

    private BillMapper() {
    }

    public static BillResponse toResponse(BillEntry bill) {
        BillResponse response = new BillResponse();
        response.setId(bill.getId());
        response.setDescription(bill.getDescription());
        response.setAmount(bill.getAmount());
        if (bill.getPaidBy() != null) {
            response.setPaidBy(GroupMapper.toMemberResponse(bill.getPaidBy()));
        }
        response.setDate(bill.getDate());
        if (bill.getGroup() != null) {
            response.setGroupId(bill.getGroup().getId());
        }
        response.setCreatedAt(bill.getCreatedAt());
        return response;
    }

    public static PageResponse<BillResponse> toPage(Page<BillEntry> page) {
        List<BillResponse> content = new ArrayList<>();
        for (BillEntry bill : page.getContent()) {
            content.add(toResponse(bill));
        }
        return PageResponse.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
