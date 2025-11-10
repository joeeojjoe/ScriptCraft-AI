<template>
  <div class="script-detail-container">
    <el-page-header @back="goBack" content="脚本详情" />

    <div class="script-content" v-if="scriptDetail">
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <h2>{{ scriptDetail.content.title }}</h2>
            <div class="actions">
              <el-button type="primary" @click="copyScript">
                <el-icon><DocumentCopy /></el-icon>
                复制全文
              </el-button>
              <el-button @click="editing = !editing">
                <el-icon><Edit /></el-icon>
                {{ editing ? '取消编辑' : '编辑脚本' }}
              </el-button>
            </div>
          </div>
        </template>

        <!-- 备选标题 -->
        <div class="section">
          <h3>📌 备选标题</h3>
          <ul class="alt-titles">
            <li v-for="(title, index) in scriptDetail.content.alternativeTitles" :key="index">
              {{ title }}
            </li>
          </ul>
        </div>

        <!-- 分镜列表 -->
        <div class="section">
          <h3>🎬 分镜内容</h3>
          <div
            v-for="(scene, index) in scriptDetail.content.scenes"
            :key="index"
            class="scene-item"
          >
            <div class="scene-header">
              <el-tag type="primary">分镜 {{ index + 1 }}</el-tag>
              <el-tag type="info">{{ scene.timeRange }}</el-tag>
            </div>
            
            <div class="scene-content">
              <div class="scene-header-actions">
                <el-button
                  :type="isSceneLocked(index) ? 'warning' : 'default'"
                  size="small"
                  @click="toggleSceneLock(index)"
                  :icon="isSceneLocked(index) ? Lock : Unlock"
                  :loading="sceneLocking.has(index)"
                  :disabled="sceneLocking.has(index)"
                >
                  {{ isSceneLocked(index) ? '已锁定' : '锁定分镜' }}
                </el-button>
              </div>

              <div class="scene-row">
                <strong>📷 画面描述：</strong>
                <el-input
                  v-if="editing"
                  v-model="scene.visualDescription"
                  type="textarea"
                  :rows="2"
                />
                <p v-else :class="{ 'locked-content': isSceneLocked(index) }">
                  {{ scene.visualDescription }}
                </p>
              </div>

              <div class="scene-row">
                <strong>🎤 文案/旁白：</strong>
                <el-input
                  v-if="editing"
                  v-model="scene.voiceover"
                  type="textarea"
                  :rows="2"
                />
                <p v-else :class="{ 'locked-content': isSceneLocked(index) }">
                  {{ scene.voiceover }}
                </p>
              </div>

              <div class="scene-row">
                <strong>📝 字幕提示：</strong>
                <el-input
                  v-if="editing"
                  v-model="scene.subtitle"
                />
                <p v-else :class="{ 'locked-content': isSceneLocked(index) }">
                  {{ scene.subtitle }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 视频元素建议 -->
        <div class="section">
          <h3>🎨 视频元素建议</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="BGM风格">
              {{ scriptDetail.content.videoElements.bgmStyle }}
            </el-descriptions-item>
            <el-descriptions-item label="拍摄场地">
              {{ scriptDetail.content.videoElements.shootingLocation }}
            </el-descriptions-item>
            <el-descriptions-item label="特效/转场">
              {{ scriptDetail.content.videoElements.effects }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 结尾互动 -->
        <div class="section">
          <h3>💬 结尾互动话术</h3>
          <ul class="cta-list">
            <li v-for="(cta, index) in scriptDetail.content.endingCTA" :key="index">
              {{ cta }}
            </li>
          </ul>
        </div>

        <!-- 保存按钮 -->
        <div class="save-actions" v-if="editing">
          <el-button type="primary" size="large" @click="saveScript">
            保存修改
          </el-button>
          <el-button size="large" @click="editing = false">
            取消
          </el-button>
        </div>

        <!-- 重新生成按钮 -->
        <div class="regenerate-actions">
          <el-button
            type="warning"
            size="large"
            @click="regenerateUnlockedScenes"
            :icon="RefreshRight"
            :loading="regenerating"
            :disabled="regenerating"
          >
            {{ regenerating ? '重新生成中...' : '重新生成未锁定分镜' }}
          </el-button>
          <div class="lock-info">
            <el-icon><InfoFilled /></el-icon>
            <span>锁定你喜欢的分镜，然后点击重新生成来优化其他部分</span>
          </div>
        </div>
      </el-card>
    </div>

    <el-skeleton v-else :rows="10" animated />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getScriptDetail, updateScript, selectScript, updateSceneLock, regenerateScript } from '@/api/script'
import { DocumentCopy, Edit, Lock, Unlock, RefreshRight, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const scriptDetail = ref(null)
const editing = ref(false)
const versionId = route.params.versionId
const lockedScenes = ref(new Set()) // 锁定的分镜索引
const regenerating = ref(false) // 重新生成中状态
const sceneLocking = ref(new Set()) // 正在锁定/解锁的分镜索引

/**
 * 加载脚本详情
 */
const loadDetail = async () => {
  try {
    const detail = await getScriptDetail(versionId)
    scriptDetail.value = detail

    // 解析锁定的分镜
    if (detail.lockedScenes) {
      try {
        const lockedArray = JSON.parse(detail.lockedScenes)
        lockedScenes.value = new Set(lockedArray)
      } catch (e) {
        console.warn('解析锁定分镜数据失败:', e)
        lockedScenes.value = new Set()
      }
    }
  } catch (error) {
    console.error('加载脚本详情失败:', error)
    ElMessage.error('加载失败，请刷新重试')
  }
}

/**
 * 复制脚本
 */
const copyScript = () => {
  if (!scriptDetail.value) return
  
  const content = scriptDetail.value.content
  let text = `【${content.title}】\n\n`
  
  text += '备选标题：\n'
  content.alternativeTitles.forEach((title, i) => {
    text += `${i + 1}. ${title}\n`
  })
  
  text += '\n分镜内容：\n'
  content.scenes.forEach((scene, i) => {
    text += `\n【分镜${i + 1}】${scene.timeRange}\n`
    text += `画面：${scene.visualDescription}\n`
    text += `文案：${scene.voiceover}\n`
    text += `字幕：${scene.subtitle}\n`
  })
  
  text += `\nBGM：${content.videoElements.bgmStyle}\n`
  text += `场地：${content.videoElements.shootingLocation}\n`
  text += `特效：${content.videoElements.effects}\n`
  
  text += '\n结尾话术：\n'
  content.endingCTA.forEach((cta, i) => {
    text += `${i + 1}. ${cta}\n`
  })
  
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

/**
 * 保存脚本
 */
const saveScript = async () => {
  try {
    await updateScript(versionId, {
      content: scriptDetail.value.content
    })
    
    ElMessage.success('保存成功')
    editing.value = false
  } catch (error) {
    console.error('保存失败:', error)
  }
}

  /**
   * 返回上一页
   */
  const goBack = () => {
    router.back()
  }

/**
 * 检查分镜是否被锁定
 */
const isSceneLocked = (index) => {
  return lockedScenes.value.has(index)
}

/**
 * 切换分镜锁定状态
 */
const toggleSceneLock = async (index) => {
  // 如果正在处理中，忽略点击
  if (sceneLocking.value.has(index)) {
    return
  }

  const currentlyLocked = isSceneLocked(index)
  const willBeLocked = !currentlyLocked

  try {
    sceneLocking.value.add(index)
    await updateSceneLock(versionId, index, willBeLocked)

    if (willBeLocked) {
      lockedScenes.value.add(index)
      ElMessage.success(`分镜 ${index + 1} 已锁定`)
    } else {
      lockedScenes.value.delete(index)
      ElMessage.success(`分镜 ${index + 1} 已解锁`)
    }
  } catch (error) {
    console.error('更新分镜锁定状态失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    sceneLocking.value.delete(index)
  }
}

/**
 * 重新生成未锁定分镜
 */
const regenerateUnlockedScenes = async () => {
  if (lockedScenes.value.size === 0) {
    ElMessage.warning('请先锁定至少一个分镜')
    return
  }

  try {
    regenerating.value = true
    ElMessage.info('正在重新生成分镜，请稍候...')

    const result = await regenerateScript(versionId)
    scriptDetail.value.content = result
    ElMessage.success('重新生成完成！锁定的分镜保持不变')
  } catch (error) {
    console.error('重新生成失败:', error)
    ElMessage.error('重新生成失败，请重试')
  } finally {
    regenerating.value = false
  }
}

/**
 * 保存脚本前更新选中状态
 */
const updateSelectedStatus = async () => {
  if (scriptDetail.value && scriptDetail.value.isSelected) {
    try {
      await selectScript(scriptDetail.value.id)
    } catch (error) {
      console.error('更新选中状态失败:', error)
    }
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.script-detail-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.script-content {
  max-width: 1000px;
  margin: 20px auto;
}

.detail-card {
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
  font-size: 22px;
  flex: 1;
}

.actions {
  display: flex;
  gap: 12px;
}

.section {
  margin: 30px 0;
}

.section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 18px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.alt-titles, .cta-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.alt-titles li, .cta-list li {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
  color: #606266;
}

.scene-item {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.scene-header {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.scene-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scene-row strong {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.scene-row p {
  margin: 0;
  color: #303133;
  line-height: 1.8;
}

.save-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 30px;
}

.regenerate-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #d4e4ff;
}

.lock-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.scene-header-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.locked-content {
  background: #fff7e6;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid #e6a23c;
}

@media (max-width: 768px) {
  .script-detail-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .actions {
    width: 100%;
  }
  
  .actions .el-button {
    flex: 1;
  }
}
</style>

