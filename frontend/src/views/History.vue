<template>
  <div class="history-container">
    <el-page-header @back="goBack" content="生成历史" />

    <div class="history-content">
      <el-card class="history-card">
        <template #header>
          <div class="card-header">
            <h2>📚 历史记录</h2>
            <el-select
                v-model="keyword"
                placeholder="请选择主题"
                style="width: 300px"
                clearable
                @change="handleSearch"
            >
              <el-option
                  v-for="item in VIDEO_TYPES"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </div>
        </template>

        <el-table
          :data="historyList"
          v-loading="loading"
          style="width: 100%"
          @row-click="handleRowClick"
          class="history-table"
        >
          <el-table-column label="主题" prop="themeInput" min-width="200">
            <template #default="{ row }">
              <div class="theme-cell">
                <el-tag size="small" type="primary">
                  {{ getVideoTypeLabel(row.videoType) }}
                </el-tag>
                <span>{{ row.themeInput }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="风格" prop="stylePreference" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.stylePreference" size="small" type="info">
                {{ getStyleLabel(row.stylePreference) }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="创建时间" prop="createdAt" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="viewSession(row.id)">
                查看
              </el-button>
              <el-button type="danger" link @click.stop="deleteSession(row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadHistory"
          @size-change="loadHistory"
          class="pagination"
        />

        <el-empty
          v-if="!loading && historyList.length === 0"
          description="暂无历史记录"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {deleteScriptSession, getScriptHistory} from '@/api/script'
import {getVideoTypeLabel, getStyleLabel, VIDEO_TYPES} from '@/utils/constants'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

/**
 * 加载历史记录
 */
const loadHistory = async () => {
  loading.value = true
  
  try {
    const res = await getScriptHistory({
      page: currentPage.value,
      pageSize: pageSize.value,
      videoType: keyword.value || undefined
    })
    
    historyList.value = res.sessions
    total.value = res.total
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索处理
 */
const handleSearch = () => {
  currentPage.value = 1
  loadHistory()
}

/**
 * 查看会话
 */
const viewSession = (sessionId) => {
  router.push(`/script/${sessionId}`)
}

/**
 * 删除会话
 */
const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用删除接口
    await deleteScriptSession(sessionId)
    ElMessage.success('删除成功')
    await loadHistory()
  } catch {
    // 用户取消或删除失败
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '请稍后重试'))
    }
  }
}

/**
 * 行点击
 */
const handleRowClick = (row) => {
  viewSession(row.id)
}

/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.push('/home')
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.history-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.history-content {
  max-width: 1200px;
  margin: 20px auto;
}

.history-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.theme-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-cell span {
  color: #606266;
}

.history-table :deep(.el-table__row) {
  cursor: pointer;
}

.history-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}

@media (max-width: 768px) {
  .history-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header .el-input {
    width: 100% !important;
  }
}
</style>

