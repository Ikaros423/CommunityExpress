package com.express.system.service;

import com.express.system.dto.DashboardRanksVO;
import com.express.system.dto.DashboardSummaryVO;
import com.express.system.dto.DashboardTrendPointVO;

import java.util.List;

public interface IDashboardService {

    DashboardSummaryVO getSummary();

    List<DashboardTrendPointVO> getTrend(int days);

    DashboardRanksVO getRanks();
}
