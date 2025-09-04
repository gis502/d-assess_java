package com.ruoyi.common.drawers.map;

import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.constant.Constants;
import com.supermap.data.*;
import com.supermap.mapping.LayerSettingVector;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author: xiaodemos
 * @date: 2025-04-02 2:29
 * @description: 地图绘制类
 */

@Component
public class MapDrawer {

    /**
     * @param workspace 工作空间
     * @param pointName 点数据集名称
     * @param center    震中心点
     * @author: xiaodemos
     * @date: 2025/3/22 13:46
     * @description: 创建灾害中心数据点数据集
     * @return: 返回一个自定义图标的震中数据点对象
     */
    public DatasetVector createCenterPoint(Workspace workspace, String datasetsName, String pointName, Point2D center) {

        // 获取数据源
        Datasource datasource = workspace.getDatasources().get(datasetsName);
        Datasets datasets = datasource.getDatasets();
        DatasetVectorInfo vectorInfo = new DatasetVectorInfo();
        // 设置数据集名称
        vectorInfo.setName(pointName);
        // 设置为点数据集
        vectorInfo.setType(DatasetType.POINT);
        // 创建点数据集
        DatasetVector datasetVector = datasets.create(vectorInfo);

        // 设置经纬度
        GeoPoint geoPoint = new GeoPoint(center);

        Recordset recordset = datasetVector.getRecordset(false, CursorType.DYNAMIC);

        recordset.addNew(geoPoint);
        recordset.update();
        recordset.close();

        return datasetVector;
    }

    /**
     * @param workspace     工作空间
     * @param intensityName 烈度圈数据名称
     * @param center        震中位置
     * @param magnitude     震级
     * @author: xiaodemos
     * @date: 2025/3/25 14:57
     * @description: 绘制烈度圈与烈度圈文本数据
     * @return: 返回一个烈度圈数据集
     */
    public DatasetVector createIntensity(Workspace workspace, String datasetName, String intensityName, Point2D center, double magnitude, int rotation) {
        // 获取数据源
        Datasource datasource = workspace.getDatasources().get(datasetName);
        Datasets datasets = datasource.getDatasets();
        DatasetVectorInfo vectorInfo = new DatasetVectorInfo();
        // 设置数据集名称
        vectorInfo.setName(intensityName);
        // 设置为椭圆数据集
        vectorInfo.setType(DatasetType.CAD);
        // 创建数据集
        DatasetVector datasetVector = datasets.create(vectorInfo);

        GeoCompound intensityGeometry = getIntensityGeometry(center, magnitude, rotation);

        Recordset recordset = datasetVector.getRecordset(false, CursorType.DYNAMIC);

        recordset.addNew(intensityGeometry);
        recordset.update();

        recordset.close();

        return datasetVector;
    }

    /**
     * @param workspace     工作空间
     * @param datasetName   数据集名称
     * @param intensityName 烈度圈名称
     * @param center        震中位置
     * @param magnitude     震级
     * @param rotation      烈度圈旋转角度
     * @author: xiaodemos
     * @date: 2025/4/10 20:04
     * @description: 创建烈度圈数据集
     * @return: 返回数据集
     */
    public DatasetVector createIntensityAffectedArea(Workspace workspace, String datasetName, String intensityName, Point2D center, double magnitude, int rotation) {

        // 获取数据源
        Datasource datasource = workspace.getDatasources().get(datasetName);
        Datasets datasets = datasource.getDatasets();
        DatasetVectorInfo vectorInfo = new DatasetVectorInfo();
        // 设置数据集名称
        vectorInfo.setName(intensityName);
        // 设置为椭圆数据集
        vectorInfo.setType(DatasetType.CAD);
        // 创建数据集
        DatasetVector datasetVector = datasets.create(vectorInfo);

        GeoCompound intensityAffectedGeometry = getIntensityAffectedGeometry(center, magnitude, rotation);

        Recordset recordset = datasetVector.getRecordset(false, CursorType.DYNAMIC);
        recordset.addNew(intensityAffectedGeometry);
        recordset.update();

        recordset.close();

        return datasetVector;

    }

