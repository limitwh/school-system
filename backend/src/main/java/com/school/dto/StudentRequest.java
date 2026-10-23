package com.school.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 学生新建/编辑请求
 */
public class StudentRequest {

    @NotBlank(message = "请输入学生姓名")
    @Size(max = 50, message = "姓名不能超过 50 字")
    private String realName;

    @NotNull(message = "请选择班级")
    private Long classId;

    @NotNull(message = "请输入入学年份")
    @Min(value = 2000, message = "入学年份不合法")
    @Max(value = 2100, message = "入学年份不合法")
    private Integer enrollYear;

    /** 学号：可不填，按「入学年份 + 顺序号」自动生成 */
    @Size(max = 20, message = "学号不能超过 20 位")
    private String studentNo;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Integer getEnrollYear() {
        return enrollYear;
    }

    public void setEnrollYear(Integer enrollYear) {
        this.enrollYear = enrollYear;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
}
