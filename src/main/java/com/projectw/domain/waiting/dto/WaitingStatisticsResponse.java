package com.projectw.domain.waiting.dto;

import com.projectw.domain.waiting.entity.DailyWaitingStatistics;
import com.projectw.domain.waiting.entity.HourlyWaitingStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public sealed interface WaitingStatisticsResponse permits
        WaitingStatisticsResponse.Monthly,
        WaitingStatisticsResponse.Daily,
        WaitingStatisticsResponse.DailyPage,
        WaitingStatisticsResponse.Hourly {

    record Monthly(
            long totalWaitingCount,
            long completedCount,
            long canceledCount,
            double completedAverageWaitingTime,
            double canceledAverageWaitingTime,
            double cancellationRate,
            Map<LocalDate, Daily> dailyStatistics
    )  implements WaitingStatisticsResponse{
    }

    record Daily(
         long totalWaitingCount,
         long completedCount,
         long canceledCount,
         double completedAverageWaitingTime,
         double canceledAverageWaitingTime,
         double cancellationRate
    )  implements WaitingStatisticsResponse{

        public Daily(DailyWaitingStatistics s) {
            this(s.getTotalWaitingCount(), s.getCompletedCount(), s.getCanceledCount(), s.getCompletedAverageWaitingTime(), s.getCanceledAverageWaitingTime(), s.getCancellationRate());
        }

        public static Daily emptyData() {
            return new Daily(0, 0, 0, 0, 0, 0);
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


    record Hourly(
            int hour,
            long totalWaitingCount,
            long completedCount,
            long canceledCount,
            long maxWaitingTime,
            long minWaitingTime,
            double completedAverageWaitingTime,
            double canceledAverageWaitingTime
    )  implements WaitingStatisticsResponse {
        public Hourly(HourlyWaitingStatistics h) {
            this(h.getHour(), h.getTotalWaitingCount(), h.getCompletedCount(), h.getCanceledCount(), h.getMaxWaitingTime(), h.getMinWaitingTime(), h.getCompletedAverageWaitingTime(), h.getCanceledAverageWaitingTime());
        }

        public static Hourly emptyData(int hour) {
            return new Hourly(hour, 0, 0, 0, 0, 0, 0, 0);
        }
    }
}
