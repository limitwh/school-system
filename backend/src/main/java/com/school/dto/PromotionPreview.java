package com.school.dto;

import java.util.List;

/**
 * 升级/毕业预览
 */
public class PromotionPreview {

    /** 当前学年名称（未设置时为空） */
    private String schoolYear;
    /** 本学年是否已执行过升级 */
    private boolean promotionExecuted;
    /** 本学年是否已执行过毕业 */
    private boolean graduationExecuted;
    /** 待升级学生总数 */
    private int promotionStudentCount;
    /** 待毕业学生总数 */
    private int graduationStudentCount;
    private List<PromotionItem> promotions;
    private List<GraduationItem> graduations;

    public String getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public boolean isPromotionExecuted() {
        return promotionExecuted;
    }

    public void setPromotionExecuted(boolean promotionExecuted) {
        this.promotionExecuted = promotionExecuted;
    }

    public boolean isGraduationExecuted() {
        return graduationExecuted;
    }

    public void setGraduationExecuted(boolean graduationExecuted) {
        this.graduationExecuted = graduationExecuted;
    }

    public int getPromotionStudentCount() {
        return promotionStudentCount;
    }

    public void setPromotionStudentCount(int promotionStudentCount) {
        this.promotionStudentCount = promotionStudentCount;
    }

    public int getGraduationStudentCount() {
        return graduationStudentCount;
    }

    public void setGraduationStudentCount(int graduationStudentCount) {
        this.graduationStudentCount = graduationStudentCount;
    }

    public List<PromotionItem> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<PromotionItem> promotions) {
        this.promotions = promotions;
    }

    public List<GraduationItem> getGraduations() {
        return graduations;
    }

    public void setGraduations(List<GraduationItem> graduations) {
        this.graduations = graduations;
    }

    /** 单个班级的升级预览 */
    public static class PromotionItem {
        private Long classId;
        private String className;
        private String fromGrade;
        private String toGrade;
        private long studentCount;

        public PromotionItem() {
        }

        public PromotionItem(Long classId, String className, String fromGrade, String toGrade, long studentCount) {
            this.classId = classId;
            this.className = className;
            this.fromGrade = fromGrade;
            this.toGrade = toGrade;
            this.studentCount = studentCount;
        }

        public Long getClassId() {
            return classId;
        }

        public void setClassId(Long classId) {
            this.classId = classId;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getFromGrade() {
            return fromGrade;
        }

        public void setFromGrade(String fromGrade) {
            this.fromGrade = fromGrade;
        }

        public String getToGrade() {
            return toGrade;
        }

        public void setToGrade(String toGrade) {
            this.toGrade = toGrade;
        }

        public long getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(long studentCount) {
            this.studentCount = studentCount;
        }
    }

    /** 单个班级的毕业预览 */
    public static class GraduationItem {
        private Long classId;
        private String className;
        private String gradeName;
        private long studentCount;

        public GraduationItem() {
        }

        public GraduationItem(Long classId, String className, String gradeName, long studentCount) {
            this.classId = classId;
            this.className = className;
            this.gradeName = gradeName;
            this.studentCount = studentCount;
        }

        public Long getClassId() {
            return classId;
        }

        public void setClassId(Long classId) {
            this.classId = classId;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getGradeName() {
            return gradeName;
        }

        public void setGradeName(String gradeName) {
            this.gradeName = gradeName;
        }

        public long getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(long studentCount) {
            this.studentCount = studentCount;
        }
    }
}
