<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'

import { fetchTransactions } from '../api/transactions'
import {
  fetchCategoryStatistics,
  fetchDailyStatistics,
  fetchSalaryStatistics,
  fetchSummary,
} from '../api/statistics'
import type {
  CategoryStatistics,
  DailyStatistics,
  SalaryStatistics,
  SummaryStatistics,
  TransactionQuery,
  TransactionRecord,
} from '../types/transaction'

const summary = ref<SummaryStatistics | null>(null)
const daily = ref<DailyStatistics[]>([])
const categories = ref<CategoryStatistics[]>([])
const salary = ref<SalaryStatistics[]>([])
const recentRecords = ref<TransactionRecord[]>([])
const recentTotal = ref(0)
const loading = ref(false)
const recentLoading = ref(false)
const errorMessage = ref('')
const recentErrorMessage = ref('')

const trendChartRef = ref<HTMLDivElement | null>(null)
const categoryChartRef = ref<HTMLDivElement | null>(null)

const incomeCategories = ['工资', '奖金', '副业收入', '转账收入', '其它收入']
const expenseCategories = ['餐饮', '交通', '房租', '游戏', '购物', '娱乐', '医疗', '数码', '其它']
const paymentMethods = ['微信', '支付宝', '银行卡', '现金', '信用卡', '其它']

const recentFilters = reactive<TransactionQuery & { datePreset: string }>({
  type: '',
  category: '',
  paymentMethod: '',
  keyword: '',
  startDate: '',
  endDate: '',
  page: 1,
  size: 8,
  datePreset: 'recent',
})

const cards = computed(() => [
  { label: '本月收入', value: summary.value?.income ?? 0, type: 'income' },
  { label: '本月支出', value: summary.value?.expense ?? 0, type: 'expense' },
  { label: '本月结余', value: summary.value?.balance ?? 0, type: 'balance' },
])

const hasDailyTrendData = computed(() =>
  daily.value.some((item) => item.income > 0 || item.expense > 0),
)

onMounted(async () => {
  await loadDashboard()
  await loadRecentRecords()
})

async function refreshDashboard() {
  await Promise.all([loadDashboard(), loadRecentRecords()])
}

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [summaryData, dailyData, categoryData, salaryData] = await Promise.all([
      fetchSummary(),
      fetchDailyStatistics(),
      fetchCategoryStatistics(),
      fetchSalaryStatistics(),
    ])
    summary.value = summaryData
    daily.value = fillCurrentMonthDailyData(dailyData)
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

async function loadRecentRecords(preserveScroll = false) {
  const scrollTop = window.scrollY
  recentLoading.value = true
  recentErrorMessage.value = ''

  try {
    applyDatePreset()
    const query: TransactionQuery = {
      type: recentFilters.type,
      category: recentFilters.category,
      paymentMethod: recentFilters.paymentMethod,
      keyword: recentFilters.keyword,
      startDate: recentFilters.startDate,
      endDate: recentFilters.endDate,
      page: 1,
      size: 8,
    }
    const result = await fetchTransactions(query)
    recentRecords.value = result.records
    recentTotal.value = result.total
  } catch (error) {
    recentErrorMessage.value = error instanceof Error ? error.message : '查询流水失败'
  } finally {
    recentLoading.value = false
    if (preserveScroll) {
      await nextTick()
      window.requestAnimationFrame(() => {
        window.scrollTo({ top: scrollTop, left: 0, behavior: 'auto' })
      })
    }
  }
}

async function searchRecentRecords() {
  await loadRecentRecords(true)
}

