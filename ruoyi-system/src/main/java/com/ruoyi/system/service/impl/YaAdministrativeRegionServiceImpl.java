package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.constant.ReportConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.YaAdministrativeRegion;
import com.ruoyi.system.mapper.YaAdministrativeRegionMapper;
import com.ruoyi.system.service.IYaAdministrativeRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;


/**
 * @author: xiaodemos
 * @date: 2025-05-07 17:26
 * @description:
 */

@Slf4j
@Service
public class YaAdministrativeRegionServiceImpl extends
        ServiceImpl<YaAdministrativeRegionMapper, YaAdministrativeRegion>
        implements IYaAdministrativeRegionService {

    @Resource
    private YaAdministrativeRegionMapper administrativeRegionMapper;


    @Override
    public String getAreaGeomByName(String name) {
        return administrativeRegionMapper.getAreaGeomByName(name);
    }

    @Override
    public String getInterSectionArea(String outCir, String area) {
        return administrativeRegionMapper.getInterSectionArea(outCir, area);
    }

    /**
     * @param lon
     * @param lat
     * @author: xiaodemos
     * @date: 2025/5/7 18:24
     * @description: 判断一个给定的经纬度坐标是否落在雅安市的某个区县范围内，并返回该区县的名称；如果不在任何一个区县范围内，则返回“不在雅安市”。
     * @return:
     */
    @Override
    public String getPlotBelongCounty(String lon, String lat) {
        // 抛出异常
        if (StringUtils.isEmpty(lon) || StringUtils.isEmpty(lat)) {
            throw new RuntimeException(BaseConstants.PARAMETER_ERROR);
        }
        // 标志位
        String searchPointGeom = "POINT(" + lon + " " + lat + ")";
        // 遍历区县列表
        for (String region : ReportConstants.CITIES) {
            String flag = administrativeRegionMapper.getPlotBelongCounty(searchPointGeom, region);
            // 如果落在某个区县范围内，则返回该区县的名称
            if (flag.equals("t")) {
                return region;
            }
        }
        // 如果落在某个区县范围外，则返回“不在雅安市”
        return ReportConstants.NOT_IN_YA;
    }

    @Override
    public Double computeIntersectionRatio(String intersectionArea, String outCir) {
        return administrativeRegionMapper.computeIntersectionRatio(intersectionArea, outCir);
    }
}
