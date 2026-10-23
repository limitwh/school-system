package com.school.dto;

/**
 * 年级信息
 */
public class GradeResponse {

    private Long id;
    private String name;
    private String stage;
    private Integer sortOrder;
    private Integer enabled;

    public GradeResponse() {
    }

    public GradeResponse(Long id, String name, String stage, Integer sortOrder, Integer enabled) {
        this.id = id;
        this.name = name;
        this.stage = stage;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
