import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 脚本状态管理
 */
export const useScriptStore = defineStore('script', () => {
  // 当前会话ID
  const currentSessionId = ref('')
  
  // 当前选中的脚本版本
  const currentVersions = ref([])
  
  // 脚本详情缓存
  const scriptCache = ref(new Map())

  /**
   * 设置当前会话
   * 
   * @param {string} sessionId 会话ID
   * @param {array} versions 版本列表
   */
  function setCurrentSession(sessionId, versions) {
    currentSessionId.value = sessionId
    currentVersions.value = versions
  }

  /**
   * 缓存脚本详情
   * 
   * @param {string} versionId 版本ID
   * @param {object} detail 脚本详情
   */
  function cacheScriptDetail(versionId, detail) {
    scriptCache.value.set(versionId, detail)
  }

  /**
   * 获取缓存的脚本详情
   * 
   * @param {string} versionId 版本ID
   * @returns {object|null} 脚本详情
   */
  function getCachedScript(versionId) {
    return scriptCache.value.get(versionId) || null
  }

  /**
   * 清除缓存
   */
  function clearCache() {
    scriptCache.value.clear()
  }

  return {
    currentSessionId,
    currentVersions,
    scriptCache,
    setCurrentSession,
    cacheScriptDetail,
    getCachedScript,
    clearCache
  }
})

