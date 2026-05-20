export type TransactionType = 'income' | 'expense'

export interface TransactionRecord {
  id: number
  type: TransactionType
  category: string
  amount: number
  paymentMethod: string | null
  transactionDate: string
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface TransactionForm {
  type: TransactionType
  category: string
  amount: string
  paymentMethod: string
  transactionDate: string
  note: string
}

export interface TransactionQuery {
  type?: TransactionType | ''
  category?: string
  paymentMethod?: string
  keyword?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface SummaryStatistics {
  income: number
  expense: number
  balance: number
  recentTransactions: TransactionRecord[]
}

export interface MonthlyStatistics {
  month: string
  income: number
  expense: number
  balance: number
}

export interface CategoryStatistics {
  category: string
  amount: number
}

export interface DailyStatistics {
  date: string
  income: number
  expense: number
  balance: number
}

export interface SalaryStatistics {
  month: string
  amount: number
}