function resetRecentFilters() {
  recentFilters.type = ''
  recentFilters.category = ''
  recentFilters.paymentMethod = ''
  recentFilters.keyword = ''
  recentFilters.startDate = ''
  recentFilters.endDate = ''
  recentFilters.datePreset = 'recent'
  loadRecentRecords(true)
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
    tooltip: { trigger: 'axis', textStyle: { fontSize: 13 } },
    legend: {
      itemWidth: 26,
      itemHeight: 14,
      textStyle: { color: '#4b5563', fontSize: 13 },
    },
    grid: { left: 54, right: 34, top: 58, bottom: 58 },
    xAxis: {
      type: 'category',
      data: daily.value.map((item) => formatDayLabel(item.date)),
      axisLabel: {
        color: '#6b7280',
        fontSize: 13,
        interval: 0,
        rotate: daily.value.length > 18 ? 45 : 0,
      },
      axisLine: { lineStyle: { color: '#e5e7eb' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#6b7280', fontSize: 13 },
      splitLine: { lineStyle: { color: '#eef2f7' } },
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        barMaxWidth: 24,
        barGap: '12%',
        data: daily.value.map((item) => item.income),
        color: '#16a34a',
      },
      {
        name: '支出',
        type: 'bar',
        barMaxWidth: 24,
        data: daily.value.map((item) => item.expense),
        color: '#ea580c',
      },
      {
        name: '结余',
        type: 'line',
        smooth: true,
        lineStyle: { width: 3 },
        symbolSize: 10,
        data: daily.value.map((item) => item.balance),
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

function applyDatePreset() {
  if (recentFilters.datePreset === 'custom') {
    return
  }
  if (recentFilters.datePreset === 'recent') {
    recentFilters.startDate = ''
    recentFilters.endDate = ''
    return
  }

  const today = new Date()
  if (recentFilters.datePreset === 'today') {
    const date = formatDate(today)
    recentFilters.startDate = date
    recentFilters.endDate = date
    return
  }
  if (recentFilters.datePreset === 'last3') {
    const start = new Date(today)
    start.setDate(today.getDate() - 2)
    recentFilters.startDate = formatDate(start)
    recentFilters.endDate = formatDate(today)
    return
  }
  if (recentFilters.datePreset === 'month') {
    recentFilters.startDate = formatDate(new Date(today.getFullYear(), today.getMonth(), 1))
    recentFilters.endDate = formatDate(today)
  }
}

function fillCurrentMonthDailyData(records: DailyStatistics[]) {
  const today = new Date()
  const year = today.getFullYear()
  const month = today.getMonth()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const recordsByDate = new Map(records.map((item) => [item.date, item]))

  return Array.from({ length: daysInMonth }, (_, index) => {
    const date = formatDate(new Date(year, month, index + 1))
    return recordsByDate.get(date) ?? { date, income: 0, expense: 0, balance: 0 }
  })
}

function formatDate(date: Date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

function formatDayLabel(date: string) {
  return date.slice(5)
}

function formatMoney(value: number) {
  return `¥${Number(value).toFixed(2)}`
}

function formatRecord(record: TransactionRecord) {
  const prefix = record.type === 'income' ? '+' : '-'
  return `${prefix}${formatMoney(record.amount)}`
}

function displayRecordType(record: TransactionRecord) {
  return record.type === 'income' ? '收入' : '支出'
}
</script>

<template>
  <section class="dashboard-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Dashboard</p>
        <h1>首页总览</h1>
      </div>
      <button class="ghost-button" type="button" @click="refreshDashboard">刷新</button>
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
            <h2>每日收支趋势</h2>
          </div>
        </div>
        <div v-if="!hasDailyTrendData && !loading" class="chart-empty trend-empty">
          <div class="trend-preview" aria-hidden="true">
            <span />
            <span />
            <span />
            <span />
            <span />
          </div>
          <div>
            <strong>暂无趋势数据</strong>
            <small>新增流水后展示本月每天的收入与支出变化</small>
          </div>
        </div>
        <div v-show="hasDailyTrendData" ref="trendChartRef" class="chart-box" />
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
            <h2>流水查询</h2>
          </div>
          <span class="muted">共 {{ recentTotal }} 条</span>
        </div>

        <div class="recent-filter-grid">
          <select v-model="recentFilters.datePreset" @change="applyDatePreset">
            <option value="recent">最近记录</option>
            <option value="today">今天</option>
            <option value="last3">近 3 天</option>
            <option value="month">本月</option>
            <option value="custom">自定义日期</option>
          </select>
          <select v-model="recentFilters.type">
            <option value="">全部类型</option>
            <option value="income">收入</option>
            <option value="expense">支出</option>
          </select>
          <select v-model="recentFilters.category">
            <option value="">全部分类</option>
            <optgroup label="收入">
              <option v-for="category in incomeCategories" :key="category" :value="category">
                {{ category }}
              </option>
            </optgroup>
            <optgroup label="支出">
              <option v-for="category in expenseCategories" :key="category" :value="category">
                {{ category }}
              </option>
            </optgroup>
          </select>
          <select v-model="recentFilters.paymentMethod">
            <option value="">全部支付方式</option>
            <option v-for="method in paymentMethods" :key="method" :value="method">
              {{ method }}
            </option>
          </select>
          <input
            v-model="recentFilters.keyword"
            class="recent-keyword"
            placeholder="搜索备注，例如：转账、吃饭"
          />
          <input
            v-model="recentFilters.startDate"
            :disabled="recentFilters.datePreset !== 'custom'"
            type="date"
          />
          <input
            v-model="recentFilters.endDate"
            :disabled="recentFilters.datePreset !== 'custom'"
            type="date"
          />
          <button class="primary-button" type="button" :disabled="recentLoading" @click="searchRecentRecords">
            {{ recentLoading ? '查询中...' : '搜索' }}
          </button>
          <button class="ghost-button" type="button" @click="resetRecentFilters">重置</button>
        </div>

        <p v-if="recentErrorMessage" class="error">{{ recentErrorMessage }}</p>

        <div class="recent-result-area">
          <div v-if="recentLoading" class="empty-state compact">
            <strong>查询中...</strong>
          </div>
          <div v-else-if="recentRecords.length === 0" class="empty-state compact">
            <strong>暂无流水</strong>
            <small>换个筛选条件试试，或去流水页新增记录。</small>
          </div>
          <div v-else class="recent-list recent-query-list">
            <div v-for="record in recentRecords" :key="record.id" class="recent-item">
              <div>
                <strong>{{ record.category }}</strong>
                <span>
                  {{ record.transactionDate }} · {{ displayRecordType(record) }} ·
                  {{ record.paymentMethod || '无支付方式' }} · {{ record.note || '无备注' }}
                </span>
              </div>
              <b :class="record.type === 'income' ? 'income-text' : 'expense-text'">
                {{ formatRecord(record) }}
              </b>
            </div>
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
