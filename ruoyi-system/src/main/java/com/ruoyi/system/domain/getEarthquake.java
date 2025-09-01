package com.ruoyi.system.domain;

import com.ruoyi.system.domain.dto.TriggerDTO;

public class getEarthquake {
    public EarthQuakeReportEntity getEarthquake(TriggerDTO triggerDTO) {
        EarthQuakeReportEntity reportEntity = new EarthQuakeReportEntity();
        /*
         *地震概况部分
         */
        reportEntity.setReportTime(triggerDTO.getEqTime());
        reportEntity.setEarthQuakeTime(triggerDTO.getEqTime());
        reportEntity.setEarthQuakePosition(triggerDTO.getEqAddr());
        reportEntity.setEarthQuakeLon(triggerDTO.getLongitude());
        reportEntity.setEarthQuakeLat(triggerDTO.getLatitude());
        reportEntity.setEarthQuakeMagnitude(triggerDTO.getMagnitude());
        reportEntity.setEarthQuakeDeath(triggerDTO.getEqDepth());
        /*
         * 风险评估部分
         */
        reportEntity.setEarthQuakeCountry(triggerDTO.getCountry());
        reportEntity.setEarthQuakePopulationDensity(triggerDTO.getDensityPop());
        reportEntity.setEarthQuakeIntensity(triggerDTO.getIntensity());
        reportEntity.setEarthQuakeDisasterArea(triggerDTO.getCircleArea());
        reportEntity.setEarthQuakeInfluencePopulation(triggerDTO.getAffectPop());
        reportEntity.setEarthQuakeDeath(triggerDTO.getDiePop());
        reportEntity.setEarthQuakeFaultZone(triggerDTO.getFaultZone());
        return reportEntity;

    }
}
