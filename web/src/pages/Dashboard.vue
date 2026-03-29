<template>
  <div class="page">
    <div v-if="!isManager" class="card">
      <div class="section-title">系统概览</div>
      <n-space vertical>
        <n-text>欢迎使用社区快递管理系统。</n-text>
        <n-text>当前账号为用户角色，请在左侧导航查看“快递管理 / 寄件管理”。</n-text>
      </n-space>
    </div>

    <div v-else class="dashboard">
      <div class="card">
        <div class="section-title">管理数据看板</div>
        <div v-if="summaryError" class="error-text">{{ summaryError }}</div>
        <div class="metric-grid">
          <div class="metric-card">
            <div class="metric-label">快递总量</div>
            <div class="metric-value">{{ summaryLoading ? '-' : formatNumber(summary.totalExpress) }}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">待取件</div>
            <div class="metric-value">{{ summaryLoading ? '-' : formatNumber(summary.pendingPickup) }}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">滞留(48h+)</div>
            <div class="metric-value">{{ summaryLoading ? '-' : formatNumber(summary.overdue48h) }}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">今日入库</div>
            <div class="metric-value">{{ summaryLoading ? '-' : formatNumber(summary.todayCheckin) }}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">今日出库</div>
            <div class="metric-value">{{ summaryLoading ? '-' : formatNumber(summary.todayCheckout) }}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">待处理寄件</div>
            <div class="metric-value">{{ summaryLoading ? '-' : formatNumber(summary.pendingSendOrders) }}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="title-row">
          <div class="section-title" style="margin-bottom: 0">近7天入库/出库趋势</div>
          <n-button size="small" @click="fetchTrend">刷新趋势</n-button>
        </div>
        <div v-if="trendError" class="error-text">{{ trendError }}</div>
        <div v-if="trendLoading" class="placeholder">趋势数据加载中...</div>
        <div v-else-if="!trendData.length" class="placeholder">暂无趋势数据</div>
        <div v-else ref="trendChartRef" class="trend-chart"></div>
      </div>

      <div class="rank-grid">
        <div class="card">
          <div class="section-title">高负载货架 Top 5</div>
          <div v-if="ranksError" class="error-text">{{ ranksError }}</div>
          <n-data-table
            :columns="shelfColumns"
            :data="topLoadShelves"
            :loading="ranksLoading"
            :bordered="false"
            :pagination="false"
          />
        </div>

        <div class="card">
          <div class="section-title">滞留最久快递 Top 10</div>
          <div v-if="ranksError" class="error-text">{{ ranksError }}</div>
          <n-data-table
            :columns="overdueColumns"
            :data="topOverdueExpresses"
            :loading="ranksLoading"
            :bordered="false"
            :pagination="false"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart } from 'echarts/charts';
import {
  GridComponent,
  LegendComponent,
  TooltipComponent
} from 'echarts/components';
import { init } from 'echarts/core';
import { api } from '../api';
import { useAuthStore } from '../stores/auth';

use([CanvasRenderer, LineChart, TooltipComponent, LegendComponent, GridComponent]);

const auth = useAuthStore();
const isManager = computed(() => ['STAFF', 'ADMIN'].includes(auth.role));

const summaryLoading = ref(false);
const trendLoading = ref(false);
const ranksLoading = ref(false);
const summaryError = ref('');
const trendError = ref('');
const ranksError = ref('');

const summary = ref({
  totalExpress: 0,
  pendingPickup: 0,
  overdue48h: 0,
  todayCheckin: 0,
  todayCheckout: 0,
  pendingSendOrders: 0
});

const trendData = ref([]);
const topLoadShelves = ref([]);
const topOverdueExpresses = ref([]);

const trendChartRef = ref(null);
let trendChart = null;
const initialized = ref(false);

const shelfColumns = [
  {
    title: '货架',
    key: 'shelf',
    render: (row) => `${row.shelfCode ?? '-'}-${row.shelfLayer ?? '-'}`
  },
  {
    title: '当前负载',
    key: 'usage',
    render: (row) => `${row.currentUsage ?? 0}/${row.totalCapacity ?? 0}`
  },
  {
    title: '负载率',
    key: 'loadRate',
    render: (row) => formatPercent(row.loadRate)
  }
];

