package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.exception.EqTriggerException;
import com.ruoyi.common.utils.Result;
import com.ruoyi.system.domain.EqList;
import com.ruoyi.system.domain.dto.EqInfoDTO;
import com.ruoyi.system.domain.dto.ReassessmentDTO;
import com.ruoyi.system.domain.dto.TriggerDTO;
import com.ruoyi.system.domain.params.EqParams;
import com.ruoyi.system.service.IEarthQuakeService;
import com.ruoyi.system.service.IEqListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author: xiaodemos
 * @date: 2025-04-18 11:35
 * @description: EqList控制类
 */

@Slf4j
@RestController
@Anonymous
@Api(tags = "历史地震控制类")
@RequestMapping("/api/open/eq")
public class EqListController {

    @Resource
    private IEqListService eqListService;


    @Anonymous
    @ApiOperation("启动地震接口")
    @PostMapping("/trigger")
    public Result trigger(@RequestBody TriggerDTO triggerDTO) {

        log.info("触发参数：{}", triggerDTO);
        try {
            EqParams trigger = eqListService.trigger(triggerDTO);
            // 触发成功
            return Result.success(trigger);
        } catch (EqTriggerException e) {
            e.printStackTrace();
            // 触发异常
            return Result.error(BaseConstants.TRIGGER_FILED);
        }
    }

    @Anonymous
    @ApiOperation("地震重新评估接口")
    @PostMapping("reassessment")
    public Result reassessment(@RequestBody ReassessmentDTO reassessmentDTO) {

        log.info("地震重新评估参数{}", reassessmentDTO);
        try {
            eqListService.reassessment(reassessmentDTO);
            // 重新评估成功
            return Result.success(BaseConstants.REASSESSMENT_SUCCESS);
        } catch (EqTriggerException e) {
            e.printStackTrace();
            // 重新评估异常
            return Result.error(BaseConstants.REASSESSMENT_FILED);
        }
    }

    @Anonymous
    @ApiOperation("获取最新的单场地震信息")
    @GetMapping("currently")
    public Result currently() {
        try {
            // 获取历史震表中最新地震信息
            EqInfoDTO currently = eqListService.currently();
            return Result.success(currently);
        } catch (EqTriggerException e) {
            e.printStackTrace();
            // 获取数据异常
            return Result.error("获取最新地震失败");
        }
    }

}
