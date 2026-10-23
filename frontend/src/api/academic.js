import request from './request'

/* ---------- 年级 ---------- */
export function getGrades() {
  return request.get('/admin/academic/grades')
}

/* ---------- 班级 ---------- */
export function getClasses() {
  return request.get('/admin/academic/classes')
}

export function createClass(data) {
  return request.post('/admin/academic/classes', data)
}

export function updateClass(id, data) {
  return request.put(`/admin/academic/classes/${id}`, data)
}

/* ---------- 学生 ---------- */
export function getStudents(params) {
  return request.get('/admin/academic/students', { params })
}

export function createStudent(data) {
  return request.post('/admin/academic/students', data)
}

export function updateStudent(id, data) {
  return request.put(`/admin/academic/students/${id}`, data)
}

/* ---------- 学年 ---------- */
export function getSchoolYears() {
  return request.get('/admin/academic/school-years')
}

export function createSchoolYear(data) {
  return request.post('/admin/academic/school-years', data)
}

export function setCurrentSchoolYear(id) {
  return request.post(`/admin/academic/school-years/${id}/current`)
}

/* ---------- 升级 / 毕业 ---------- */
export function getPromotionPreview() {
  return request.get('/admin/academic/promotion/preview')
}

export function executePromotion() {
  return request.post('/admin/academic/promotion/execute')
}
