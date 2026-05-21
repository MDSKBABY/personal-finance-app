<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'

import TransactionForm from '../components/TransactionForm.vue'
import { useTransactionStore } from '../stores/transactionStore'
import type {
  TransactionForm as TransactionFormData,
  TransactionQuery,
  TransactionRecord,
} from '../types/transaction'

const store = useTransactionStore()
const { records, total, page, size, loading, errorMessage } = storeToRefs(store)

const editingRecord = ref<TransactionRecord | null>(null)
const saving = ref(false)
const notice = ref('')
const formResetKey = ref(0)

const filters = reactive<TransactionQuery>({
  type: '',
  category: '',
  paymentMethod: '',
  keyword: '',
  startDate: '',
  endDate: '',
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

const pageNumbers = computed(() => {
  const current = page.value
  const pages = totalPages.value

  if (pages <= 7) {
    return Array.from({ length: pages }, (_, index) => index + 1)
  }

  const result: Array<number | string> = [1]
  const start = Math.max(2, current - 1)
  const end = Math.min(pages - 1, current + 1)

  if (start > 2) {
    result.push('...')
  }

  for (let pageNumber = start; pageNumber <= end; pageNumber += 1) {
    result.push(pageNumber)
  }

  if (end < pages - 1) {
    result.push('...')
  }

  result.push(pages)
  return result
})

onMounted(() => {
  loadRecords()
})

async function loadRecords(targetPage = 1) {
  store.page = targetPage
  await store.loadTransactions(filters)
}

async function saveRecord(form: TransactionFormData, id?: number) {
  saving.value = true
  notice.value = ''

  try {
    await store.saveTransaction(form, id)
    editingRecord.value = null
    notice.value = id ? '流水已更新' : '流水已新增'
    if (!id) {
      formResetKey.value += 1
    }
    await loadRecords(page.value)
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '保存失败'
  } finally {
    saving.value = false
  }
}

async function removeRecord(record: TransactionRecord) {
  const confirmed = window.confirm(`确认删除 ${record.category} ${record.amount} 元这笔流水吗？`)
  if (!confirmed) {
    return
  }

  await store.removeTransaction(record.id)
  notice.value = '流水已删除'
  await loadRecords(page.value)
}

function resetFilters() {
  filters.type = ''
  filters.category = ''
  filters.paymentMethod = ''
  filters.keyword = ''
  filters.startDate = ''
  filters.endDate = ''
  loadRecords(1)
}

function formatAmount(record: TransactionRecord) {
  const prefix = record.type === 'income' ? '+' : '-'
  return `${prefix}${Number(record.amount).toFixed(2)}`
}
</script>

<template>
  <section class="page-grid">
    <div class="panel">
      <div class="panel-title">
        <div>
          <p class="eyebrow">Records</p>
          <h1>流水列表</h1>
        </div>
        <span class="muted">共 {{ total }} 条</span>
      </div>

      <form class="filter-grid" @submit.prevent="loadRecords(1)">
        <select v-model="filters.type">
          <option value="">全部类型</option>
          <option value="income">收入</option>
          <option value="expense">支出</option>
        </select>
        <input v-model="filters.category" placeholder="分类" />
        <input v-model="filters.paymentMethod" placeholder="支付方式" />
        <input v-model="filters.keyword" placeholder="搜索备注" />
        <input v-model="filters.startDate" type="date" />
        <input v-model="filters.endDate" type="date" />
        <button class="primary-button" type="submit">查询</button>
        <button class="ghost-button" type="button" @click="resetFilters">重置</button>
      </form>

      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>类型</th>
              <th>分类</th>
              <th>金额</th>
              <th>支付方式</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7">加载中...</td>
            </tr>
            <tr v-else-if="records.length === 0">
              <td colspan="7">暂无流水</td>
            </tr>
            <tr v-for="record in records" v-else :key="record.id">
              <td>{{ record.transactionDate }}</td>
              <td>{{ record.type === 'income' ? '收入' : '支出' }}</td>
              <td>{{ record.category }}</td>
              <td :class="record.type === 'income' ? 'income-text' : 'expense-text'">
                {{ formatAmount(record) }}
              </td>
              <td>{{ record.paymentMethod || '-' }}</td>
              <td>{{ record.note || '-' }}</td>
              <td>
                <div class="table-actions">
                  <button class="ghost-button" type="button" @click="editingRecord = record">编辑</button>
                  <button class="danger-button" type="button" @click="removeRecord(record)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination">
        <button class="ghost-button" :disabled="page <= 1" @click="loadRecords(page - 1)">上一页</button>
        <button
          v-for="pageNumber in pageNumbers"
          :key="pageNumber"
          class="page-button"
          :class="{ active: pageNumber === page }"
          :disabled="pageNumber === '...' || pageNumber === page"
          type="button"
          @click="typeof pageNumber === 'number' && loadRecords(pageNumber)"
        >
          {{ pageNumber }}
        </button>
        <span>共 {{ totalPages }} 页 / 每页 {{ size }} 条</span>
        <button class="ghost-button" :disabled="page * size >= total" @click="loadRecords(page + 1)">
          下一页
        </button>
      </div>
    </div>

    <TransactionForm
      :editing-record="editingRecord"
      :reset-key="formResetKey"
      :saving="saving"
      @cancel="editingRecord = null"
      @save="saveRecord"
    />
  </section>
</template>
