import request from './request'

/* ---------- 科目 ---------- */
export function getSubjects() {
  return request.get('/admin/timetable/subjects')
}

/* ---------- 年级课程计划 ---------- */
export function getGradePlan(gradeId) {
  return request.get(`/admin/timetable/plans/${gradeId}`)
}

export function saveGradePlan(gradeId, items) {
  return request.put(`/admin/timetable/plans/${gradeId}`, items)
}

/* ---------- 节次作息 ---------- */
export function getPeriods(stage) {
  return request.get('/admin/timetable/periods', { params: { stage } })
}

/* ---------- 班级课表 ---------- */
export function getTimetable(params) {
  return request.get('/admin/timetable', { params })
}

export function saveTimetable(data) {
  return request.put('/admin/timetable', data)
}
