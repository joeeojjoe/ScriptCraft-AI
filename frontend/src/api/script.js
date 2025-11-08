import request from '@/utils/request'

/**
 * 生成脚本
 * 
 * @param {object} data 生成参数
 * @returns {Promise}
 */
export function generateScript(data) {
  return request({
    url: '/scripts/generate',
    method: 'post',
    data
  })
}

/**
 * 获取脚本版本详情
 * 
 * @param {string} versionId 版本ID
 * @returns {Promise}
 */
export function getScriptDetail(versionId) {
  return request({
    url: `/scripts/versions/${versionId}`,
    method: 'get'
  })
}

/**
 * 更新脚本内容
 * 
 * @param {string} versionId 版本ID
 * @param {object} data 更新数据
 * @returns {Promise}
 */
export function updateScript(versionId, data) {
  return request({
    url: `/scripts/versions/${versionId}`,
    method: 'put',
    data
  })
}

/**
 * 标记选中脚本
 * 
 * @param {string} versionId 版本ID
 * @returns {Promise}
 */
export function selectScript(versionId) {
  return request({
    url: `/scripts/versions/${versionId}/select`,
    method: 'post'
  })
}

/**
 * 获取会话详情
 * 
 * @param {string} sessionId 会话ID
 * @returns {Promise}
 */
export function getSessionVersions(sessionId) {
  return request({
    url: `/scripts/sessions/${sessionId}`,
    method: 'get'
  })
}

/**
 * 获取生成历史
 * 
 * @param {object} params 查询参数
 * @returns {Promise}
 */
export function getScriptHistory(params) {
  return request({
    url: '/scripts/sessions',
    method: 'get',
    params
  })
}

