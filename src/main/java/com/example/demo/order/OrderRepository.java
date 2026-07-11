package com.example.demo.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query("select distinct o.type from Order o order by o.type")
    List<String> findDistinctTypes();

    @Query("select distinct o.status from Order o order by o.status")
    List<String> findDistinctStatuses();
}
