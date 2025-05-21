/*
package com.supermap.samplecode.utils;

import com.supermap.data.*;
import com.supermap.mapping.*;
import com.supermap.ui.MapControl;
import javax.swing.*;
import java.awt.*;

public class IntensityEllipseDrawer {

    public static void main(String[] args) {
        // 打开工作空间
        String workspacePath = "E:/GIS小组专题图产出/专题图模板/专题图.smwu";
        Workspace workspace = WorkSpaceUtils.open(workspacePath);

        // 创建地图窗口
        MapControl mapControl = new MapControl();
        Map map = mapControl.getMap();
        map.setWorkspace(workspace);

        // 定义震级和椭圆中心点
        double magnitude = 6.1;
        Point2D center = new Point2D(120.5, 30.5); // 示例经纬度

        // 定义烈度值
        int[] intensityLevels = {6, 7, 8};
        String[] intensityLevelsRoma = {"Ⅷ度", "Ⅶ度", "Ⅵ度"};

        // 创建数据集并绘制烈度圈
        DatasetVector datasetVector = createLineIntensityEllipses(workspace, center, magnitude, intensityLevels);


        Layer layer = map.getLayers().add(datasetVector, true);

        DatasetVector epicenterDataset = createEpicenter(workspace, center,map);
        map.getLayers().add(epicenterDataset, true);

        // themeActiveLineIntensity(map,layer);
        themeActiveRegionIntensity(map,layer);

        // 创建文本标注数据集
        DatasetVector textDataset = createTextDataset(workspace);
        addTextAnnotations(textDataset, center, magnitude, intensityLevels, intensityLevelsRoma);
        map.getLayers().add(textDataset, true);

        // 设置地图范围并刷新
        map.setViewBounds(datasetVector.getBounds());

        map.refresh();

        // 创建 JFrame 并添加 MapControl
        JFrame frame = new JFrame("Intensity Ellipses Map");
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mapControl, BorderLayout.CENTER);
        frame.setVisible(true);
    }


    // 创建线烈度圈
    private static DatasetVector createLineIntensityEllipses(Workspace workspace, Point2D center, double magnitude, int[] intensityLevels) {
        DatasourceConnectionInfo info = new DatasourceConnectionInfo();
        info.setEngineType(EngineType.UDB);

        // TODO 这里需要根据发震时间和地点来创建数据源
        info.setServer("E:/GIS小组专题图产出/专题图模板/烈度圈数据源/震中点数据源.udbx");
        info.setAlias("IntensityEllipseData");

        Datasource datasource = workspace.getDatasources().create(info);
        DatasetVectorInfo datasetInfo = new DatasetVectorInfo();
        datasetInfo.setName("IntensityEllipses");
        datasetInfo.setType(DatasetType.REGION);
        DatasetVector datasetVector = datasource.getDatasets().create(datasetInfo);

        Recordset recordset = datasetVector.getRecordset(false, CursorType.DYNAMIC);
        for (int intensity : intensityLevels) {
            double Ra = calculateRa(magnitude, intensity);
            double Rb = calculateRb(magnitude, intensity);
            GeoEllipse ellipse = new GeoEllipse(center, Ra, Rb, 0);

            recordset.addNew(ellipse);
            recordset.update();
        }
        recordset.close();

        return datasetVector;
    }


    //创建文本数据集
    private static DatasetVector createTextDataset(Workspace workspace) {
        Datasource datasource = workspace.getDatasources().get("IntensityEllipseData");
        DatasetVectorInfo datasetInfo = new DatasetVectorInfo();
        datasetInfo.setName("IntensityLabels");
        datasetInfo.setType(DatasetType.TEXT); // 修改为 TEXT 类型
        return datasource.getDatasets().create(datasetInfo);
    }


    private static void addTextAnnotations(DatasetVector datasetVector, Point2D center, double magnitude,
                                           int[] intensityLevels, String[] intensityLevelsRoma) {

        Recordset recordset = datasetVector.getRecordset(false, CursorType.DYNAMIC);
        for (int i = 0; i < intensityLevels.length; i++) {
            int intensity = intensityLevels[i];
            double Rb = calculateRb(magnitude, intensity);

            Point2D textPosition = new Point2D(center.getX() - 1.5, center.getY() + Rb - 0.5);

            // 创建文本对象
            TextPart textPart = new TextPart(intensityLevelsRoma[i]+ "(" + intensity + "度)", textPosition);
            GeoText geoText = new GeoText(textPart);

            // **修改文本样式**
            TextStyle textStyle = new TextStyle();
            textStyle.setForeColor(new Color(255, 0, 0)); // 文字颜色
            textStyle.setFontName("Arial");  // 字体
            textStyle.setFontHeight(3.1);     // 字体高度
            textStyle.setBold(true);         // 加粗
            geoText.setTextStyle(textStyle); // **使用 TextStyle 代替 GeoStyle**

            recordset.addNew(geoText);
            recordset.update();

        }
        recordset.close();
    }

    //创建点
    private static DatasetVector createEpicenter(Workspace workspace, Point2D center, Map map) {

        Datasource datasource = workspace.getDatasources().get("IntensityEllipseData");
        if (datasource == null) {
            throw new RuntimeException("Failed to get datasource: IntensityEllipseData");
        }

        DatasetVectorInfo datasetInfo = new DatasetVectorInfo();
        datasetInfo.setName("Epicenter");
        datasetInfo.setType(DatasetType.POINT);
        DatasetVector datasetVector = datasource.getDatasets().create(datasetInfo);
        Recordset recordset = datasetVector.getRecordset(false, CursorType.DYNAMIC);
        GeoPoint epicenter = new GeoPoint(center);
        recordset.addNew(epicenter);
        recordset.update();
        recordset.close();

        // **添加图层**
        Layer layer = map.getLayers().add(datasetVector, true);

        // **设置图层样式**
        LayerSettingVector layerSetting = (LayerSettingVector) layer.getAdditionalSetting();
        GeoStyle style = new GeoStyle();
        style.setMarkerSymbolID(500);  // **尝试不同的 ID（可用 ID 可能不同）**
        style.setMarkerSize(new Size2D(10, 10)); // **确保使用 Size2D**
        style.setFillForeColor(Color.RED); // **填充颜色**
        style.setFillOpaqueRate(100); // **不透明**
        style.setLineColor(Color.RED); // **边框颜色**

        layerSetting.setStyle(style);  // **绑定样式**
        map.refresh(); // **刷新地图**

        return datasetVector;
    }

    // 确保样式生效
    public static void themeActiveLineIntensity(Map map, Layer layer){

        // **确保样式生效**
        LayerSettingVector layerSetting = (LayerSettingVector) layer.getAdditionalSetting();
        GeoStyle layerStyle = new GeoStyle();
        layerStyle.setFillForeColor(new Color(0, 0, 0)); // 设置填充颜色（红色）
        layerStyle.setFillOpaqueRate(0); // 设置透明度
        layerStyle.setLineColor(Color.RED); // 设置边框颜色
        layerStyle.setLineWidth(0.1); // 设置边框宽度

        layerSetting.setStyle(layerStyle);
        map.refresh();

    }

    public static void themeActiveRegionIntensity(Map map, Layer layer) {

        // **确保样式生效**
        LayerSettingVector layerSetting = (LayerSettingVector) layer.getAdditionalSetting();
        GeoStyle layerStyle = new GeoStyle();
        layerStyle.setFillForeColor(new Color(255, 0, 0)); // 设置填充颜色（红色）
        layerStyle.setFillOpaqueRate(0); // 设置透明度
        layerStyle.setLineColor(Color.RED); // 设置边框颜色
        layerStyle.setLineWidth(0.1); // 设置边框宽度

        layerSetting.setStyle(layerStyle);
        map.refresh();

    }

    private static double calculateRa(double M, double Ia) {
        return Math.pow(10, (4.0293 + 1.3003 * M - Ia) / 3.6404) - 10;
    }

    private static double calculateRb(double M, double Ib) {
        return Math.pow(10, (2.3816 + 1.3003 * M - Ib) / 2.8573) - 5;
    }

}
*/