    /**
     * @param workspace     工作空间
     * @param intensityName 烈度圈数据名称
     * @param center        震中位置
     * @param magnitude     震级
     * @author: xiaodemos
     * @date: 2025/4/2 2:57
     * @description: 绘制烈度圈文本数据源
     * @return: 返回一个烈度圈数据集
     */
    public DatasetVector createIntensityTextInfo(Workspace workspace, String intensityName, Point2D center, double magnitude) {
        // 获取数据源
        Datasource datasource = workspace.getDatasources().get(BaseConstants.XI_AN_SEISMIC_DATASETS_NAME);
        Datasets datasets = datasource.getDatasets();
        DatasetVectorInfo vectorInfo = new DatasetVectorInfo();
        // 设置数据集名称
        vectorInfo.setName(intensityName);
        // 设置为椭圆数据集
        vectorInfo.setType(DatasetType.CAD);
        // 创建数据集
        DatasetVector datasetVector = datasets.create(vectorInfo);

        GeoCompound intensityGeometry = getIntensityTextInfo(center, magnitude);

        Recordset recordset = datasetVector.getRecordset(false, CursorType.DYNAMIC);
        recordset.addNew(intensityGeometry);
        recordset.update();

        recordset.close();

        return datasetVector;
    }

    /**
     * @param center    震中位置
     * @param magnitude 震级
     * @author: xiaodemos
     * @date: 2025/4/2 2:34
     * @description: 绘制烈度圈文本信息
     * @return: 返回几何复合文本信息对象
     */
    public GeoCompound getIntensityTextInfo(Point2D center, double magnitude) {
        // 获取烈度值
        int[] intensityLevels = gainIntensityLevels(magnitude);

        // 创建复合几何对象
        GeoCompound geoCompound = new GeoCompound();

        // 添加文本标注
        for (int i = 0; i < intensityLevels.length; i++) {
            int intensity = intensityLevels[i];
            double Rb = calculateRb(magnitude, intensity);
            Point2D textPosition = new Point2D(center.getX(), center.getY() + Rb / 1.5);

            TextPart textPart = new TextPart(BaseConstants.SEISMIC_INTENSITY_MAPPING.get(intensity) + "(" + intensity + "度)", textPosition);
            GeoText geoText = new GeoText(textPart);

            TextStyle textStyle = new TextStyle();
            textStyle.setForeColor(new Color(255, 0, 0));
            textStyle.setFontName("Arial");
            textStyle.setFontHeight(3.1);
            geoText.setTextStyle(textStyle);

            geoCompound.addPart(geoText);

        }

        return geoCompound;
    }


    /**
     * @param center    震中位置
     * @param magnitude 震级
     * @param rotation  烈度圈旋转角度
     * @author: xiaodemos
     * @date: 2025/3/25 11:29
     * @description: 创建线状的烈度圈
     * @return: 返回一个三层的椭圆复合对象
     */
    public GeoCompound getIntensityGeometry(Point2D center, double magnitude, int rotation) {
        // 获取烈度值
        int[] intensityLevels = gainIntensityLevels(magnitude);

        // 创建复合几何对象
        GeoCompound geoCompound = new GeoCompound();

        // 添加烈度圈（椭圆）
        for (int intensity : intensityLevels) {

            GeoEllipse ellipse = getGeoEllipse(center, magnitude, intensity, rotation);
            ellipse.setCenter(center);
            geoCompound.addPart(ellipse);
        }

        return geoCompound;
    }

    /**
     * @param center    震中位置
     * @param magnitude 震级
     * @param rotation  烈度圈旋转角度
     * @author: xiaodemos
     * @date: 2025/3/25 11:29
     * @description: 创建带填充颜色的烈度圈
     * @return: 返回一个三层的椭圆复合对象
     */
    public GeoCompound getIntensityAffectedGeometry(Point2D center, double magnitude, int rotation) {
        // 获取烈度值
        int[] intensityLevels = gainIntensityLevels(magnitude);

        // 创建复合几何对象
        GeoCompound geoCompound = new GeoCompound();

        // 添加烈度圈（椭圆）
        for (int intensity : intensityLevels) {

            GeoEllipse ellipse = getAffectedArea(center, magnitude, intensity, rotation);
            ellipse.setCenter(center);
            geoCompound.addPart(ellipse);
        }

        return geoCompound;
    }

