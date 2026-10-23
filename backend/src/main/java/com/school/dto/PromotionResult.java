package com.school.dto;

/**
 * 升级/毕业执行结果
 */
public class PromotionResult {

    private String schoolYear;
    /** 升级的班级数 */
    private int promotedClasses;
    /** 升级的学生数 */
    private int promotedStudents;
    /** 毕业的班级数 */
    private int graduatedClasses;
    /** 毕业的学生数 */
    private int graduatedStudents;
    /** 本次是否跳过了升级（本学年已执行过） */
    private boolean promotionSkipped;
    /** 本次是否跳过了毕业（本学年已执行过） */
    private boolean graduationSkipped;

    public String getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public int getPromotedClasses() {
        return promotedClasses;
    }

    public void setPromotedClasses(int promotedClasses) {
        this.promotedClasses = promotedClasses;
    }

    public int getPromotedStudents() {
        return promotedStudents;
    }

    public void setPromotedStudents(int promotedStudents) {
        this.promotedStudents = promotedStudents;
    }

    public int getGraduatedClasses() {
        return graduatedClasses;
    }

    public void setGraduatedClasses(int graduatedClasses) {
        this.graduatedClasses = graduatedClasses;
    }

    public int getGraduatedStudents() {
        return graduatedStudents;
    }

    public void setGraduatedStudents(int graduatedStudents) {
        this.graduatedStudents = graduatedStudents;
    }

    public boolean isPromotionSkipped() {
        return promotionSkipped;
    }

    public void setPromotionSkipped(boolean promotionSkipped) {
        this.promotionSkipped = promotionSkipped;
    }

    public boolean isGraduationSkipped() {
        return graduationSkipped;
    }

    public void setGraduationSkipped(boolean graduationSkipped) {
        this.graduationSkipped = graduationSkipped;
    }
}
