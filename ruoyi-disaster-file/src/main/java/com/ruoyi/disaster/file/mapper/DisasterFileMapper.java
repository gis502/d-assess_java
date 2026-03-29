package com.ruoyi.disaster.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.disaster.file.domain.DisasterFile;
import com.ruoyi.disaster.file.domain.dto.DisasterInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 灾害文件管理 Mapper 接口
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
@Mapper
public interface DisasterFileMapper extends BaseMapper<DisasterFile> {

    /**
     * 查询灾害列表（暴雨和地震）
     * 
     * @param disasterType 灾害类型（rain:暴雨，earthquake:地震，null 或空：全部）
     * @return 灾害信息列表
     */
    List<DisasterInfoDTO> selectDisasterList(@Param("disasterType") String disasterType);

    /**
     * 根据灾害 ID 和类型查询文件列表
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 文件列表
     */
    List<DisasterFile> selectFilesByDisasterId(@Param("disasterId") String disasterId, 
                                                @Param("disasterType") String disasterType);

    /**
     * 批量删除灾害文件（逻辑删除）
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 结果
     */
    int deleteFilesByDisasterId(@Param("disasterId") String disasterId, 
                                 @Param("disasterType") String disasterType);
}
