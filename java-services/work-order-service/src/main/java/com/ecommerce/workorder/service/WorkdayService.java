package com.ecommerce.workorder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 工作日历服务 - 用于SLA计算的工作日处理
 */
@Slf4j
@Service
public class WorkdayService {

    // 默认工作日时间: 9:00 - 18:00
    private static final LocalTime DEFAULT_WORK_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_WORK_END_TIME = LocalTime.of(18, 0);

    // 默认工作日: 周一到周五
    private static final Set<DayOfWeek> DEFAULT_WORKDAYS = Set.of(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    );

    @Value("${workday.holidays:}")
    private List<String> holidayStrings;

    @Value("${workday.work-start-time:09:00}")
    private String workStartTimeStr;

    @Value("${workday.work-end-time:18:00}")
    private String workEndTimeStr;

    @Value("${workday.exclude-weekend:true}")
    private boolean excludeWeekend;

    // 缓存的节假日集合
    private Set<LocalDate> holidays;

    /**
     * 判断是否为工作日
     * @param date 日期
     * @return 是否为工作日
     */
    public boolean isWorkday(LocalDate date) {
        // 排除周末
        if (excludeWeekend && isWeekend(date)) {
            return false;
        }
        // 排除节假日
        if (isHoliday(date)) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为周末
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 判断是否为节假日
     */
    private boolean isHoliday(LocalDate date) {
        if (holidayStrings == null || holidayStrings.isEmpty()) {
            return false;
        }
        // 初始化节假日缓存
        if (holidays == null) {
            initHolidays();
        }
        return holidays.contains(date);
    }

    /**
     * 初始化节假日缓存
     */
    private synchronized void initHolidays() {
        if (holidays != null) {
            return;
        }
        holidays = new HashSet<>();
        if (holidayStrings != null) {
            for (String holidayStr : holidayStrings) {
                try {
                    LocalDate holiday = LocalDate.parse(holidayStr.trim());
                    holidays.add(holiday);
                } catch (Exception e) {
                    log.warn("解析节假日失败: {}", holidayStr);
                }
            }
        }
        log.info("初始化节假日缓存: {} 个", holidays.size());
    }

    /**
     * 计算工作日后的日期
     * @param start 起始日期
     * @param days 工作日数
     * @return 工作日后的日期
     */
    public LocalDate addWorkdays(LocalDate start, int days) {
        if (days <= 0) {
            return start;
        }
        
        LocalDate current = start;
        int added = 0;
        
        while (added < days) {
            current = current.plusDays(1);
            if (isWorkday(current)) {
                added++;
            }
        }
        
        return current;
    }

    /**
     * 计算工作日后的日期时间
     * @param start 起始日期时间
     * @param hours 工作小时数
     * @return 工作日后的日期时间
     */
    public LocalDateTime addWorkHours(LocalDateTime start, int hours) {
        if (hours <= 0) {
            return start;
        }

        LocalDateTime current = start;
        LocalTime workStart = parseTime(workStartTimeStr, DEFAULT_WORK_START_TIME);
        LocalTime workEnd = parseTime(workEndTimeStr, DEFAULT_WORK_END_TIME);
        int remainingHours = hours;

        while (remainingHours > 0) {
            LocalDate currentDate = current.toLocalDate();
            LocalTime currentTime = current.toLocalTime();

            // 如果当前时间在工作时间之外，调整到下一个工作日开始
            if (!isWorkday(currentDate)) {
                // 找到下一个工作日
                current = currentDate.plusDays(1).atTime(workStart);
                continue;
            }

            if (currentTime.isBefore(workStart)) {
                // 在工作日开始之前，调整到工作日开始
                current = currentDate.atTime(workStart);
                continue;
            }

            if (currentTime.compareTo(workEnd) >= 0) {
                // 在工作日结束之后，调整到下一个工作日
                current = currentDate.plusDays(1).atTime(workStart);
                continue;
            }

            // 计算今天剩余工作时间
            long hoursUntilEnd = java.time.Duration.between(currentTime, workEnd).toHours();
            
            if (remainingHours <= hoursUntilEnd) {
                // 在今天可以完成
                current = current.plusHours(remainingHours);
                remainingHours = 0;
            } else {
                // 今天无法完成，使用剩余时间并跳到下一天
                remainingHours -= hoursUntilEnd;
                current = currentDate.plusDays(1).atTime(workStart);
            }
        }

        return current;
    }

    /**
     * 计算工作小时数（从开始时间到结束时间的工作小时）
     * @param start 开始时间
     * @param end 结束时间
     * @return 工作小时数
     */
    public long calculateWorkdayHours(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.isEqual(start)) {
            return 0;
        }

        LocalTime workStart = parseTime(workStartTimeStr, DEFAULT_WORK_START_TIME);
        LocalTime workEnd = parseTime(workEndTimeStr, DEFAULT_WORK_END_TIME);
        
        long totalHours = 0;
        LocalDateTime current = start;

        while (current.isBefore(end)) {
            LocalDate currentDate = current.toLocalDate();
            LocalTime currentTime = current.toLocalTime();

            // 非工作日，跳过
            if (!isWorkday(currentDate)) {
                current = currentDate.plusDays(1).atTime(workStart);
                continue;
            }

            // 时间段开始于工作日之前，调整到工作日开始
            if (currentTime.isBefore(workStart)) {
                current = currentDate.atTime(workStart);
                currentTime = workStart;
            }

            // 时间段结束于工作日之后，调整到工作日结束
            LocalTime endTimeForDay = end.toLocalDate().equals(currentDate) 
                ? end.toLocalTime() 
                : workEnd;
            
            if (endTimeForDay.isAfter(workEnd)) {
                endTimeForDay = workEnd;
            }

            if (currentTime.compareTo(workEnd) >= 0) {
                // 当前时间在工作时间之后，跳到下一天
                current = currentDate.plusDays(1).atTime(workStart);
                continue;
            }

            if (endTimeForDay.isBefore(currentTime)) {
                // 结束时间在工作开始时间之前，跳到下一天
                current = currentDate.plusDays(1).atTime(workStart);
                continue;
            }

            // 累加工作小时
            long hours = java.time.Duration.between(currentTime, endTimeForDay).toHours();
            totalHours += hours;

            // 跳到下一天
            current = currentDate.plusDays(1).atTime(workStart);
        }

        return totalHours;
    }

