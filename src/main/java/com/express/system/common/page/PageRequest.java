package com.express.system.common.page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "通用分页请求")
public class PageRequest {

    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "15")
    private Integer pageSize = 15;

    @Schema(description = "排序字段，格式: field:asc|desc", example = "updateTime:desc")
    private String sort;

    public long safePage() {
        if (page == null || page < 1) {
            return 1L;
        }
        return page;
    }

    public long safePageSize() {
        if (pageSize == null || pageSize < 1) {
            return 15L;
        }
        return Math.min(pageSize, 100);
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
