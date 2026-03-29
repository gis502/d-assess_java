package com.ruoyi.disaster.file.service.impl;

import com.ruoyi.disaster.file.domain.dto.DisasterInfoDTO;
import com.ruoyi.disaster.file.domain.vo.DisasterFileVO;
import com.ruoyi.disaster.file.mapper.DisasterFileMapper;
import com.ruoyi.disaster.file.service.IDisasterFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 灾害文件管理 Service 业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
@Service
public class DisasterFileServiceImpl implements IDisasterFileService {

    @Autowired
    private DisasterFileMapper disasterFileMapper;

    /**
     * 查询灾害列表（暴雨和地震）
     * 
     * @param disasterType 灾害类型（rain:暴雨，earthquake:地震，null 或空：全部）
     * @return 灾害信息列表
     */
    @Override
    public List<DisasterInfoDTO> selectDisasterList(String disasterType) {
        return disasterFileMapper.selectDisasterList(disasterType);
    }

    /**
     * 根据灾害 ID 和类型查询文件列表
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 文件列表
     */
    @Override
    public List<DisasterFileVO> selectFilesByDisasterId(String disasterId, String disasterType) {
        return disasterFileMapper.selectFilesByDisasterId(disasterId, disasterType);
    }

    /**
     * 删除灾害记录（逻辑删除）
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 结果
     */
    @Override
    public int deleteDisasterById(String disasterId, String disasterType) {
        return disasterFileMapper.deleteDisasterById(disasterId, disasterType);
    }
}
