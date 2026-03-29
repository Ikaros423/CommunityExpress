package com.express.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.express.system.dto.DashboardRanksVO;
import com.express.system.dto.DashboardSummaryVO;
import com.express.system.dto.DashboardTrendPointVO;
import com.express.system.dto.OverdueExpressVO;
import com.express.system.dto.ShelfLoadVO;
import com.express.system.entity.ExpressInfo;
import com.express.system.entity.SendOrder;
import com.express.system.entity.ShelfInfo;
import com.express.system.mapper.ExpressInfoMapper;
import com.express.system.mapper.SendOrderMapper;
import com.express.system.mapper.ShelfInfoMapper;
import com.express.system.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements IDashboardService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Autowired
    private ExpressInfoMapper expressInfoMapper;

    @Autowired
    private ShelfInfoMapper shelfInfoMapper;

    @Autowired
    private SendOrderMapper sendOrderMapper;

    @Override
    public DashboardSummaryVO getSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1).minusNanos(1);
        LocalDateTime overdueThreshold = now.minusHours(48);

        DashboardSummaryVO summary = new DashboardSummaryVO();
        summary.setTotalExpress(countExpress(null, null, null));
        summary.setPendingPickup(countExpress((byte) 1, null, null));
        summary.setOverdue48h(countExpress((byte) 1, null, overdueThreshold));
        summary.setTodayCheckin(countExpress(null, startOfToday, endOfToday));
        summary.setTodayCheckout(countTodayCheckout(startOfToday, endOfToday));
        summary.setPendingSendOrders(countPendingSendOrders());
        return summary;
    }

    @Override
    public List<DashboardTrendPointVO> getTrend(int days) {
        int safeDays = days <= 0 ? 7 : Math.min(days, 30);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(safeDays - 1L);

        List<DashboardTrendPointVO> points = new ArrayList<>();
        for (int i = 0; i < safeDays; i++) {
            LocalDate date = startDate.plusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1).minusNanos(1);
            DashboardTrendPointVO point = new DashboardTrendPointVO();
            point.setDate(date.format(DAY_FORMATTER));
            point.setCheckinCount(countCheckinByDay(start, end));
            point.setCheckoutCount(countCheckoutByDay(start, end));
            points.add(point);
        }
        return points;
    }

    @Override
    public DashboardRanksVO getRanks() {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(48);

        List<ShelfInfo> shelfInfos = shelfInfoMapper.selectList(new LambdaQueryWrapper<ShelfInfo>()
                .eq(ShelfInfo::getIsDeleted, (byte) 0)
                .eq(ShelfInfo::getStatus, (byte) 1));
        List<ShelfLoadVO> topLoadShelves = shelfInfos.stream()
                .map(this::toShelfLoadVO)
                .sorted(Comparator
                        .comparing(DashboardServiceImpl::safeLoadRate, Comparator.reverseOrder())
                        .thenComparing(item -> item.getCurrentUsage() == null ? 0 : item.getCurrentUsage(), Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toList());

        List<ExpressInfo> overdueExpresses = expressInfoMapper.selectList(new LambdaQueryWrapper<ExpressInfo>()
                .eq(ExpressInfo::getIsDeleted, (byte) 0)
                .eq(ExpressInfo::getStatus, (byte) 1)
                .le(ExpressInfo::getCreateTime, overdueThreshold)
                .orderByAsc(ExpressInfo::getCreateTime, ExpressInfo::getId)
                .last("LIMIT 10"));
        List<OverdueExpressVO> topOverdueExpresses = overdueExpresses.stream()
                .map(this::toOverdueExpressVO)
                .collect(Collectors.toList());

        DashboardRanksVO ranks = new DashboardRanksVO();
        ranks.setTopLoadShelves(topLoadShelves);
        ranks.setTopOverdueExpresses(topOverdueExpresses);
        return ranks;
    }

    private long countExpress(Byte status, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<ExpressInfo> query = new LambdaQueryWrapper<ExpressInfo>()
                .eq(ExpressInfo::getIsDeleted, (byte) 0)
                .eq(status != null, ExpressInfo::getStatus, status)
                .ge(start != null, ExpressInfo::getCreateTime, start)
                .le(end != null, ExpressInfo::getCreateTime, end);
        return expressInfoMapper.selectCount(query);
    }

    private long countTodayCheckout(LocalDateTime startOfToday, LocalDateTime endOfToday) {
        return expressInfoMapper.selectCount(new LambdaQueryWrapper<ExpressInfo>()
                .eq(ExpressInfo::getIsDeleted, (byte) 0)
                .eq(ExpressInfo::getStatus, (byte) 2)
                .ge(ExpressInfo::getUpdateTime, startOfToday)
                .le(ExpressInfo::getUpdateTime, endOfToday));
    }

    private long countPendingSendOrders() {
        return sendOrderMapper.selectCount(new LambdaQueryWrapper<SendOrder>()
                .eq(SendOrder::getIsDeleted, (byte) 0)
                .eq(SendOrder::getStatus, (byte) 0));
    }

    private long countCheckinByDay(LocalDateTime start, LocalDateTime end) {
        return expressInfoMapper.selectCount(new LambdaQueryWrapper<ExpressInfo>()
                .eq(ExpressInfo::getIsDeleted, (byte) 0)
                .ge(ExpressInfo::getCreateTime, start)
                .le(ExpressInfo::getCreateTime, end));
    }

    private long countCheckoutByDay(LocalDateTime start, LocalDateTime end) {
        return expressInfoMapper.selectCount(new LambdaQueryWrapper<ExpressInfo>()
                .eq(ExpressInfo::getIsDeleted, (byte) 0)
                .eq(ExpressInfo::getStatus, (byte) 2)
                .ge(ExpressInfo::getUpdateTime, start)
                .le(ExpressInfo::getUpdateTime, end));
    }

    private ShelfLoadVO toShelfLoadVO(ShelfInfo shelf) {
        int currentUsage = shelf.getCurrentUsage() == null ? 0 : shelf.getCurrentUsage();
        int totalCapacity = shelf.getTotalCapacity() == null ? 0 : shelf.getTotalCapacity();
        BigDecimal loadRate;
        if (totalCapacity <= 0) {
            loadRate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            loadRate = BigDecimal.valueOf(currentUsage)
                    .divide(BigDecimal.valueOf(totalCapacity), 2, RoundingMode.HALF_UP);
        }

        ShelfLoadVO vo = new ShelfLoadVO();
        vo.setId(shelf.getId());
        vo.setShelfCode(shelf.getShelfCode());
        vo.setShelfLayer(shelf.getShelfLayer());
        vo.setShelfName(shelf.getShelfName());
        vo.setShelfType(shelf.getShelfType());
        vo.setStatus(shelf.getStatus());
        vo.setCurrentUsage(currentUsage);
        vo.setTotalCapacity(totalCapacity);
        vo.setLoadRate(loadRate);
        return vo;
    }

    private OverdueExpressVO toOverdueExpressVO(ExpressInfo express) {
        OverdueExpressVO vo = new OverdueExpressVO();
        vo.setId(express.getId());
        vo.setTrackingNumber(express.getTrackingNumber());
        vo.setReceiverName(express.getReceiverName());
        vo.setReceiverPhone(express.getReceiverPhone());
        vo.setShelfCode(express.getShelfCode());
        vo.setShelfLayer(express.getShelfLayer());
        vo.setCreateTime(express.getCreateTime());
        if (express.getCreateTime() == null) {
            vo.setOverdueDays(0L);
        } else {
            long days = ChronoUnit.DAYS.between(express.getCreateTime(), LocalDateTime.now());
            vo.setOverdueDays(Math.max(days, 2L));
        }
        return vo;
    }

    private static BigDecimal safeLoadRate(ShelfLoadVO vo) {
        if (vo.getLoadRate() == null) {
            return BigDecimal.ZERO;
        }
        return vo.getLoadRate();
    }
}
