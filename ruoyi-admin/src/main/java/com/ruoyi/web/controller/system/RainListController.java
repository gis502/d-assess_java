package com.ruoyi.web.controller.system;

import com.ruoyi.common.utils.Result;
import com.ruoyi.system.domain.dto.RainTriggerDTO;
import com.ruoyi.system.service.IRainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: xiaodemos
 * @date: 2025-08-30 13:46
 * @description: 暴雨控制类
 */


@Slf4j
@RestController
@Api(tags = "暴雨控制类")
@RequestMapping("/api/open/rain")
public class RainListController {



    @Resource
    private IRainService rainService;


    @ApiOperation("启动暴雨接口")
    @PostMapping("trigger")
    public Result trigger(@RequestBody RainTriggerDTO triggerDTO){

        log.info("触发参数：{}", triggerDTO);
        return Result.success(rainService.trigger(triggerDTO));
    }

}
