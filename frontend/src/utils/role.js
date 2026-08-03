/**
 * 角色 → 首页路径 / 欢迎语称呼 / 角色中文名 映射
 * 角色编码见 sys_role.code：ADMIN / PRINCIPAL / GRADE_ADMIN / TEACHER / STUDENT
 */
export const ROLE_MAP = {
  ADMIN: {
    path: '/admin',
    suffix: '管理员', // 欢迎语后缀：欢迎 XXX 管理员
    name: '系统管理员' // 角色标签中文名
  },
  PRINCIPAL: {
    path: '/principal',
    suffix: '校长',
    name: '校长'
  },
  GRADE_ADMIN: {
    path: '/grade-admin',
    suffix: '年级管理员',
    name: '年级管理员'
  },
  TEACHER: {
    path: '/teacher',
    suffix: '老师',
    name: '教师'
  },
  STUDENT: {
    path: '/student',
    suffix: '同学',
    name: '学生'
  }
}

/**
 * 根据用户角色返回其对应的首页路径
 * 按 roles 顺序取第一个可识别角色；无匹配时返回默认首页 '/'
 */
export function getHomePath(roles = []) {
  for (const code of roles) {
    if (ROLE_MAP[code]) {
      return ROLE_MAP[code].path
    }
  }
  return '/'
}

/**
 * 根据用户角色返回中文角色名（用于标签展示）
 */
export function getRoleName(roles = []) {
  for (const code of roles) {
    if (ROLE_MAP[code]) {
      return ROLE_MAP[code].name
    }
  }
  return '未知角色'
}
