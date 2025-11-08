<template>
  <div class="script-list-container">
    <el-page-header @back="goBack" content="选择脚本方案" />

    <div class="script-content">
      <el-tabs v-model="activeTab" type="card" class="script-tabs">
        <el-tab-pane
          v-for="version in versions"
          :key="version.versionId"
          :label="`方案 ${version.versionIndex}`"
          :name="version.versionId"
        >
          <el-card class="script-card">
            <template #header>
              <div class="card-header">
                <h2>{{ version.title }}</h2>
                <div class="stats">
                  <el-tag>{{ version.preview.sceneCount }} 个分镜</el-tag>
                  <el-tag type="info">{{ version.preview.wordCount }} 字</el-tag>
                </div>
              </div>
            </template>

            <div class="preview-content">
              <h3>✨ 开场预览</h3>
              <p>{{ version.preview.firstScene }}</p>
            </div>

            <div class="actions">
              <el-button
                type="primary"
                size="large"
                @click="viewDetail(version.versionId)"
              >
                <el-icon><View /></el-icon>
                查看完整脚本
              </el-button>
              <el-button
                size="large"
                @click="selectScript(version.versionId)"
              >
                <el-icon><Select /></el-icon>
                选择此方案
              </el-button>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>

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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSessionVersions, selectScript as selectScriptApi } from '@/api/script'
import { useScriptStore } from '@/stores/script'
import { View, Select, RefreshRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const scriptStore = useScriptStore()

const activeTab = ref('')
const versions = ref([])
const sessionId = route.params.sessionId

/**
 * 加载脚本列表
 */
const loadVersions = async () => {
  try {
    const res = await getSessionVersions(sessionId)
    versions.value = res
    
    if (res.length > 0) {
      activeTab.value = res[0].versionId
    }
  } catch (error) {
    console.error('加载脚本列表失败:', error)
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
 * 选择脚本
 */
const selectScript = async (versionId) => {
  try {
    await selectScriptApi(versionId)
    ElMessage.success('已标记为选中方案')
  } catch (error) {
    console.error('选择脚本失败:', error)
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

