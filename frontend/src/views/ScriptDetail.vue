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
              <div class="scene-row">
                <strong>📷 画面描述：</strong>
                <el-input
                  v-if="editing"
                  v-model="scene.visualDescription"
                  type="textarea"
                  :rows="2"
                />
                <p v-else>{{ scene.visualDescription }}</p>
              </div>
              
              <div class="scene-row">
                <strong>🎤 文案/旁白：</strong>
                <el-input
                  v-if="editing"
                  v-model="scene.voiceover"
                  type="textarea"
                  :rows="2"
                />
                <p v-else>{{ scene.voiceover }}</p>
              </div>
              
              <div class="scene-row">
                <strong>📝 字幕提示：</strong>
                <el-input
                  v-if="editing"
                  v-model="scene.subtitle"
                />
                <p v-else>{{ scene.subtitle }}</p>
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
      </el-card>
    </div>

    <el-skeleton v-else :rows="10" animated />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getScriptDetail, updateScript, selectScript } from '@/api/script'
import { DocumentCopy, Edit } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const scriptDetail = ref(null)
const editing = ref(false)
const versionId = route.params.versionId

/**
 * 加载脚本详情
 */
const loadDetail = async () => {
  try {
    scriptDetail.value = await getScriptDetail(versionId)
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