    /**
     * 计算在SLA时间前还剩多少工作小时
     * @param start 开始时间
     * @param slaTime SLA截止时间
     * @return 剩余工作小时数（负数表示已超时）
     */
    public long getRemainingWorkHours(LocalDateTime start, LocalDateTime slaTime) {
        return -calculateWorkdayHours(slaTime, start);
    }

    /**
     * 获取配置的工作开始时间
     */
    public LocalTime getWorkStartTime() {
        return parseTime(workStartTimeStr, DEFAULT_WORK_START_TIME);
    }

    /**
     * 获取配置的工作结束时间
     */
    public LocalTime getWorkEndTime() {
        return parseTime(workEndTimeStr, DEFAULT_WORK_END_TIME);
    }

    /**
     * 解析时间字符串
     */
    private LocalTime parseTime(String timeStr, LocalTime defaultTime) {
        try {
            if (timeStr != null && timeStr.contains(":")) {
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0].trim());
                int minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                return LocalTime.of(hour, minute);
            }
        } catch (Exception e) {
            log.warn("解析时间失败: {}, 使用默认值", timeStr);
        }
        return defaultTime;
    }

    /**
     * 添加节假日（用于动态配置）
     * @param holiday 节假日日期
     */
    public void addHoliday(LocalDate holiday) {
        if (holidays == null) {
            initHolidays();
        }
        holidays.add(holiday);
    }

    /**
     * 移除节假日
     * @param holiday 节假日日期
     */
    public void removeHoliday(LocalDate holiday) {
        if (holidays != null) {
            holidays.remove(holiday);
        }
    }

    /**
     * 获取所有节假日
     */
    public List<LocalDate> getHolidays() {
        if (holidays == null) {
            initHolidays();
        }
        return new ArrayList<>(holidays);
    }
}