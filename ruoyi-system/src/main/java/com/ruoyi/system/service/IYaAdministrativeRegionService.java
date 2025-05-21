package com.ruoyi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.YaAdministrativeRegion;
import org.apache.ibatis.annotations.Param;

public interface IYaAdministrativeRegionService extends IService<YaAdministrativeRegion> {

    // 通过名称获取该区域经纬度
    public String getAreaGeomByName(String name);
    // 获取两个区域的交集部分
    public String getInterSectionArea(String outCir, String area);
    // 根据经纬度查找其所属的县
    public String getPlotBelongCounty(String searchPointGeom, String county);
    // 计算交集区域与外圆面积的比例
    public Double computeIntersectionRatio(String intersectionArea, String outCir);
}
