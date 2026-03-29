package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.common.exception.BusinessException;
import com.express.system.common.page.PageResponse;
import com.express.system.dto.ShelfLoadVO;
import com.express.system.dto.query.ShelfPageQuery;
import com.express.system.entity.ShelfInfo;
import com.express.system.service.IShelfInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 货架信息表 前端控制器
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/system/shelves")
@Tag(name = "货架管理")
public class ShelfInfoController {

    private final IShelfInfoService shelfInfoService;

    public ShelfInfoController(IShelfInfoService shelfInfoService) {
        this.shelfInfoService = shelfInfoService;
    }

    @Operation(summary = "货架列表查询")
    @GetMapping
    public ApiResponse<PageResponse<ShelfInfo>> list(ShelfPageQuery query) {
        if (query == null) {
            query = new ShelfPageQuery();
        }
        return ApiResponse.success(shelfInfoService.pageByFilter(
                query.getShelfType(), query.getStatus(), query.getShelfCode(), query.getShelfLayer(), query));
    }

    @Operation(summary = "货架负载查询")
    @GetMapping("/load")
    public ApiResponse<List<ShelfLoadVO>> load(
            @Parameter(description = "货架类型") @RequestParam(value = "shelfType", required = false) Integer shelfType,
            @Parameter(description = "货架状态") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "货架编号") @RequestParam(value = "shelfCode", required = false) Integer shelfCode,
            @Parameter(description = "货架层数") @RequestParam(value = "shelfLayer", required = false) Integer shelfLayer) {
        return ApiResponse.success(shelfInfoService.listLoadByFilter(
                shelfType, status, shelfCode, shelfLayer));
    }

    @Operation(summary = "货架详情")
    @GetMapping("/{id}")
    public ApiResponse<ShelfInfo> detail(@Parameter(description = "货架ID") @PathVariable("id") Long id) {
        return ApiResponse.success(shelfInfoService.getDetail(id));
    }

    @Operation(summary = "按编号+层数查货架")
    @GetMapping("/lookup")
    public ApiResponse<ShelfInfo> byCodeLayer(@Parameter(description = "货架编号") @RequestParam("shelfCode") Integer shelfCode,
                                              @Parameter(description = "货架层数") @RequestParam("shelfLayer") Integer shelfLayer) {
        ShelfInfo shelf = shelfInfoService.getByCodeAndLayer(shelfCode, shelfLayer);
        if (shelf == null) {
            throw BusinessException.badRequest("货架不存在");
        }
        return ApiResponse.success(shelf);
    }

    @Operation(summary = "推荐可用货架")
    @GetMapping("/recommend")
    public ApiResponse<ShelfInfo> recommend(@Parameter(description = "快递尺寸类型") @RequestParam("sizeType") Integer sizeType) {
        ShelfInfo shelf = shelfInfoService.getRecommendShelf(sizeType);
        if (shelf == null) {
            throw BusinessException.badRequest("没有可用货架");
        }
        return ApiResponse.success(shelf);
    }

    @Operation(summary = "新增货架")
    @PostMapping
    public ApiResponse<ShelfInfo> create(@RequestBody ShelfInfo shelfInfo) {
        return ApiResponse.success("新增成功", shelfInfoService.createShelf(shelfInfo));
    }

    @Operation(summary = "更新货架")
    @PutMapping("/{id}")
    public ApiResponse<ShelfInfo> update(@Parameter(description = "货架ID") @PathVariable("id") Long id,
                                         @RequestBody ShelfInfo shelfInfo) {
        shelfInfo.setId(id);
        return ApiResponse.success("更新成功", shelfInfoService.updateShelf(shelfInfo));
    }

    @Operation(summary = "删除货架")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@Parameter(description = "货架ID") @PathVariable("id") Long id) {
        return ApiResponse.success("删除成功", shelfInfoService.deleteShelf(id));
    }
}
