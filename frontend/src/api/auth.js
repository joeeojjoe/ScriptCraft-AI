import request from '@/utils/request'

/**
 * 用户注册
 * 
 * @param {object} data 注册数据
 * @returns {Promise}
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * 
 * @param {object} data 登录数据
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 * 
 * @returns {Promise}
 */
export function getUserProfile() {
  return request({
    url: '/auth/profile',
    method: 'get'
  })
}

