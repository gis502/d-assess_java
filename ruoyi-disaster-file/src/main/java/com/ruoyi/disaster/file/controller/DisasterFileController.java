package com.ruoyi.disaster.file.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.disaster.file.domain.dto.DisasterInfoDTO;
import com.ruoyi.disaster.file.domain.vo.DisasterFileVO;
import com.ruoyi.disaster.file.service.IDisasterFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 灾害文件管理 Controller
 * 基于已有的 rain_list 和 xian_earthquake_list 表实现
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
@RestController
@RequestMapping("/disaster/file")
public class DisasterFileController extends BaseController {

    @Autowired
    private IDisasterFileService disasterFileService;

    /**
     * 查询灾害列表（暴雨和地震）
     * 
     * @param disasterType 灾害类型（rain:暴雨，earthquake:地震，null 或空：全部）
     * @return 灾害信息列表
     */
    @GetMapping("/disasterList")
    public AjaxResult selectDisasterList(@RequestParam(required = false) String disasterType) {
        List<DisasterInfoDTO> list = disasterFileService.selectDisasterList(disasterType);
        return success(list);
    }

    /**
     * 根据灾害 ID 和类型查询文件列表
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 文件列表
     */
    @GetMapping("/fileList")
    public AjaxResult selectFilesByDisasterId(
            @RequestParam String disasterId,
            @RequestParam String disasterType) {
        List<DisasterFileVO> list = disasterFileService.selectFilesByDisasterId(disasterId, disasterType);
        return success(list);
    }

    /**
     * 删除灾害记录（逻辑删除）
     * 只能删除一整场灾害的所有信息
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 结果
     */
    @DeleteMapping("/{disasterId}/{disasterType}")
    public AjaxResult deleteDisasterById(
            @PathVariable String disasterId,
            @PathVariable String disasterType) {
        int result = disasterFileService.deleteDisasterById(disasterId, disasterType);
        return result > 0 ? success("删除成功") : error("删除失败");
    }
}
