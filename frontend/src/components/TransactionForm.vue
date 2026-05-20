<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

import type { TransactionForm, TransactionRecord, TransactionType } from '../types/transaction'

const props = defineProps<{
  editingRecord: TransactionRecord | null
  resetKey: number
  saving: boolean
}>()

const emit = defineEmits<{
  save: [form: TransactionForm, id?: number]
  cancel: []
}>()

const incomeCategories = ['工资', '奖金', '副业收入', '转账收入', '其它收入']
const expenseCategories = ['餐饮', '交通', '房租', '游戏', '购物', '娱乐', '医疗', '数码', '其它']
const paymentMethods = ['微信', '支付宝', '银行卡', '现金', '信用卡', '其它']

const today = new Date().toISOString().slice(0, 10)
const formElement = ref<HTMLFormElement | null>(null)

const form = reactive<TransactionForm>({
  type: 'expense',
  category: '餐饮',
  amount: '',
  paymentMethod: '微信',
  transactionDate: today,
  note: '',
})

const categories = computed(() => (form.type === 'income' ? incomeCategories : expenseCategories))

watch(
  () => props.editingRecord,
  (record) => {
    if (!record) {
      resetForm()
      return
    }

    form.type = record.type
    form.category = record.category
    form.amount = String(record.amount)
    form.paymentMethod = record.paymentMethod || ''
    form.transactionDate = record.transactionDate
    form.note = record.note || ''
  },
  { immediate: true },
)

watch(
  () => props.resetKey,
  () => {
    if (!props.editingRecord) {
      resetForm()
    }
  },
)

watch(
  () => form.type,
  (type) => {
    const defaultCategory = type === 'income' ? incomeCategories[0] : expenseCategories[0]
    if (!categories.value.includes(form.category)) {
      form.category = defaultCategory
    }
    if (type === 'income') {
      form.paymentMethod = ''
    }
  },
)

function resetForm() {
  form.type = 'expense'
  form.category = '餐饮'
  form.amount = ''
  form.paymentMethod = '微信'
  form.transactionDate = today
  form.note = ''
}

function selectType(type: TransactionType) {
  form.type = type
}

function handleSubmit() {
  if (formElement.value && !formElement.value.reportValidity()) {
    return
  }
  emit('save', { ...form }, props.editingRecord?.id)
}
</script>

<template>
  <form ref="formElement" class="panel form-panel" @submit.prevent="handleSubmit">
    <div class="panel-title">
      <div>
        <p class="eyebrow">Transaction</p>
        <h2>{{ editingRecord ? '编辑流水' : '新增流水' }}</h2>
      </div>
      <button v-if="editingRecord" class="ghost-button" type="button" @click="emit('cancel')">
        取消编辑
      </button>
    </div>

    <div class="segmented">
      <button
        type="button"
        :class="{ active: form.type === 'expense' }"
        @click="selectType('expense')"
      >
        支出
      </button>
      <button
        type="button"
        :class="{ active: form.type === 'income' }"
        @click="selectType('income')"
      >
        收入
      </button>
    </div>

    <label>
      金额
      <input v-model="form.amount" min="0.01" step="0.01" type="number" required placeholder="0.00" />
    </label>

    <label>
      分类
      <select v-model="form.category" required>
        <option v-for="category in categories" :key="category" :value="category">
          {{ category }}
        </option>
      </select>
    </label>

    <label v-if="form.type === 'expense'">
      支付方式
      <select v-model="form.paymentMethod">
        <option v-for="method in paymentMethods" :key="method" :value="method">
          {{ method }}
        </option>
      </select>
    </label>

    <label>
      日期
      <input v-model="form.transactionDate" type="date" required />
    </label>

    <label>
      备注
      <textarea v-model="form.note" maxlength="255" rows="3" placeholder="可填写用途或说明" />
    </label>

    <div class="form-actions">
      <button class="primary-button" type="button" :disabled="saving" @click="handleSubmit">
        {{ saving ? '保存中...' : editingRecord ? '保存修改' : '新增流水' }}
      </button>
    </div>
  </form>
</template>
