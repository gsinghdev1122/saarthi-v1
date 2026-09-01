package com.csd.canteen.service;

import com.csd.canteen.dto.response.ApprovalResponse;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final CanteenMapper mapper;

    @Transactional(readOnly = true)
    public List<ApprovalResponse> listAll() {
        return approvalRepository.findAllByOrderBySubmittedAtDesc().stream().map(mapper::toResponse).toList();
    }
}
