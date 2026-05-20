import { buildQuery, request } from './http'
import type {
  CategoryStatistics,
  DailyStatistics,
  MonthlyStatistics,
  SalaryStatistics,
  SummaryStatistics,
} from '../types/transaction'

interface DateRange {
  startDate?: string
  endDate?: string
}

export function fetchSummary(query: DateRange = {}) {
  return request<SummaryStatistics>(`/api/statistics/summary${buildQuery(query)}`)
}

export function fetchMonthlyStatistics(query: DateRange = {}) {
  return request<MonthlyStatistics[]>(`/api/statistics/monthly${buildQuery(query)}`)
}

export function fetchCategoryStatistics(query: DateRange = {}) {
  return request<CategoryStatistics[]>(`/api/statistics/category${buildQuery(query)}`)
}

export function fetchDailyStatistics(query: DateRange = {}) {
  return request<DailyStatistics[]>(`/api/statistics/daily${buildQuery(query)}`)
}

export function fetchSalaryStatistics(query: DateRange = {}) {
  return request<SalaryStatistics[]>(`/api/statistics/salary${buildQuery(query)}`)
}
