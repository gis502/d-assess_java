package com.ruoyi.disaster.file.service;

import com.ruoyi.disaster.file.domain.dto.DisasterInfoDTO;
import com.ruoyi.disaster.file.domain.vo.DisasterFileVO;

import java.util.List;

/**
 * 灾害文件管理 Service 接口
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
public interface IDisasterFileService {

    /**
     * 查询灾害列表（暴雨和地震）
     * 
     * @param disasterType 灾害类型（rain:暴雨，earthquake:地震，null 或空：全部）
     * @return 灾害信息列表
     */
    List<DisasterInfoDTO> selectDisasterList(String disasterType);

    /**
     * 根据灾害 ID 和类型查询文件列表
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 文件列表
     */
    List<DisasterFileVO> selectFilesByDisasterId(String disasterId, String disasterType);

    /**
     * 删除灾害记录（逻辑删除）
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 结果
     */
    int deleteDisasterById(String disasterId, String disasterType);
}
