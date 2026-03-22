<template>
  <div class="purchase-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>采购订单</span>
              <div class="header-actions">
                <el-select v-model="filters.status" placeholder="状态" clearable>
                  <el-option label="待采购" value="pending" />
                  <el-option label="采购中" value="processing" />
                  <el-option label="已采购" value="completed" />
                  <el-option label="采购失败" value="failed" />
                </el-select>
                <el-button type="primary" @click="handleAutoPurchase">一键采购</el-button>
              </div>
            </div>
          </template>
          
          <el-table :data="purchaseList" style="width: 100%">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="orderNo" label="销售订单号" width="180" />
            <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="supplier" label="供应商" width="150" />
            <el-table-column prop="sourcePrice" label="采购价" width="100">
              <template #default="{ row }">¥{{ row.sourcePrice.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="sellPrice" label="售价" width="100">
              <template #default="{ row }">¥{{ row.sellPrice.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="profit" label="利润" width="100">
              <template #default="{ row }">
                <span :class="{ 'profit-positive': row.profit > 0, 'profit-negative': row.profit < 0 }">
                  ¥{{ row.profit.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentType" label="支付方式" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.paymentType === 'auto'" type="success">自动</el-tag>
                <el-tag v-else type="warning">人工</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handlePurchase(row)" v-if="row.status === 'pending'">立即采购</el-button>
                <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>采购统计</span>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">¥12,450.00</div>
              <div class="stat-label">今日采购额</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">¥3,280.00</div>
              <div class="stat-label">今日利润</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">26.3%</div>
              <div class="stat-label">利润率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">45</div>
              <div class="stat-label">采购订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待人工确认</span>
          </template>
          <el-table :data="manualConfirmList" style="width: 100%" max-height="200">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleConfirm(row)">确认</el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const filters = reactive({
  status: ''
})

const purchaseList = ref([
  { orderNo: 'DD202401150001', productName: 'iPhone 15 Pro Max 256GB', supplier: '深圳数码批发', sourcePrice: 7500, sellPrice: 8999, profit: 1499, status: 'pending', statusText: '待采购', paymentType: 'auto' },
  { orderNo: 'DD202401150002', productName: 'MacBook Air M3', supplier: '广州电子城', sourcePrice: 7200, sellPrice: 8499, profit: 1299, status: 'processing', statusText: '采购中', paymentType: 'auto' },
  { orderNo: 'DD202401150003', productName: 'AirPods Pro 2', supplier: '东莞耳机批发', sourcePrice: 1400, sellPrice: 1899, profit: 499, status: 'completed', statusText: '已采购', paymentType: 'auto' },
  { orderNo: 'DD202401150004', productName: 'iPad Pro 12.9', supplier: '深圳数码批发', sourcePrice: 9200, sellPrice: 10999, profit: 1799, status: 'pending', statusText: '待采购', paymentType: 'manual' },
  { orderNo: 'DD202401150005', productName: 'Apple Watch Ultra 2', supplier: '广州电子城', sourcePrice: 5500, sellPrice: 6499, profit: 999, status: 'failed', statusText: '采购失败', paymentType: 'auto' }
])

const manualConfirmList = ref([
  { orderNo: 'DD202401150004', amount: 9200, reason: '金额超过自动采购阈值' },
  { orderNo: 'DD202401150006', amount: 15000, reason: '新供应商首次采购' }
])

const getStatusType = (status) => {
  const types = { pending: 'warning', processing: 'primary', completed: 'success', failed: 'danger' }
  return types[status] || 'info'
}

const handleAutoPurchase = () => {
  console.log('一键采购')
}

const handlePurchase = (row) => {
  console.log('采购:', row)
}

const handleDetail = (row) => {
  console.log('详情:', row)
}

const handleConfirm = (row) => {
  console.log('确认:', row)
}

const handleReject = (row) => {
  console.log('拒绝:', row)
}
</script>

<style lang="scss" scoped>
.purchase-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .profit-positive {
    color: #67c23a;
    font-weight: 500;
  }
  
  .profit-negative {
    color: #f56c6c;
    font-weight: 500;
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    
    .stat-item {
      text-align: center;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 8px;
      
      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }
  
  .mt-20 {
    margin-top: 20px;
  }
}
</style>