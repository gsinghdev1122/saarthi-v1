package com.csd.canteen.repository;

import com.csd.canteen.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findAllByOrderBySubmittedAtDesc();
    long countByStatus(String status);
}
