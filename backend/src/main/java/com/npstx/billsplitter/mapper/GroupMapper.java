package com.npstx.billsplitter.mapper;

import com.npstx.billsplitter.dto.response.GroupResponse;
import com.npstx.billsplitter.dto.response.GroupSummaryResponse;
import com.npstx.billsplitter.dto.response.MemberResponse;
import com.npstx.billsplitter.dto.response.PageResponse;
import com.npstx.billsplitter.entity.Group;
import com.npstx.billsplitter.entity.Member;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class GroupMapper {

    private GroupMapper() {
    }

    public static GroupResponse toResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setCreatedAt(group.getCreatedAt());
        List<MemberResponse> members = new ArrayList<>();
        if (group.getMembers() != null) {
            for (Member member : group.getMembers()) {
                members.add(toMemberResponse(member));
            }
        }
        response.setMembers(members);
        return response;
    }

    public static GroupSummaryResponse toSummary(Group group) {
        GroupSummaryResponse response = new GroupSummaryResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setCreatedAt(group.getCreatedAt());
        int memberCount = group.getMembers() == null ? 0 : group.getMembers().size();
        response.setMemberCount(memberCount);
        return response;
    }

    public static MemberResponse toMemberResponse(Member member) {
        return new MemberResponse(member.getId(), member.getName());
    }

    public static PageResponse<GroupSummaryResponse> toSummaryPage(Page<Group> page) {
        List<GroupSummaryResponse> content = new ArrayList<>();
        for (Group group : page.getContent()) {
            content.add(toSummary(group));
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
