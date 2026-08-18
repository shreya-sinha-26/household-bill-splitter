package com.npstx.billsplitter.controller;

import com.npstx.billsplitter.dto.request.GroupRequest;
import com.npstx.billsplitter.dto.response.GroupResponse;
import com.npstx.billsplitter.dto.response.GroupSummaryResponse;
import com.npstx.billsplitter.dto.response.MemberBalanceResponse;
import com.npstx.billsplitter.dto.response.PageResponse;
import com.npstx.billsplitter.dto.response.SettlementResponse;
import com.npstx.billsplitter.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(request));
    }

    @GetMapping
    public PageResponse<GroupSummaryResponse> search(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return groupService.search(search, pageable);
    }

    @GetMapping("/{id}")
    public GroupResponse getById(@PathVariable Long id) {
        return groupService.getById(id);
    }

    @PutMapping("/{id}")
    public GroupResponse update(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return groupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/balances")
    public List<MemberBalanceResponse> getBalances(@PathVariable Long groupId) {
        return groupService.getBalances(groupId);
    }

    @GetMapping("/{groupId}/settlements")
    public List<SettlementResponse> getSettlements(@PathVariable Long groupId) {
        return groupService.getSettlements(groupId);
    }
}
