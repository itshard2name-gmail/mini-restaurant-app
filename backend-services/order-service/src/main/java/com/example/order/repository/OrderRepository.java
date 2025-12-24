package com.example.order.repository;

import com.example.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
        List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

        List<Order> findByUserIdAndStatusIn(String userId, List<String> statuses,
                        org.springframework.data.domain.Sort sort);

        org.springframework.data.domain.Page<Order> findByUserIdAndStatusIn(String userId, List<String> statuses,
                        org.springframework.data.domain.Pageable pageable);

        // Analytics

        @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.createdAt >= :date")
        BigDecimal sumTotalPriceAfter(@Param("date") Instant date);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :date")
        Long countOrdersAfter(@Param("date") Instant date);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
        Long countOrdersByStatus(@Param("statuses") List<String> statuses);

        // Group By Date: Returns [Date(String), TotalRevenue(BigDecimal), Count(Long)]
        // NOTE: DATE_FORMAT on Instant (UTC) works, but groups by UTC Date.
        // Ideal long term is CONVERT_TZ, but for MVP this ensures strict UTC grouping.
        @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d') as date, SUM(o.totalPrice), COUNT(o) " +
                        "FROM Order o WHERE o.createdAt >= :date " +
                        "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d') " +
                        "ORDER BY date ASC")
        List<Object[]> getDailyRevenueTrends(@Param("date") Instant date);

        // Top Items: Returns [ItemName(String), TotalQuantity(Long)]
        @Query("SELECT oi.snapshotName, SUM(oi.quantity) " +
                        "FROM OrderItem oi " +
                        "GROUP BY oi.snapshotName " +
                        "ORDER BY SUM(oi.quantity) DESC")
        List<Object[]> getTopSellingItems(org.springframework.data.domain.Pageable pageable);

        // Distribution by OrderType: Returns [OrderType(Enum), Count(Long),
        // Revenue(BigDecimal)]
        @Query("SELECT o.orderType, COUNT(o), SUM(o.totalPrice) " +
                        "FROM Order o WHERE o.createdAt >= :date " +
                        "GROUP BY o.orderType")
        List<Object[]> getOrderDistribution(@Param("date") Instant date);

        // Phase 20: Server-Side Search & Pagination
        @Query("SELECT o FROM Order o WHERE " +
                        "(:status IS NULL OR o.status = :status) AND " +
                        "(:date IS NULL OR FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d') = :date) AND " +
                        "(:query IS NULL OR (CAST(o.id AS string) LIKE %:query% OR LOWER(o.userId) LIKE %:query%))")
        org.springframework.data.domain.Page<Order> findOrders(
                        @Param("status") String status,
                        @Param("date") String date,
                        @Param("query") String query,
                        org.springframework.data.domain.Pageable pageable);
}
