package com.npstx.billsplitter.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private List<MemberResponse> members = new ArrayList<>();

    public GroupResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<MemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<MemberResponse> members) {
        this.members = members;
    }
}
