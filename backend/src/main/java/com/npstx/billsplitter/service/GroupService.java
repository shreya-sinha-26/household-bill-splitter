package com.npstx.billsplitter.service;

import com.npstx.billsplitter.dto.request.GroupRequest;
import com.npstx.billsplitter.dto.response.GroupResponse;
import com.npstx.billsplitter.dto.response.GroupSummaryResponse;
import com.npstx.billsplitter.dto.response.MemberBalanceResponse;
import com.npstx.billsplitter.dto.response.PageResponse;
import com.npstx.billsplitter.dto.response.SettlementResponse;
import com.npstx.billsplitter.entity.BillEntry;
import com.npstx.billsplitter.entity.Group;
import com.npstx.billsplitter.entity.Member;
import com.npstx.billsplitter.exception.BusinessRuleException;
import com.npstx.billsplitter.exception.ResourceNotFoundException;
import com.npstx.billsplitter.mapper.GroupMapper;
import com.npstx.billsplitter.repository.BillEntryRepository;
import com.npstx.billsplitter.repository.GroupRepository;
import com.npstx.billsplitter.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final BillEntryRepository billEntryRepository;
    private final SplitCalculator splitCalculator;

    public GroupService(
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            BillEntryRepository billEntryRepository,
            SplitCalculator splitCalculator) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.billEntryRepository = billEntryRepository;
        this.splitCalculator = splitCalculator;
    }

    public GroupResponse create(GroupRequest request) {
        List<String> names = trimmedNames(request.getMembers());
        assertUniqueMemberNames(names);

        Group group = new Group(request.getName().trim());
        for (String name : names) {
            Member member = new Member(name, group);
            group.getMembers().add(member);
        }
        return GroupMapper.toResponse(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public PageResponse<GroupSummaryResponse> search(String search, Pageable pageable) {
        return GroupMapper.toSummaryPage(groupRepository.search(search, pageable));
    }

    @Transactional(readOnly = true)
    public GroupResponse getById(Long id) {
        return GroupMapper.toResponse(getGroupOrThrow(id));
    }

    public GroupResponse update(Long id, GroupRequest request) {
        Group group = getGroupOrThrow(id);
        List<String> names = trimmedNames(request.getMembers());
        assertUniqueMemberNames(names);

        group.setName(request.getName().trim());
        syncMembers(group, names);
        return GroupMapper.toResponse(group);
    }

    public void delete(Long id) {
        Group group = getGroupOrThrow(id);
        groupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public List<MemberBalanceResponse> getBalances(Long groupId) {
        getGroupOrThrow(groupId);
        List<Member> members = memberRepository.findByGroupIdOrderByIdAsc(groupId);
        List<BillEntry> bills = billEntryRepository.findByGroupId(groupId);
        return splitCalculator.calculateBalances(members, bills);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlements(Long groupId) {
        return splitCalculator.calculateSettlements(getBalances(groupId));
    }

    private Group getGroupOrThrow(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id " + id));
    }

    private void syncMembers(Group group, List<String> incomingNames) {
        Set<String> incomingKeys = new LinkedHashSet<>();
        for (String name : incomingNames) {
            incomingKeys.add(name.toLowerCase(Locale.ROOT));
        }

        Iterator<Member> iterator = group.getMembers().iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            String key = member.getName().toLowerCase(Locale.ROOT);
            if (!incomingKeys.contains(key)) {
                if (billEntryRepository.existsByPaidById(member.getId())) {
                    throw new BusinessRuleException(
                            "Cannot remove member '" + member.getName() + "' who has paid bills");
                }
                member.setGroup(null);
                iterator.remove();
            }
        }

        Set<String> existingKeys = new HashSet<>();
        for (Member member : group.getMembers()) {
            existingKeys.add(member.getName().toLowerCase(Locale.ROOT));
        }
        for (String name : incomingNames) {
            String key = name.toLowerCase(Locale.ROOT);
            if (!existingKeys.contains(key)) {
                Member member = new Member(name, group);
                group.getMembers().add(member);
                existingKeys.add(key);
            }
        }
    }

    private List<String> trimmedNames(List<String> names) {
        List<String> trimmed = new ArrayList<>();
        for (String name : names) {
            trimmed.add(name.trim());
        }
        return trimmed;
    }

    private void assertUniqueMemberNames(List<String> names) {
        Set<String> seen = new HashSet<>();
        for (String name : names) {
            String key = name.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                throw new BusinessRuleException("Duplicate member name within the group: " + name);
            }
        }
    }
}