package com.ruoyi.common.drawers.map;

import com.supermap.data.*;

import java.awt.*;

public class IntensityEllipseDrawer {


    /**
     * @param workspace 工作空间
     * @param center 震中点
     * @param magnitude 震级
     * @author: xiaodemos
     * @date: 2025/3/31 9:46
     * @description: 绘制烈度圈和烈度值
     * @return: 返回一个复合对象
     */
    public static GeoCompound getIntensityGeometry(Workspace workspace, Point2D center, double magnitude) {
        // 定义烈度值
        int[] intensityLevels = {8, 7, 6};
        String[] intensityLevelsRoma = {"Ⅷ度", "Ⅶ度", "Ⅵ度"};

        // 创建复合几何对象
        GeoCompound geoCompound = new GeoCompound();

        // 添加烈度圈（椭圆）
        for (int intensity : intensityLevels) {

            GeoEllipse ellipse = getGeoEllipse(center, magnitude, intensity);

            geoCompound.addPart(ellipse);
        }

        // 添加文本标注
        for (int i = 0; i < intensityLevels.length; i++) {
            int intensity = intensityLevels[i];
            double Rb = calculateRb(magnitude, intensity);
            Point2D textPosition = new Point2D(1300, 917 + Rb + 50);

            TextPart textPart = new TextPart(intensityLevelsRoma[i] + "(" + intensity + "度)", textPosition);
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
     * @param center    震中点
     * @param magnitude 震级
     * @param intensity 烈度
     * @author: xiaodemos
     * @date: 2025/3/31 9:45
     * @description: 绘制烈度圈（椭圆）并设置样式
     * @return: 返回一个椭圆对象
     */
    private static GeoEllipse getGeoEllipse(Point2D center, double magnitude, int intensity) {
        double Ra = calculateRa(magnitude, intensity);

        double Rb = calculateRb(magnitude, intensity);

        GeoEllipse ellipse = new GeoEllipse(new Point2D(1376, 917), Ra, Rb, 90);

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
     * @param M  震级
     * @param Ia 长轴烈度
     * @author: xiaodemos
     * @date: 2025/3/31 9:43
     * @description: 计算椭圆长轴
     * @return: 返回椭圆长轴
     */
    private static double calculateRa(double M, double Ia) {
        return (Math.pow(10, (4.0293 + 1.3003 * M - Ia) / 3.6404) - 10) * 27;
    }

    /**
     * @param M  震级
     * @param Ib 短轴烈度
     * @author: xiaodemos
     * @date: 2025/3/31 9:43
     * @description: 计算椭圆短轴
     * @return: 返回椭圆短轴
     */
    private static double calculateRb(double M, double Ib) {
        return (Math.pow(10, (2.3816 + 1.3003 * M - Ib) / 2.8573) - 5) * 27;
    }

}
