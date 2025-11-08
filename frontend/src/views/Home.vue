<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1 class="logo">🎬 ScriptCraft AI</h1>
          <div class="header-right">
            <span class="welcome">欢迎，{{ userStore.userInfo?.nickname }}</span>
            <el-button type="primary" @click="logout">退出登录</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main">
        <div class="hero-section">
          <h1 class="title">AI短视频脚本生成器</h1>
          <p class="subtitle">一键生成专业脚本，释放创作灵感</p>
          
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="goToGenerate">
              <el-icon><Edit /></el-icon>
              开始创作
            </el-button>
            <el-button size="large" @click="goToHistory">
              <el-icon><Clock /></el-icon>
              历史记录
            </el-button>
          </div>
        </div>

        <div class="features">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="feature-card">
                <el-icon class="feature-icon" color="#409eff"><Lightning /></el-icon>
                <h3>快速生成</h3>
                <p>1分钟内生成2-3个专业脚本方案</p>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="feature-card">
                <el-icon class="feature-icon" color="#67c23a"><PictureRounded /></el-icon>
                <h3>多种风格</h3>
                <p>支持幽默、专业、温情等多种创作风格</p>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="feature-card">
                <el-icon class="feature-icon" color="#e6a23c"><Edit /></el-icon>
                <h3>灵活编辑</h3>
                <p>所见即所得，随时编辑修改脚本内容</p>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-card class="feature-card">
                <el-icon class="feature-icon" color="#f56c6c"><DocumentCopy /></el-icon>
                <h3>一键复制</h3>
                <p>复制脚本内容，直接用于视频拍摄</p>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Lightning, Edit, DocumentCopy, Clock, PictureRounded } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

/**
 * 跳转到脚本生成页
 */
const goToGenerate = () => {
  router.push('/generate')
}

/**
 * 跳转到历史记录页
 */
const goToHistory = () => {
  router.push('/history')
}

/**
 * 退出登录
 */
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome {
  color: #606266;
}

.main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.hero-section {
  text-align: center;
  padding: 60px 0;
}

.title {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 16px 0;
}

.subtitle {
  font-size: 20px;
  color: #909399;
  margin: 0 0 40px 0;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.features {
  margin-top: 60px;
}

.feature-card {
  text-align: center;
  padding: 20px;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-card h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
}

.feature-card p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .title {
    font-size: 32px;
  }
  
  .subtitle {
    font-size: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
}
</style>

