package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.entity.ExpressInfo;
import com.express.system.service.IExpressInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 快递信息表 前端控制器
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/system/expressInfo")
public class ExpressInfoController {

    @Autowired
    private IExpressInfoService expressInfoService;

    @GetMapping("/list")
    public ApiResponse<List<ExpressInfo>> list() {
        return ApiResponse.success(expressInfoService.list());
    }

    @PostMapping("/checkin")
    public ApiResponse<ExpressInfo> checkIn(@RequestBody ExpressInfo expressInfo) {
        return ApiResponse.success("入库成功", expressInfoService.checkIn(expressInfo));
    }

    @PostMapping("/checkout")
    public ApiResponse<Boolean> checkOut(@RequestParam("code") String pickupCode) {
        expressInfoService.checkOut(pickupCode);
        return ApiResponse.success("核销成功", true);
    }
}
