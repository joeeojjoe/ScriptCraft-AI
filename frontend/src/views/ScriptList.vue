<template>
  <div class="script-list-container">
    <el-page-header @back="goBack" content="脚本详情" />

    <div class="script-content">
      <el-card v-if="currentVersion" class="script-card selected-card">
        <template #header>
          <div class="card-header">
            <h2>{{ currentVersion.title }}</h2>
            <div class="stats">
              <el-tag>{{ currentVersion.preview.sceneCount }} 个分镜</el-tag>
              <el-tag type="info">{{ currentVersion.preview.wordCount }} 字</el-tag>
              <el-tag type="success">已选中</el-tag>
            </div>
          </div>
        </template>

        <div class="preview-content">
          <h3>✨ 开场预览</h3>
          <p>{{ currentVersion.preview.firstScene }}</p>
        </div>

        <div class="actions">
          <el-button
            type="primary"
            size="large"
            @click="viewDetail(currentVersion.versionId)"
          >
            <el-icon><View /></el-icon>
            查看完整脚本
          </el-button>
          <el-button
            type="warning"
            size="large"
            @click="regenerateScript"
          >
            <el-icon><RefreshRight /></el-icon>
            重新生成脚本
          </el-button>
        </div>
      </el-card>

      <el-empty v-else description="暂无脚本数据" />

      <div class="bottom-actions">
        <el-button @click="regenerate">
          <el-icon><RefreshRight /></el-icon>
          不满意？重新生成
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSessionVersions, selectScript as selectScriptApi } from '@/api/script'
import { useScriptStore } from '@/stores/script'
import { View, Select, RefreshRight, Check } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const scriptStore = useScriptStore()

const currentVersion = ref(null)
const sessionId = route.params.sessionId

/**
 * 加载脚本
 */
const loadVersions = async () => {
  try {
    const res = await getSessionVersions(sessionId)
    if (res && res.length > 0) {
      currentVersion.value = {
        ...res[0],
        isSelected: true // 只有一个版本，默认为选中状态
      }
    }
  } catch (error) {
    console.error('加载脚本失败:', error)
    ElMessage.error('加载失败，请刷新重试')
  }
}

/**
 * 查看脚本详情
 */
const viewDetail = (versionId) => {
  router.push(`/script/detail/${versionId}`)
}

/**
 * 重新生成脚本
 */
const regenerateScript = async () => {
  try {
    const result = await ElMessageBox.confirm(
      '确定要重新生成脚本吗？这将创建一个全新的脚本。',
      '确认重新生成',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 导航到生成页面重新生成
    router.push('/generate')
  } catch {
    // 用户取消
  }
}

/**
 * 重新生成
 */
const regenerate = async () => {
  try {
    await ElMessageBox.confirm(
      '重新生成将返回生成页面，当前脚本将保留在历史记录中',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    router.push('/generate')
  } catch {
    // 用户取消
  }
}

  /**
   * 返回上一页
   */
  const goBack = () => {
    router.push('/home')
  }

  /**
   * 页面激活时重新加载数据（解决从详情页返回时状态丢失问题）
   */
  onActivated(() => {
    loadVersions()
  })

onMounted(() => {
  loadVersions()
})
</script>

<style scoped>
.script-list-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.script-content {
  max-width: 1000px;
  margin: 20px auto;
}

.script-tabs {
  margin-top: 20px;
}

.script-card {
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

.stats {
  display: flex;
  gap: 8px;
}

.preview-content {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.preview-content h3 {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 16px;
}

.preview-content p {
  margin: 0;
  color: #303133;
  font-size: 14px;
  line-height: 1.8;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.bottom-actions {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .script-list-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .actions {
    flex-direction: column;
  }
  
  .actions .el-button {
    width: 100%;
  }
}
</style>

