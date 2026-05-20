import { defineStore } from 'pinia'

import {
  createTransaction,
  deleteTransaction,
  fetchTransactions,
  updateTransaction,
} from '../api/transactions'
import type {
  PageResult,
  TransactionForm,
  TransactionQuery,
  TransactionRecord,
} from '../types/transaction'

interface TransactionState {
  records: TransactionRecord[]
  total: number
  page: number
  size: number
  loading: boolean
  errorMessage: string
}

export const useTransactionStore = defineStore('transaction', {
  state: (): TransactionState => ({
    records: [],
    total: 0,
    page: 1,
    size: 10,
    loading: false,
    errorMessage: '',
  }),
  actions: {
    async loadTransactions(query: TransactionQuery = {}) {
      this.loading = true
      this.errorMessage = ''

      try {
        const result: PageResult<TransactionRecord> = await fetchTransactions({
          page: this.page,
          size: this.size,
          ...query,
        })
        this.records = result.records
        this.total = result.total
        this.page = result.page
        this.size = result.size
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '加载流水失败'
      } finally {
        this.loading = false
      }
    },
    async saveTransaction(form: TransactionForm, id?: number) {
      if (id) {
        await updateTransaction(id, form)
        return
      }

      await createTransaction(form)
    },
    async removeTransaction(id: number) {
      await deleteTransaction(id)
    },
  },
})
