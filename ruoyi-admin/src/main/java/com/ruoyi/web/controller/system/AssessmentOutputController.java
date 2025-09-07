package com.ruoyi.web.controller.system;

import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.utils.Result;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;
import com.ruoyi.system.domain.dto.RainAssessmentOutputDTO;
import com.ruoyi.system.domain.params.EqParams;
import com.ruoyi.system.domain.params.RainParams;
import com.ruoyi.system.service.IAssessmentOutputService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: xiaodemos
 * @date: 2025-04-18 11:41
 * @description: 图件、报告、文件结果控制类
 */

@Slf4j
@RestController
@Api(tags = "产出结果控制类")
@RequestMapping("/api/open")
public class AssessmentOutputController {

    @Resource
    private IAssessmentOutputService assessmentOutputService;

    @ApiOperation("获取地震专题图接口")
    @PostMapping("/eq/getMap")
    public Result<List<AssessmentOutputDTO>> getMap(@RequestBody EqParams eqParams) {

        log.info("获取地震专题图参数{}", eqParams);
        try {
            // 返回图件集合
            return Result.success(assessmentOutputService.getMap(eqParams));
        } catch (Exception e) {
            e.printStackTrace();
            // 获取图件异常
            return Result.error(BaseConstants.THEMATIC_MAP_ERROR);
        }
    }

    @ApiOperation("获取地震专题图接口")
    @PostMapping("/rain/getMap")
    public Result<List<RainAssessmentOutputDTO>> getMap(@RequestBody RainParams eqParams) {

        log.info("获取地震专题图参数{}", eqParams);
        try {
            // 返回图件集合
            return Result.success(assessmentOutputService.getMap(eqParams));
        } catch (Exception e) {
            e.printStackTrace();
            // 获取图件异常
            return Result.error(BaseConstants.THEMATIC_MAP_ERROR);
        }
    }

}
