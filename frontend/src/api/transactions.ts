import { buildQuery, request } from './http'
import type {
  PageResult,
  TransactionForm,
  TransactionQuery,
  TransactionRecord,
} from '../types/transaction'

function toPayload(form: TransactionForm) {
  return {
    type: form.type,
    category: form.category,
    amount: Number(form.amount),
    paymentMethod: form.paymentMethod || null,
    transactionDate: form.transactionDate,
    note: form.note || null,
  }
}

export function fetchTransactions(query: TransactionQuery) {
  return request<PageResult<TransactionRecord>>(`/api/transactions${buildQuery(query)}`)
}

export function createTransaction(form: TransactionForm) {
  return request<TransactionRecord>('/api/transactions', {
    method: 'POST',
    body: JSON.stringify(toPayload(form)),
  })
}

export function updateTransaction(id: number, form: TransactionForm) {
  return request<TransactionRecord>(`/api/transactions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(toPayload(form)),
  })
}

export function deleteTransaction(id: number) {
  return request<void>(`/api/transactions/${id}`, {
    method: 'DELETE',
  })
}
