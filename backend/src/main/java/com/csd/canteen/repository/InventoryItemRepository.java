package com.csd.canteen.repository;

import com.csd.canteen.entity.InventoryItem;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByIndexNoAndCanteen(String indexNo, String canteen);

    @Query("""
           select i from InventoryItem i
           where (:division is null or i.division = :division)
             and (:search is null
                  or lower(i.name) like :search
                  or lower(i.indexNo) like :search)
           """)
    List<InventoryItem> search(
            @Param("division") String division, 
            @Param("search") String search, 
            Pageable pageable
    );

    long countByStatus(String status);
}