const overdueColumns = [
  { title: '单号', key: 'trackingNumber' },
  { title: '收件人', key: 'receiverName' },
  { title: '手机号', key: 'receiverPhone' },
  {
    title: '货架',
    key: 'shelf',
    render: (row) => `${row.shelfCode ?? '-'}-${row.shelfLayer ?? '-'}`
  },
  {
    title: '入库时间',
    key: 'createTime',
    render: (row) => formatDateTime(row.createTime)
  },
  {
    title: '滞留天数',
    key: 'overdueDays',
    render: (row) => `${Number(row.overdueDays || 0)} 天`
  }
];

const formatDateTime = (value) => (value ? String(value).replace('T', ' ') : '-');
const formatNumber = (value) => Number(value || 0).toLocaleString('zh-CN');
const formatPercent = (rate) => `${Math.round(Number(rate || 0) * 100)}%`;

const fetchSummary = async () => {
  summaryLoading.value = true;
  summaryError.value = '';
  try {
    const res = await api.getDashboardSummary();
    summary.value = res.data || summary.value;
  } catch (err) {
    summaryError.value = '核心指标加载失败';
  } finally {
    summaryLoading.value = false;
  }
};

const renderTrendChart = async () => {
  await nextTick();
  if (!trendChartRef.value || !trendData.value.length) {
    if (trendChart) {
      trendChart.dispose();
      trendChart = null;
    }
    return;
  }
  if (!trendChart) {
    trendChart = init(trendChartRef.value);
  }
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库', '出库'] },
    grid: { left: 24, right: 24, top: 36, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.map((item) => item.date)
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '入库',
        type: 'line',
        smooth: true,
        data: trendData.value.map((item) => Number(item.checkinCount || 0)),
        lineStyle: { color: '#2563eb' },
        areaStyle: { color: 'rgba(37, 99, 235, 0.15)' }
      },
      {
        name: '出库',
        type: 'line',
        smooth: true,
        data: trendData.value.map((item) => Number(item.checkoutCount || 0)),
        lineStyle: { color: '#16a34a' },
        areaStyle: { color: 'rgba(22, 163, 74, 0.15)' }
      }
    ]
  });
  requestAnimationFrame(() => {
    if (trendChart) {
      trendChart.resize();
    }
  });
};

const fetchTrend = async () => {
  trendLoading.value = true;
  trendError.value = '';
  try {
    const res = await api.getDashboardTrend({ days: 7 });
    trendData.value = Array.isArray(res.data) ? res.data : [];
  } catch (err) {
    trendData.value = [];
    trendError.value = '趋势数据加载失败';
  } finally {
    trendLoading.value = false;
    if (!trendError.value && trendData.value.length) {
      await renderTrendChart();
    }
  }
};

const fetchRanks = async () => {
  ranksLoading.value = true;
  ranksError.value = '';
  try {
    const res = await api.getDashboardRanks();
    const payload = res.data || {};
    topLoadShelves.value = Array.isArray(payload.topLoadShelves) ? payload.topLoadShelves : [];
    topOverdueExpresses.value = Array.isArray(payload.topOverdueExpresses) ? payload.topOverdueExpresses : [];
  } catch (err) {
    topLoadShelves.value = [];
    topOverdueExpresses.value = [];
    ranksError.value = '榜单数据加载失败';
  } finally {
    ranksLoading.value = false;
  }
};

const onResize = () => {
  if (trendChart) {
    trendChart.resize();
  }
};

watch(
  isManager,
  async (val) => {
    if (!val || initialized.value) {
      return;
    }
    initialized.value = true;
    await Promise.allSettled([fetchSummary(), fetchTrend(), fetchRanks()]);
    window.addEventListener('resize', onResize);
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize);
  if (trendChart) {
    trendChart.dispose();
    trendChart = null;
  }
});
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(200px, 1fr));
  gap: 12px;
}

.metric-card {
  border-radius: 10px;
  padding: 14px 16px;
  background: linear-gradient(160deg, #f8fbff 0%, #eef4ff 100%);
  border: 1px solid #dbe7ff;
}

.metric-label {
  font-size: 13px;
  color: #5c6470;
}

.metric-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #1f2933;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.trend-chart {
  height: 360px;
}

.rank-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 16px;
}

.error-text {
  color: #dc2626;
  margin-bottom: 10px;
}

.placeholder {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

@media (max-width: 1200px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(200px, 1fr));
  }
}

@media (max-width: 900px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .rank-grid {
    grid-template-columns: 1fr;
  }

  .trend-chart {
    height: 300px;
  }
}
</style>
