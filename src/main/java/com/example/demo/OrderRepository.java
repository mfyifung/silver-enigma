package com.example.demo;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select o from Order o
        where (:hasStatus = false or o.status in :statuses)
          and (:hasType = false or o.type in :types)
          and (:date is null or o.date = :date)
        order by o.id
        """)
    List<Order> search(@Param("hasStatus") boolean hasStatus,
                       @Param("statuses") Collection<String> statuses,
                       @Param("hasType") boolean hasType,
                       @Param("types") Collection<String> types,
                       @Param("date") String date);

    @Query("select distinct o.type from Order o order by o.type")
    List<String> distinctTypes();

    @Query("select distinct o.status from Order o order by o.status")
    List<String> distinctStatuses();
}
