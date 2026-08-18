package com.npstx.billsplitter.controller;

import com.npstx.billsplitter.dto.request.BillRequest;
import com.npstx.billsplitter.dto.response.BillResponse;
import com.npstx.billsplitter.dto.response.PageResponse;
import com.npstx.billsplitter.service.BillEntryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class BillEntryController {

    private final BillEntryService billEntryService;

    public BillEntryController(BillEntryService billEntryService) {
        this.billEntryService = billEntryService;
    }

    @PostMapping("/api/groups/{groupId}/bills")
    public ResponseEntity<BillResponse> create(
            @PathVariable Long groupId,
            @Valid @RequestBody BillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billEntryService.create(groupId, request));
    }

    @GetMapping("/api/groups/{groupId}/bills")
    public PageResponse<BillResponse> list(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long paidById,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return billEntryService.list(groupId, paidById, from, to, pageable);
    }

    @GetMapping("/api/bills/{id}")
    public BillResponse getById(@PathVariable Long id) {
        return billEntryService.getById(id);
    }

    @PutMapping("/api/bills/{id}")
    public BillResponse update(@PathVariable Long id, @Valid @RequestBody BillRequest request) {
        return billEntryService.update(id, request);
    }

    @DeleteMapping("/api/bills/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
