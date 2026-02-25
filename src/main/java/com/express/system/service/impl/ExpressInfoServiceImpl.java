package com.express.system.service.impl;

import com.express.system.entity.ExpressInfo;
import com.express.system.mapper.ExpressInfoMapper;
import com.express.system.service.IExpressInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 快递信息表 服务实现类
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@Service
public class ExpressInfoServiceImpl extends ServiceImpl<ExpressInfoMapper, ExpressInfo> implements IExpressInfoService {

}
