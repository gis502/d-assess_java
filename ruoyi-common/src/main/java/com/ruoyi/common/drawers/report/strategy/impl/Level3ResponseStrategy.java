package com.ruoyi.common.drawers.report.strategy.impl;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 10:13
 * @description: 市级地震灾害三级应急响应策略
 */

@AllArgsConstructor
@NoArgsConstructor
public class Level3ResponseStrategy implements ResponseStrategy {

    private TrafficRequest request;


    // 应急响应策略
    @Override
    public String generateResponse(String cityOrState, String countyOrDistrict, String earthquakeName) {
        if ("雅安市".equals(cityOrState)) {
            return createYaAnLevel3Response(countyOrDistrict);
        }
        return createNonYaAnLevel3Response();
    }

    //应急救援计划策略
    @Override
    public String generatePlan(String cityOrState, Double eqMagnitude, String neighboringCityOrState) {

        if ("雅安市".equals(cityOrState)) {

            return "视情况组织抢险救援队伍、医疗救护队伍、相应技术人员赶赴灾区开展救援抢险";

        } else if (eqMagnitude < 6.0) {

            return "根据情况组织抢险救援队伍、医疗救护队伍、相应技术人员赶赴我市灾区开展救援抢险";

        } else {

            return "根据情况组织抢险救援队伍、医疗救护队伍、相应技术人员赶赴我市灾区开展救援抢险，"
                    + "迅速联系" + neighboringCityOrState + "和省抗震救灾指挥部，根据需要和我市震情灾情组织必要的抢险救援队伍、"
                    + "医疗救护队伍、相应技术人员和物资对灾区进行支援（我市灾情未完全调查清楚前，至少保留2/3以上队伍和物资）";
        }
    }

    @Override
    public String generateTraffic() {
        return "保障通往震中区域的国省干道畅通，确保救援车辆和物资顺利到达震中";
    }

    private String createYaAnLevel3Response(String countyOrDistrict) {
        return "市政府负责防震减灾工作副市长组织召开抗震救灾紧急会议；根据工作需要，请市政府指派一位市领导带领市应急管理局、市委宣传部、市卫生健康委、市自然资源和规划局、市水利局、市防震减灾服务中心等部门前往" + countyOrDistrict +
                "，会同省应急厅、省地震局专家指导、协调、督促抗震救灾工作；立即向省委、省政府和省抗震救灾指挥机构报告震情、灾情和应急处置情况，并持续报告工作进展";
    }

    private String createNonYaAnLevel3Response() {
        return "市政府负责防震减灾工作副市长组织召开抗震救灾紧急会议；根据工作需要，请市政府指派一位市领导带领市应急管理局、市委宣传部、市卫生健康委、市自然资源和规划局、市水利局、市防震减灾服务中心等部门前往我市受灾较重的县（区），会同省应急厅、省地震局专家指导、协调、督促抗震救灾工作；立即向省委、省政府和省抗震救灾指挥机构报告震情、灾情和应急处置情况，并持续报告工作进展";
    }


}
