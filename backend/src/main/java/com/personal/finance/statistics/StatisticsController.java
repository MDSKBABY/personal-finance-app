package com.personal.finance.statistics;

import java.util.List;

import com.personal.finance.common.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/summary")
    public ApiResponse<SummaryResponse> getSummary(@ModelAttribute DateRangeQuery query) {
        return ApiResponse.success(statisticsService.getSummary(query));
    }

    @GetMapping("/monthly")
    public ApiResponse<List<MonthlyStatisticsResponse>> getMonthly(@ModelAttribute DateRangeQuery query) {
        return ApiResponse.success(statisticsService.getMonthly(query));
    }

    @GetMapping("/category")
    public ApiResponse<List<CategoryStatisticsResponse>> getCategory(@ModelAttribute DateRangeQuery query) {
        return ApiResponse.success(statisticsService.getCategory(query));
    }

    @GetMapping("/daily")
    public ApiResponse<List<DailyStatisticsResponse>> getDaily(@ModelAttribute DateRangeQuery query) {
        return ApiResponse.success(statisticsService.getDaily(query));
    }

    @GetMapping("/salary")
    public ApiResponse<List<SalaryStatisticsResponse>> getSalary(@ModelAttribute DateRangeQuery query) {
        return ApiResponse.success(statisticsService.getSalary(query));
    }
}
