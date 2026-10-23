package com.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 班级新建/编辑请求
 */
public class ClassRequest {

    @NotBlank(message = "请输入班级名称")
    @Size(max = 50, message = "班级名称不能超过 50 字")
    private String name;

    @NotNull(message = "请选择年级")
    private Long gradeId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }
}