    /**
     * 根据震级获取对应的烈度值数组
     *
     * @param magnitude 震级
     * @return 对应烈度值数组
     */
    private int[] gainIntensityLevels(double magnitude) {
        // 对震级进行四舍五入处理，取整数级别
        int magLevel = (int) Math.round(magnitude);

        // 根据震级范围返回对应的烈度值数组
        if (magLevel == 6) {
            return new int[]{6, 7, 8};
        } else if (magLevel == 7) {
            return new int[]{7, 8, 9};
        } else if (magLevel == 8) {
            return new int[]{8, 9, 10};
        } else if (magLevel >= 9) {
            // 9级及以上都返回9-12级烈度
            return new int[]{9, 10, 11, 12};
        } else {
            // 对于6级以下的震级，默认返回低烈度值
            return new int[]{6};
        }
    }

    /**
     * @param center    烈度圈中心
     * @param magnitude 震级
     * @param intensity 烈度
     * @author: xiaodemos
     * @date: 2025/3/25 11:00
     * @description: 根据震级创建一个新的椭圆烈度
     * @return: 返回椭圆
     */
    private GeoEllipse getGeoEllipse(Point2D center, double magnitude, int intensity, int rotation) {

        // 计算原始半径（km）
        double Ra = calculateRa(magnitude, intensity);
        double Rb = calculateRb(magnitude, intensity);

        System.out.println("长轴：" + Ra + "\n短轴：" + Rb);


        GeoEllipse ellipse = new GeoEllipse(center, Ra, Rb, rotation);

        // 设置椭圆样式
        GeoStyle ellipseStyle = new GeoStyle();
        ellipseStyle.setFillForeColor(new Color(255, 0, 0)); // 填充红色
        ellipseStyle.setFillOpaqueRate(0); // 设置透明度
        ellipseStyle.setLineColor(Color.RED); // 边框颜色
        ellipseStyle.setLineWidth(0.2); // 线宽

        ellipse.setStyle(ellipseStyle);
        return ellipse;
    }


    /**
     * @param center    震中位置
     * @param magnitude 震级
     * @param intensity 烈度值
     * @param rotation  旋转角度
     * @author: xiaodemos
     * @date: 2025/4/10 19:48
     * @description: 根据震级创建一个带填充颜色的椭圆烈度
     * @return: 返回一个带填充颜色的椭圆
     */
    public GeoEllipse getAffectedArea(Point2D center, double magnitude, int intensity, int rotation) {

        double Ra = calculateRa(magnitude, intensity);
        double Rb = calculateRb(magnitude, intensity);

        // 创建椭圆
        GeoEllipse ellipse = new GeoEllipse(center, Ra, Rb, rotation);

        // 根据烈度值设置椭圆的填充颜色渐变
        Color fillColor = getFillColorBasedOnIntensity(intensity);

        // 设置椭圆样式
        GeoStyle ellipseStyle = new GeoStyle();
        ellipseStyle.setFillForeColor(fillColor); // 设置渐变填充颜色
        ellipseStyle.setFillOpaqueRate(80); // 设置透明度，透明度越低颜色越鲜明
        ellipseStyle.setLineColor(Color.LIGHT_GRAY); // 边框颜色
        ellipseStyle.setLineWidth(0.1); // 设置边框宽度

        // 将风格应用到椭圆上
        ellipse.setStyle(ellipseStyle);
        return ellipse;
    }


