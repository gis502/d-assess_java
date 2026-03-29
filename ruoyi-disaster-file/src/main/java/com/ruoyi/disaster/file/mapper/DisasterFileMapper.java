package com.ruoyi.disaster.file.mapper;

import com.ruoyi.disaster.file.domain.dto.DisasterInfoDTO;
import com.ruoyi.disaster.file.domain.vo.DisasterFileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 灾害文件管理 Mapper 接口
 * 基于已有的 rain_list 和 xian_earthquake_list 表
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
@Mapper
public interface DisasterFileMapper {

    /**
     * 查询灾害列表（暴雨和地震）
     * 
     * @param disasterType 灾害类型（rain:暴雨，earthquake:地震，null 或空：全部）
     * @return 灾害信息列表
     */
    List<DisasterInfoDTO> selectDisasterList(@Param("disasterType") String disasterType);

    /**
     * 根据灾害 ID 和类型查询文件列表
     * 从 rain_list 或 xian_earthquake_list 表中获取
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 文件列表
     */
    List<DisasterFileVO> selectFilesByDisasterId(@Param("disasterId") String disasterId, 
                                                  @Param("disasterType") String disasterType);

    /**
     * 删除灾害记录（逻辑删除）
     * 根据灾害类型更新对应表的 is_deleted 字段
     * 
     * @param disasterId 灾害 ID
     * @param disasterType 灾害类型
     * @return 结果
     */
    int deleteDisasterById(@Param("disasterId") String disasterId, 
                           @Param("disasterType") String disasterType);
}
