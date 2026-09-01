package com.csd.canteen.repository;

import com.csd.canteen.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    List<ImportBatch> findAllByOrderByUploadedAtDesc(Pageable pageable);
    ImportBatch findFirstByOrderByUploadedAtDesc();
}
