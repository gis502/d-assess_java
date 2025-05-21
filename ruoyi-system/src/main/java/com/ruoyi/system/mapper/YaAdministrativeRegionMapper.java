package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.YaAdministrativeRegion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface YaAdministrativeRegionMapper extends BaseMapper<YaAdministrativeRegion> {

    // 通过名称获取该区域经纬度
    @Select("SELECT ST_AsText(geom) FROM yaan_json WHERE name = #{name}")
    public String getAreaGeomByName(String name);
    // 获取两个区域的交集部分
    @Select("SELECT ST_AsText(ST_Intersection(ST_GeomFromText(#{Outcir}, 4326), ST_GeomFromText(#{Area}) ))")
    public String getInterSectionArea(String outCir, String area);
    // 根据经纬度查找其所属的县
    @Select("SELECT ST_Contains((SELECT geom FROM yaan_json where name = #{county}),st_geometryfromtext( #{searchPointGeom}))as judge;")
    public String getPlotBelongCounty(String searchPointGeom, String county);
    // 计算交集区域与外圆面积的比例
    @Select("SELECT area1.area1_val / area2.area2_val AS ratio " +
            "FROM (SELECT ST_Area(ST_Transform(ST_GeomFromText(#{intersectionArea}))) AS area1_val) AS area1, " +
            "(SELECT ST_Area(ST_Transform(ST_GeomFromText(#{Outcir}))) AS area2_val) AS area2")
    public Double computeIntersectionRatio(String intersectionArea, String outCir);

}
