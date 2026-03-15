package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.entity.ShelfInfo;
import com.express.system.service.IShelfInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/system/shelfInfo")
public class ShelfInfoController {

    @Autowired
    private IShelfInfoService shelfInfoService;

    @GetMapping("/list")
    public ApiResponse<List<ShelfInfo>> list(
            @RequestParam(value = "shelfType", required = false) Integer shelfType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "shelfCode", required = false) Integer shelfCode,
            @RequestParam(value = "shelfLayer", required = false) Integer shelfLayer) {
        return ApiResponse.success(shelfInfoService.listByFilter(
                shelfType, status, shelfCode, shelfLayer));
    }

    @GetMapping("/detail")
    public ApiResponse<ShelfInfo> detail(@RequestParam("id") Long id) {
        return ApiResponse.success(shelfInfoService.getDetail(id));
    }

    @GetMapping("/byCodeLayer")
    public ApiResponse<ShelfInfo> byCodeLayer(@RequestParam("shelfCode") Integer shelfCode,
                                              @RequestParam("shelfLayer") Integer shelfLayer) {
        ShelfInfo shelf = shelfInfoService.getByCodeAndLayer(shelfCode, shelfLayer);
        if (shelf == null) {
            throw new RuntimeException("货架不存在");
        }
        return ApiResponse.success(shelf);
    }

    @GetMapping("/recommend")
    public ApiResponse<ShelfInfo> recommend(@RequestParam("sizeType") Integer sizeType) {
        ShelfInfo shelf = shelfInfoService.getRecommendShelf(sizeType);
        if (shelf == null) {
            throw new RuntimeException("没有可用货架");
        }
        return ApiResponse.success(shelf);
    }

    @PostMapping("/create")
    public ApiResponse<ShelfInfo> create(@RequestBody ShelfInfo shelfInfo) {
        return ApiResponse.success("新增成功", shelfInfoService.createShelf(shelfInfo));
    }

    @PostMapping("/update")
    public ApiResponse<ShelfInfo> update(@RequestBody ShelfInfo shelfInfo) {
        return ApiResponse.success("更新成功", shelfInfoService.updateShelf(shelfInfo));
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> delete(@RequestParam("id") Long id) {
        return ApiResponse.success("删除成功", shelfInfoService.deleteShelf(id));
    }
}
