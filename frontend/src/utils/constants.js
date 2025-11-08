/**
 * 视频类型选项
 */
export const VIDEO_TYPES = [
  { value: 'product_review', label: '产品测评' },
  { value: 'knowledge', label: '知识科普' },
  { value: 'vlog', label: 'Vlog日记' },
  { value: 'comedy', label: '搞笑剧情' },
  { value: 'food', label: '美食制作' },
  { value: 'makeup', label: '美妆教程' },
  { value: 'movie', label: '影视解说' },
  { value: 'unboxing', label: '开箱体验' },
  { value: 'skill', label: '技能教学' }
]

/**
 * 风格偏好选项
 */
export const STYLE_PREFERENCES = [
  { value: 'humorous', label: '幽默风趣' },
  { value: 'professional', label: '专业严谨' },
  { value: 'cute', label: '亲切可爱' },
  { value: 'passionate', label: '激情澎湃' },
  { value: 'emotional', label: '温情故事' },
  { value: 'suspenseful', label: '悬念刺激' }
]

/**
 * 获取视频类型标签
 * 
 * @param {string} value 类型值
 * @returns {string} 类型标签
 */
export function getVideoTypeLabel(value) {
  const type = VIDEO_TYPES.find(t => t.value === value)
  return type ? type.label : value
}

/**
 * 获取风格标签
 * 
 * @param {string} value 风格值
 * @returns {string} 风格标签
 */
export function getStyleLabel(value) {
  const style = STYLE_PREFERENCES.find(s => s.value === value)
  return style ? style.label : value
}

