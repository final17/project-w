package com.projectw.domain.waiting.dto;

import com.projectw.domain.waiting.entity.DailyWaitingStatistics;

import java.util.List;

public sealed interface WaitingStatisticsResponse permits
        WaitingStatisticsResponse.Daily,
        WaitingStatisticsResponse.DailyPage,
        WaitingStatisticsResponse.HourlyPage,
        WaitingStatisticsResponse.Hourly {

    record Daily(
         long totalWaitingCount,
         long completedCount,
         long canceledCount,
         double averageWaitingTime,
         double cancellationRate
    )  implements WaitingStatisticsResponse{

        public Daily(DailyWaitingStatistics s) {
            this(s.getTotalWaitingCount(), s.getCompletedCount(), s.getCanceledCount(), s.getAverageWaitingTime(), s.getCancellationRate());
        }
    }

    record DailyPage(
            int currentPage,
            int pageSize,
            long currentElement,
            long totalElement,
            int totalPage,
            boolean hasNextPage,
            List<Daily> contents
    )  implements WaitingStatisticsResponse { }

    record  HourlyPage(
            int currentPage,
            int pageSize,
            long currentElement,
            long totalElement,
            int totalPage,
            boolean hasNextPage,
            List<Hourly> contents
    ) implements WaitingStatisticsResponse { }

    record Hourly(
            int hour,
            long totalWaitingCount,
            long completedCount,
            long canceledCount,
            long maxWaitingTime,
            long minWaitingTime,
            double averageWaitingTime
    )  implements WaitingStatisticsResponse {

    }
}
