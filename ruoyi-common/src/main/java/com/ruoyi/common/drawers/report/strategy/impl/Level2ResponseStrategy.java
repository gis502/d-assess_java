package com.ruoyi.common.drawers.report.strategy.impl;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 10:15
 * @description: 市级地震灾害二级应急响应策略
 */

@AllArgsConstructor
@NoArgsConstructor
public class Level2ResponseStrategy implements ResponseStrategy {

    private TrafficRequest request;


    @Override
    public String generateResponse(String cityOrState, String countyOrDistrict, String earthquakeName) {
        return "市政府常务副市长和市政府负责防震减灾工作副市长组织召开抗震救灾紧急会议；成立雅安市" + earthquakeName +
                "抗震救灾指挥部，建立市政府市长任总指挥，市政府常务副市长和市政府负责防震减灾工作副市长任指挥长的指挥体系；立即向省委、省政府和省抗震救灾指挥机构报告震情、灾情和应急处置情况，并持续报告工作进展；组建市应对地震灾害指挥部后方协调中心";
    }

    @Override
    public String generatePlan(String cityOrState, Double eqMagnitude, String neighboringCityOrState) {
        return "迅速组织抢险救援队伍、医疗救护队伍、相应技术人员赶赴灾区开展救援抢险";
    }

    @Override
    public String generateTraffic() {
        return "迅速对通往" + request.getCountyOrDistrict() + "的国省干道进行必要的交通管制，保障救援车辆、机械和人员优先通行，"
                + "其他救灾车辆和人员调节通行，根据需要启用直升机起降场地";
    }

}