    /**
     * @param intensity
     * @author: xiaodemos
     * @date: 2025/4/10 19:44
     * @description: 根据不同的烈度值设置烈度圈颜色
     * 各烈度颜色定义：
     * Ⅵ rgba(231,211,223,100%)
     * Ⅶ rgba(230, 199, 207, 100%)
     * Ⅷ rgba(230, 163, 170, 100%)
     * Ⅸ rgba(187, 100, 109, 100%)
     * Ⅹ rgba(163, 96, 103, 100%)
     * Ⅺ rgba(115, 96, 102, 100%)
     * Ⅻ rgba(113, 126, 134, 100%)
     * @return: 返回RGB颜色
     */
    private Color getFillColorBasedOnIntensity(int intensity) {
        // 定义颜色值
        Color color;

        switch (intensity) {
            case 6:
                // Ⅵ级（低烈度）：浅红色，视觉最浅，对应外围影响区
                color = new Color(255, 167, 167, 126);
                break;
            case 7:
                // Ⅶ级：中度红色，比6级深，代表轻度破坏区
                color = new Color(255, 82, 82, 126);
                break;
            case 8:
                // Ⅷ级：标准深红色，开始向深色过渡，代表中度破坏区
                color = new Color(220, 20, 60, 126);
                break;
            case 9:
                // Ⅸ级：暗红黑色，融入少量黑色调，代表严重破坏区
                color = new Color(185, 10, 45, 126);
                break;
            case 10:
                // Ⅹ级：深黑红色，黑色感增强，代表重大破坏区
                color = new Color(145, 5, 30, 126);
                break;
            case 11:
                // Ⅺ级：近黑色，红色仅残留底色，代表毁灭性破坏区
                color = new Color(105, 3, 20, 126);
                break;
            case 12:
                // Ⅻ级（最高烈度）：黑红色，视觉最深沉，对应核心极重破坏区
                color = new Color(65, 2, 12, 126);
                break;
            default:
                // 如果烈度值超出范围，可以使用一个默认颜色或者根据你的需要返回相应的颜色
                int redIntensity = 255; // 红色最大值
                int greenIntensity = Math.max(0, 255 - intensity * 40); // 根据烈度调节绿色强度
                int blueIntensity = Math.max(0, 255 - intensity * 80); // 根据烈度调节蓝色强度
                // 根据不同的烈度等级计算颜色，烈度越高，红色越深
                color = new Color(redIntensity, greenIntensity, blueIntensity);
                break;
        }

        return color;

    }

    /**
     * @author: xiaodemos
     * @date: 2025/3/22 13:46
     * @description: 绘制震中点样式
     * @return: 返回一个图层样式对象
     */
    public LayerSettingVector drawerCenterPointStyle() {

        // 设置震中点图层样式
        GeoStyle centerStyle = new GeoStyle();
        centerStyle.setMarkerSize(new Size2D(15, 15)); // 设置点图标大小
        centerStyle.setMarkerSymbolID(922591); // 设置点图标样式（ID为922591的符号）
        centerStyle.setLineColor(new Color(255, 0, 0)); // 设置点图标边框颜色为红色
        centerStyle.setFillForeColor(new Color(255, 0, 0)); // 设置点图标填充颜色为番茄色

        // 设置图层样式
        LayerSettingVector vector = new LayerSettingVector();
        vector.setStyle(centerStyle);

        return vector;
    }

    // 绘制暴雨中心点位置
    public LayerSettingVector drawerRainCenterPointStyle() {

        // 设置震中点图层样式
        GeoStyle centerStyle = new GeoStyle();
        centerStyle.setMarkerSize(new Size2D(15, 15)); // 设置点图标大小
        centerStyle.setMarkerSymbolID(922591); // 设置点图标样式（ID为922591的符号）
        centerStyle.setLineColor(new Color(255, 0, 0)); // 设置点图标边框颜色为红色
        centerStyle.setFillForeColor(new Color(255, 0, 0)); // 设置点图标填充颜色为番茄色

        // 设置图层样式
        LayerSettingVector vector = new LayerSettingVector();
        vector.setStyle(centerStyle);

        return vector;
    }

    /**
     * @param M  震级
     * @param Ia 烈度
     * @author: xiaodemos
     * @date: 2025/3/25 10:57
     * @description: 计算椭圆的长轴
     * @return: 返回长轴
     */
    private double calculateRa(double M, double Ia) {
        return (Math.pow(10, (4.0293 + 1.3003 * M - Ia) / 3.6404) - 10) / 50;
    }


    /**
     * @param M  震级
     * @param Ib 烈度
     * @author: xiaodemos
     * @date: 2025/3/25 10:58
     * @description: 计算椭圆的短轴
     * @return: 返回短轴
     */
    private double calculateRb(double M, double Ib) {
        return (Math.pow(10, (2.3816 + 1.3003 * M - Ib) / 2.8573) - 5) / 50;
    }

}
