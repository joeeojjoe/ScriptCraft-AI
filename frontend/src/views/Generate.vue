<template>
  <div class="generate-container">
    <el-page-header @back="goBack" content="生成脚本" />

    <div class="generate-content">
      <el-card class="form-card">
        <template #header>
          <h2>📝 填写脚本需求</h2>
        </template>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
          size="large"
        >
          <el-form-item label="视频类型" prop="videoType">
            <el-select
              v-model="formData.videoType"
              placeholder="请选择视频类型"
              style="width: 100%"
            >
              <el-option
                v-for="item in VIDEO_TYPES"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="主题描述" prop="themeInput">
            <el-input
              v-model="formData.themeInput"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="请描述您的视频主题，例如：如何在30天内学会弹吉他"
            />
            <div class="input-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>建议：描述越详细，生成的脚本越精准</span>
            </div>
          </el-form-item>

          <el-form-item label="风格偏好" prop="stylePreference">
            <el-radio-group v-model="formData.stylePreference">
              <el-radio
                v-for="item in STYLE_PREFERENCES"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleGenerate"
              style="width: 100%"
            >
              <el-icon v-if="!loading"><PieChart /></el-icon>
              {{ loading ? '正在生成中，请稍候...' : '一键生成脚本' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="example-section">
          <h3>💡 主题示例</h3>
          <el-tag
            v-for="example in examples"
            :key="example"
            class="example-tag"
            @click="useExample(example)"
          >
            {{ example }}
          </el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { generateScript } from '@/api/script'
import { useScriptStore } from '@/stores/script'
import { VIDEO_TYPES, STYLE_PREFERENCES } from '@/utils/constants'
import { InfoFilled, PieChart } from '@element-plus/icons-vue'

const router = useRouter()
const scriptStore = useScriptStore()

const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  videoType: '',
  themeInput: '',
  stylePreference: 'humorous'
})

const rules = {
  videoType: [
    { required: true, message: '请选择视频类型', trigger: 'change' }
  ],
  themeInput: [
    { required: true, message: '请输入主题描述', trigger: 'blur' },
    { min: 1, max: 200, message: '主题描述长度为1-200字', trigger: 'blur' }
  ]
}

const examples = [
  '如何在30天内学会弹吉他',
  '5分钟快速做出美味早餐',
  '手机摄影小技巧，随手拍出大片',
  '职场新人必备的5个沟通技巧',
  '开箱评测：最新款蓝牙耳机'
]

/**
 * 处理生成脚本
 */
const handleGenerate = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      const res = await generateScript(formData)
      
      ElMessage.success('脚本生成成功！')
      
      // 保存到store
      scriptStore.setCurrentSession(res.sessionId, res.versions)
      
      // 跳转到脚本列表页
      router.push(`/script/${res.sessionId}`)
    } catch (error) {
      console.error('生成脚本失败:', error)
    } finally {
      loading.value = false
    }
  })
}

/**
 * 使用示例主题
 */
const useExample = (example) => {
  formData.themeInput = example
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.generate-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.generate-content {
  max-width: 800px;
  margin: 20px auto;
}

.form-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.form-card h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.input-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

.example-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.example-section h3 {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 16px;
}

.example-tag {
  margin: 0 8px 8px 0;
  cursor: pointer;
  transition: all 0.3s;
}

.example-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .generate-container {
    padding: 10px;
  }
  
  .generate-content {
    margin: 10px auto;
  }
}
</style>

