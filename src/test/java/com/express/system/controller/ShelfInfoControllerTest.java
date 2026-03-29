package com.express.system.controller;

import com.express.system.common.page.PageResponse;
import com.express.system.entity.ShelfInfo;
import com.express.system.service.IShelfInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShelfInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ShelfInfoControllerTest.TestApplication.class)
class ShelfInfoControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(excludeName = {
            "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
            "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    })
    @Import(ShelfInfoController.class)
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IShelfInfoService shelfInfoService;

    @Test
    @WithMockUser(roles = "STAFF")
    void listReturnsShelves() throws Exception {
        // 货架列表接口：STAFF 查询返回列表。
        ShelfInfo shelf = new ShelfInfo();
        shelf.setId(1L);
        shelf.setShelfCode(101);
        shelf.setShelfLayer(1);
        when(shelfInfoService.pageByFilter(eq(null), eq(null), eq(null), eq(null), any()))
                .thenReturn(PageResponse.of(List.of(shelf), 1, 1, 15));

        mockMvc.perform(get("/system/shelves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].shelfCode").value(101));
    }
}
