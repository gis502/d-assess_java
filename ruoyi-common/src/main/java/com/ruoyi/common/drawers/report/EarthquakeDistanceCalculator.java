package com.ruoyi.common.drawers.report;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 9:19
 * @description: 地震距离计算
 */

public class EarthquakeDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6371004;

    /**
     * @param latitude1  各A，B，C，D 控制点的纬度
     * @param longitude1 各A，B，C，D 控制点的经度
     * @param latitude2  新建地震的纬度
     * @param longitude2 新建地震的经度
     * @author: xiaodemos
     * @date: 2025/5/9 9:31
     * @description: 计算 QX 的函数
     * @return: 返回距离
     */
    public static double calculateQX(double latitude1, double longitude1, double latitude2, double longitude2) {
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(90 - latitude1);
        double lon1Rad = Math.toRadians(longitude1);
        double lat2Rad = Math.toRadians(90 - latitude2);
        double lon2Rad = Math.toRadians(longitude2);

        // 计算球面距离公式
        double part1 = Math.sin(lat1Rad) * Math.cos(lon1Rad) - Math.sin(lat2Rad) * Math.cos(lon2Rad);
        double part2 = Math.sin(lat1Rad) * Math.sin(lon1Rad) - Math.sin(lat2Rad) * Math.sin(lon2Rad);
        double part3 = Math.cos(lat1Rad) - Math.cos(lat2Rad);

        double distance = (6371004 * Math.acos(1 - (Math.pow(part1, 2) + Math.pow(part2, 2) + Math.pow(part3, 2)) / 2)) / 1000; // 返回千米

        return distance;
    }

    // 计算公式 (D5 + D6 + C10) / 2
    public static double calculateQAB(double QA, double QB, double AB) {
        return (QA + QB + AB) / 2;
    }

    // 计算公式 (D6 + D7 + C11) / 2
    public static double calculateQBC(double QB, double QC, double BC) {
        return (QB + QC + BC) / 2;
    }

    // 计算公式 (D7 + D8 + C12) / 2
    public static double calculateQCD(double QC, double QD, double CD) {
        return (QC + QD + CD) / 2;
    }

    // 计算公式 (D5 + D8 + C13) / 2
    public static double calculateQDA(double QA, double QD, double DA) {
        return (QA + QD + DA) / 2;
    }

    public static double calculateArea(double QAB, double QBC, double QCD, double QAD,
                                       double QA, double QB, double QC, double QD,
                                       double AB, double BC, double CD, double DA) {

        double part1 = Math.sqrt(QAB * (QAB - QA) * (QAB - QB) * (QAB - AB));
        double part2 = Math.sqrt(QBC * (QBC - QB) * (QBC - QC) * (QBC - BC));
        double part3 = Math.sqrt(QCD * (QCD - QC) * (QCD - QD) * (QCD - CD));
        double part4 = Math.sqrt(QAD * (QAD - QA) * (QAD - QD) * (QAD - DA));

        return part1 + part2 + part3 + part4;
    }

    // 计算地理距离的方法（单位：公里）
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 地球半径，单位：公里
        final double R = 6371;
        // 将经纬度从度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        // 纬度和经度的差值
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离（单位：公里）
        return R * c;

    }

}
