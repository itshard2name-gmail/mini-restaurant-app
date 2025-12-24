package com.example.order.controller;

import com.example.order.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/orders/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final OrderRepository orderRepository;
    private final com.example.order.repository.RestaurantSettingsRepository settingsRepository;

    public AnalyticsController(OrderRepository orderRepository,
            com.example.order.repository.RestaurantSettingsRepository settingsRepository) {
        this.orderRepository = orderRepository;
        this.settingsRepository = settingsRepository;
    }

    @GetMapping("/summary")
    public SummaryResponse getSummary() {
        // 1. Get Target Timezone
        String timeZoneId = settingsRepository.findBySettingKey("TIMEZONE")
                .map(s -> s.getSettingValue())
                .orElse("UTC"); // Default to UTC if missing

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZoneId);
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }

        // 2. Calculate Start of Day in Target Zone
        LocalDateTime nowInTargetZone = LocalDateTime.now(zoneId);
        LocalDateTime startOfDayInTargetZone = nowInTargetZone.with(LocalTime.MIN);

        // 3. Convert back to UTC for DB Query
        // We need to convert startOfDayInTargetZone (e.g. 00:00 Taipei) -> UTC Instanct
        // -> UTC LocalDateTime
        LocalDateTime startOfDayUTC = startOfDayInTargetZone.atZone(zoneId)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();

        System.out.println("DEBUG ANALYTICS: Restaurant Timezone = " + timeZoneId);
        System.out.println("DEBUG ANALYTICS: StartOfDay (Local) = " + startOfDayInTargetZone);
        System.out.println("DEBUG ANALYTICS: StartOfDay (UTC for DB) = " + startOfDayUTC);

        BigDecimal todayRevenue = orderRepository.sumTotalPriceAfter(startOfDayUTC);
        Long todayOrders = orderRepository.countOrdersAfter(startOfDayUTC);
        Long activeOrders = orderRepository.countOrdersByStatus(Arrays.asList("PENDING", "PREPARING", "PAID"));

        return new SummaryResponse(
                todayRevenue != null ? todayRevenue : BigDecimal.ZERO,
                todayOrders != null ? todayOrders : 0L,
                activeOrders != null ? activeOrders : 0L);
    }

    @GetMapping("/trends")
    public List<TrendResponse> getTrends() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        List<Object[]> rawData = orderRepository.getDailyRevenueTrends(sevenDaysAgo);

        return rawData.stream()
                .map(row -> new TrendResponse(
                        (String) row[0], // Date String
                        (BigDecimal) row[1], // Total Price
                        (Long) row[2] // Count
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/top-items")
    public List<TopItemResponse> getTopItems() {
        List<Object[]> rawData = orderRepository.getTopSellingItems(PageRequest.of(0, 5));

        return rawData.stream()
                .map(row -> new TopItemResponse(
                        (String) row[0],
                        (Long) row[1]))
                .collect(Collectors.toList());
    }

    // DTOs
    public static class SummaryResponse {
        public BigDecimal todayRevenue;
        public Long todayOrders;
        public Long activeOrders;

        public SummaryResponse(BigDecimal todayRevenue, Long todayOrders, Long activeOrders) {
            this.todayRevenue = todayRevenue;
            this.todayOrders = todayOrders;
            this.activeOrders = activeOrders;
        }
    }

    public static class TrendResponse {
        public String date;
        public BigDecimal revenue;
        public Long count;

        public TrendResponse(String date, BigDecimal revenue, Long count) {
            this.date = date;
            this.revenue = revenue;
            this.count = count;
        }
    }

    public static class TopItemResponse {
        public String name;
        public Long quantity;

        public TopItemResponse(String name, Long quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }

    @GetMapping("/distribution")
    public List<DistributionResponse> getDistribution() {
        // 1. Get Target Timezone (Same logic as summary)
        String timeZoneId = settingsRepository.findBySettingKey("TIMEZONE")
                .map(s -> s.getSettingValue())
                .orElse("UTC");

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZoneId);
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }

        // Start of Day in Local -> UTC
        LocalDateTime startOfDayUTC = LocalDateTime.now(zoneId).with(LocalTime.MIN)
                .atZone(zoneId)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();

        List<Object[]> rawData = orderRepository.getOrderDistribution(startOfDayUTC);

        return rawData.stream()
                .map(row -> new DistributionResponse(
                        row[0] != null ? row[0].toString() : "UNKNOWN",
                        (Long) row[1],
                        (BigDecimal) row[2]))
                .collect(Collectors.toList());
    }

    public static class DistributionResponse {
        public String orderType;
        public Long count;
        public BigDecimal revenue;

        public DistributionResponse(String orderType, Long count, BigDecimal revenue) {
            this.orderType = orderType;
            this.count = count;
            this.revenue = revenue;
        }
    }
}
