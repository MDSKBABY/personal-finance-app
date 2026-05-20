<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, ref } from 'vue'

import {
  fetchCategoryStatistics,
  fetchMonthlyStatistics,
  fetchSalaryStatistics,
  fetchSummary,
} from '../api/statistics'
import type {
  CategoryStatistics,
  MonthlyStatistics,
  SalaryStatistics,
  SummaryStatistics,
  TransactionRecord,
} from '../types/transaction'

const summary = ref<SummaryStatistics | null>(null)
const monthly = ref<MonthlyStatistics[]>([])
const categories = ref<CategoryStatistics[]>([])
const salary = ref<SalaryStatistics[]>([])
const loading = ref(false)
const errorMessage = ref('')

const trendChartRef = ref<HTMLDivElement | null>(null)
const categoryChartRef = ref<HTMLDivElement | null>(null)

const cards = computed(() => [
  { label: '本月收入', value: summary.value?.income ?? 0, type: 'income' },
  { label: '本月支出', value: summary.value?.expense ?? 0, type: 'expense' },
  { label: '本月结余', value: summary.value?.balance ?? 0, type: 'balance' },
])

onMounted(async () => {
  await loadDashboard()
})

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [summaryData, monthlyData, categoryData, salaryData] = await Promise.all([
      fetchSummary(),
      fetchMonthlyStatistics(),
      fetchCategoryStatistics(),
      fetchSalaryStatistics(),
    ])
    summary.value = summaryData
    monthly.value = monthlyData
    categories.value = categoryData
    salary.value = salaryData

    await nextTick()
    renderCharts()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载统计失败'
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  renderTrendChart()
  renderCategoryChart()
}

function renderTrendChart() {
  if (!trendChartRef.value) {
    return
  }

  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#4b5563' } },
    grid: { left: 46, right: 28, top: 50, bottom: 36 },
    xAxis: {
      type: 'category',
      data: monthly.value.map((item) => item.month),
      axisLabel: { color: '#6b7280' },
      axisLine: { lineStyle: { color: '#e5e7eb' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#6b7280' },
      splitLine: { lineStyle: { color: '#eef2f7' } },
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        barMaxWidth: 34,
        barGap: '18%',
        data: monthly.value.map((item) => item.income),
        color: '#16a34a',
      },
      {
        name: '支出',
        type: 'bar',
        barMaxWidth: 34,
        data: monthly.value.map((item) => item.expense),
        color: '#ea580c',
      },
      {
        name: '结余',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        data: monthly.value.map((item) => item.balance),
        color: '#2563eb',
      },
    ],
  })
}

function renderCategoryChart() {
  if (!categoryChartRef.value) {
    return
  }

  const chart = echarts.init(categoryChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        name: '支出分类',
        type: 'pie',
        radius: ['42%', '70%'],
        data: categories.value.map((item) => ({
          name: item.category,
          value: item.amount,
        })),
        label: { color: '#374151' },
      },
    ],
  })
}

function formatMoney(value: number) {
  return `¥${Number(value).toFixed(2)}`
}

function formatRecord(record: TransactionRecord) {
  const prefix = record.type === 'income' ? '+' : '-'
  return `${prefix}${formatMoney(record.amount)}`
}
</script>

<template>
  <section class="dashboard-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Dashboard</p>
        <h1>首页总览</h1>
      </div>
      <button class="ghost-button" type="button" @click="loadDashboard">刷新</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="metric-grid">
      <article v-for="card in cards" :key="card.label" class="metric-card">
        <span>{{ card.label }}</span>
        <strong :class="`${card.type}-text`">{{ formatMoney(card.value) }}</strong>
        <small>
          {{ card.type === 'income' ? 'Income' : card.type === 'expense' ? 'Expense' : 'Balance' }}
        </small>
      </article>
    </div>

    <div class="dashboard-grid">
      <section class="panel chart-panel">
        <div class="panel-title">
          <div>
            <p class="eyebrow">Trend</p>
            <h2>月度收支趋势</h2>
          </div>
        </div>
        <div v-if="monthly.length === 0 && !loading" class="chart-empty trend-empty">
          <div class="trend-preview" aria-hidden="true">
            <span />
            <span />
            <span />
            <span />
            <span />
          </div>
          <div>
            <strong>暂无趋势数据</strong>
            <small>新增流水后展示月度收入与支出变化</small>
          </div>
        </div>
        <div v-show="monthly.length > 0" ref="trendChartRef" class="chart-box" />
      </section>

      <section class="panel chart-panel">
        <div class="panel-title">
          <div>
            <p class="eyebrow">Category</p>
            <h2>支出分类占比</h2>
          </div>
        </div>
        <div v-if="categories.length === 0 && !loading" class="chart-empty category-empty">
          <div class="donut-preview" aria-hidden="true" />
          <div>
            <strong>暂无分类数据</strong>
            <small>记录支出后查看餐饮、交通等分类占比</small>
          </div>
        </div>
        <div v-show="categories.length > 0" ref="categoryChartRef" class="chart-box" />
      </section>

      <section class="panel">
        <div class="panel-title">
          <div>
            <p class="eyebrow">Recent</p>
            <h2>最近流水</h2>
          </div>
        </div>
        <div v-if="!summary?.recentTransactions.length" class="empty-state compact">
          <strong>暂无流水</strong>
          <small>去流水页新增第一笔记录。</small>
        </div>
        <div v-else class="recent-list">
          <div v-for="record in summary.recentTransactions" :key="record.id" class="recent-item">
            <div>
              <strong>{{ record.category }}</strong>
              <span>{{ record.transactionDate }} · {{ record.note || '无备注' }}</span>
            </div>
            <b :class="record.type === 'income' ? 'income-text' : 'expense-text'">
              {{ formatRecord(record) }}
            </b>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-title">
          <div>
            <p class="eyebrow">Salary</p>
            <h2>工资统计</h2>
          </div>
        </div>
        <div v-if="salary.length === 0" class="empty-state compact">
          <strong>暂无工资收入</strong>
          <small>新增“工资”收入后自动统计。</small>
        </div>
        <div v-else class="salary-list">
          <div v-for="item in salary" :key="item.month">
            <span>{{ item.month }}</span>
            <strong>{{ formatMoney(item.amount) }}</strong>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>
