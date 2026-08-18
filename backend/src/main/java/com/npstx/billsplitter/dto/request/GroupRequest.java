package com.npstx.billsplitter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class GroupRequest {

    @NotBlank
    @Size(min = 2, max = 60)
    private String name;

    @NotEmpty
    @Size(max = 50)
    private List<@NotBlank @Size(max = 40) String> members;

    public GroupRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
