package com.ruoyi.common.drawers.map;

import com.ruoyi.common.constant.BaseConstants;
import com.supermap.analyst.spatialanalyst.ComputeDistanceResult;
import com.supermap.analyst.spatialanalyst.ProximityAnalyst;
import com.supermap.data.*;
import com.supermap.layout.LayoutElements;
import com.supermap.mapping.Map;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * @author: xiaodemos
 * @date: 2025-04-02 2:30
 * @description: 布局绘制类
 */

@Component
public class LayoutsDrawer {

    // 计算断层距震中距离
    public int computeDistance(Workspace workspace, DatasetVector datasetVector) {
        // 震中点数据集
        Recordset center = datasetVector.getRecordset(false, CursorType.DYNAMIC);
        // 断裂带数据集
        DatasetVector datasource = WorkSpaceUtils.getDatasource(workspace, BaseConstants.XI_AN_MAP_DATASETS_NAME, BaseConstants.XI_AN_RUPTURE_NAME);
        Recordset fault = datasource.getRecordset(false, CursorType.DYNAMIC);

        // 使用同样的坐标系
        datasetVector.setPrjCoordSys(datasource.getPrjCoordSys());
        // 开始计算
        ComputeDistanceResult[] computeDistanceResults = ProximityAnalyst.computeMinDistance(center, fault, 0, -1, null);

        // 最短距离
        int faultId = 0;
        for (ComputeDistanceResult result : computeDistanceResults) {
            for (int id : result.getReferenceGeometryIDs()) {
                faultId = id;
                break;
            }
        }

        // 查询断带数据集中的数据
        Recordset query = datasource.query("SmID=" + faultId, CursorType.STATIC);
        Object rotation = query.getFieldValue("Strike");

        return Integer.parseInt(String.valueOf(rotation));
    }

    // 绘制网格线
    public void gridDrawer(LayoutElements elements, Map map) {
        // 获取地图对象
        Geometry layoutMap = elements.getGeometry();

        // 转换为 GeoMap 对象
        GeoMap geoMap = (GeoMap) layoutMap;
        GeoMapGrid mapGrid = geoMap.getMapGrid();
        GeoMapBorder mapBorder = geoMap.getMapBorder();

        // 设置地图边界范围大小
        Rectangle2D bounds = layoutMap.getBounds();
        bounds.setLeft(bounds.getLeft());
        bounds.setBottom(bounds.getBottom());
        bounds.setRight(bounds.getRight());
        bounds.setTop(bounds.getTop());
        // 设置地图边界线
        GeoRectangle rect = new GeoRectangle(bounds, 0);

        // 获取地图内容
        String mapXml = map.toXML();
        // 创建新地图
        GeoMap replaceMap = new GeoMap(mapXml, rect);
        replaceMap.setMapName(map.getName());
        // 设置地图网格线
        replaceMap.setGridVisible(true);
        // 设置地图边界线
        replaceMap.setBorderVisible(true);
        replaceMap.setMapScale(map.getScale());

        // 设置 GeoMapGrid 对象的相关属性，即设置地图的经纬网的风格。
        GeoMapGrid geoMapGrid = new GeoMapGrid();
        // 设置为地图添加经纬网。
        geoMapGrid.setGridType(GeoMapGridType.GRATICULE);
        // 设置经纬网的格网线的线型风格。
        geoMapGrid.getGridLineStyle().setLineSymbolID(0);
        geoMapGrid.getGridLineStyle().setLineColor(Color.BLACK);
        // 设置经纬网的格网线的水平、竖直间距。
        geoMapGrid.setHorizontalGridDistance(0.299);
        geoMapGrid.setVerticalGridDistance(0.298);
        // 设置经纬网的格网线的类型为实线。
        geoMapGrid.setGridLineType(GeoMapGridLineType.SOLIDLINE);
        // 设置经纬网的边框风格。
        GeoStyle style = new GeoStyle();
        style.setLineSymbolID(6);
        style.setLineColor(Color.BLACK);
        style.setLineWidth(0.1);
        geoMapGrid.setBorderLineStyle(style);
        // 设置经纬网文本标注的水平、竖直位置。
        geoMapGrid.setHorizontalTextPosition(HorizontalTextPositionType.MIDDLE);
        geoMapGrid.setVerticalTextPosition(VerticalTextPositionType.MIDDLE);
        // 设置经纬网文本标注的文本风格。
        geoMapGrid.getGridLineTextStyle().setFontName("Segoe UI");
        geoMapGrid.getGridLineTextStyle().setForeColor(Color.BLACK);
        geoMapGrid.getGridLineTextStyle().setFontHeight(42);
        geoMapGrid.setMaxDisplayDecimalLength(0);   // 设置标注显示的小数位

        // 设置地图网格线
        replaceMap.setMapGrid(geoMapGrid);
        // 设置地图边界线
        replaceMap.setMapBorder(mapBorder);
        // 修改地图对象
        elements.setGeometry(replaceMap);
        elements.refresh();
    }

    // 绘制页脚单位
    public void madeUnitDrawer(LayoutElements elements, double pageWidth) {

        double segmentWidth = pageWidth / 3;
        double baseY = -50;

        // 创建文本对象
        TextPart unitText = new TextPart();
        unitText.setText("制图单位：" + BaseConstants.UNIT);

        TextStyle unitTextStyle = new TextStyle();
        unitTextStyle.setFontName("宋体");
        unitTextStyle.setForeColor(new Color(0, 0, 0));
        unitTextStyle.setFontHeight(5);

        GeoText unitGeoText = new GeoText(unitText);
        unitGeoText.setTextStyle(unitTextStyle);

        // 计算文本宽度，在第二部分居中
        double unitTextWidth = getTextWidth(unitText.getText(), unitTextStyle);
        double secondPartCenterX = segmentWidth + segmentWidth / 4;
        unitGeoText.offset(secondPartCenterX - unitTextWidth / 2, baseY);

        elements.addNew(unitGeoText);

    }

    // 绘制页脚制图时间
    public void madeTimeDrawer(LayoutElements elements, String makeTime, double pageWidth) {

        double segmentWidth = pageWidth / 3;
        double baseY = -50;
        // 创建文本对象
        TextPart makeTimeText = new TextPart();
        makeTimeText.setText("制图时间：" + makeTime);

        TextStyle makeTimeTextStyle = new TextStyle();
        makeTimeTextStyle.setFontName("宋体");
        makeTimeTextStyle.setForeColor(new Color(0, 0, 0));
        makeTimeTextStyle.setFontHeight(5);

        GeoText makeTimeGeoText = new GeoText(makeTimeText);
        makeTimeGeoText.setTextStyle(makeTimeTextStyle);

        // 计算文本宽度，在第三部分居中
        double timeTextWidth = getTextWidth(makeTimeText.getText(), makeTimeTextStyle);
        double thirdPartCenterX = segmentWidth * 2 + segmentWidth / 4;
        makeTimeGeoText.offset(thirdPartCenterX - timeTextWidth / 2, baseY);

        elements.addNew(makeTimeGeoText);
    }

    // 绘制页眉标题
    public void thematicTitleTextDrawer(LayoutElements elements, String title, double width, double height) {
        // 设置文本风格
        TextStyle textStyle = new TextStyle();
        textStyle.setFontName("微软雅黑");
        textStyle.setFontHeight(13);
        textStyle.setForeColor(new Color(0, 0, 0));
        textStyle.setAlignment(TextAlignment.TOPCENTER);

        double textWidth = title.length() * textStyle.getFontHeight() * 0.5;

        Point2D textPosition = new Point2D((width - textWidth) / 2, height - 380);
        TextPart textPart = new TextPart(title, textPosition);

        // 创建一个文本对象
        GeoText titleGeoText = new GeoText(textPart);
        titleGeoText.setTextStyle(textStyle);

        // 将元素加入到当前的布局中
        elements.addNew(titleGeoText);
    }

    // 计算文本宽度
    private static double getTextWidth(String text, TextStyle style) {
        // 根据字体大小和字符数估算文本宽度
        return text.length() * style.getFontHeight() * 0.5;
    }

    public void madeScaleDrawer(LayoutElements elements, double pageWidth, String mapName) {
        // 计算每个部分的宽度（总宽度分为3等份）
        double segmentWidth = pageWidth / 3;
        // 底部元素的Y坐标（统一设置，确保在同一水平线上）
        double baseY = -50;

        // 比例尺文本
        TextPart scaleText = new TextPart();
        scaleText.setText("比例尺：");

        TextStyle scaleTextStyle = new TextStyle();
        scaleTextStyle.setFontName("宋体");
        scaleTextStyle.setForeColor(new Color(0, 0, 0));
        scaleTextStyle.setFontHeight(5);

        GeoText scaleGeoText = new GeoText(scaleText);
        scaleGeoText.setTextStyle(scaleTextStyle);

        // 计算比例尺文本的宽度，用于定位
        double scaleTextWidth = getTextWidth(scaleText.getText(), scaleTextStyle);
        // 第一部分的中心位置
        double firstPartCenterX = segmentWidth / 10;
        // 文本放在比例尺左侧，整体在第一部分居中
        scaleGeoText.offset(firstPartCenterX - scaleTextWidth, baseY);  // 50是比例尺宽度的大致偏移

        // 比例尺几何对象
        GeoMapScale geoMapScale = new GeoMapScale(new Point2D(), 1000, 70);
        // 设置比例尺尺寸
        geoMapScale.setScale(BaseConstants.MAP_SCALE.get(mapName));
        geoMapScale.setScaleShowUnit(Unit.KILOMETER);
        geoMapScale.setScaleType(GeoMapScaleType.RAILWAY);
        geoMapScale.setSegmentCount(2);
        geoMapScale.setLeftDivisionCount(0);  // 取消0之前的主刻度
        geoMapScale.setDivisions(1);
        geoMapScale.setDivisionDisplayType(GeoMapScaleDisplayType.MAIN_DIVISON);
        geoMapScale.setNumberDisplayType(GeoMapScaleDisplayType.MAIN_DIVISON);
        // 比例尺放在文本右侧，整体在第一部分居中
        geoMapScale.offset(firstPartCenterX + 300, baseY - 50);

        // 添加比例尺文本和比例尺
        elements.addNew(scaleGeoText);
        elements.addNew(geoMapScale);

    }

    // 绘制地震三要素
    public void seismicThreeElementDrawer(LayoutElements elements, String time, String addr, double intensity) {

        // 创建矩形面对象
        GeoRectangle rectangle = new GeoRectangle();
        // 设置矩形的位置和大小，这里以左上角坐标和宽高为例
        rectangle.setWidth(700);
        rectangle.setHeight(300);

        rectangle.offset(404, 2248);

        // 设置矩形框背景、线条颜色
        GeoStyle style = new GeoStyle();
        style.setLineColor(Color.BLACK);
        style.setFillForeColor(new Color(251, 213, 181));

        rectangle.setStyle(style);
        elements.addNew(rectangle);

        // 设置地震三要素文本对象
        TextPart timeText = new TextPart();
        timeText.setText("时间：" + time);
        timeText.offset(100, 2365);

        GeoText timeGeoText = new GeoText(timeText);

        TextStyle timeTextStyle = new TextStyle();
        timeTextStyle.setFontName("微软雅黑");
        timeTextStyle.setForeColor(new Color(0, 0, 0));
        timeTextStyle.setFontHeight(5);
        timeGeoText.setTextStyle(timeTextStyle);

        elements.addNew(timeGeoText);

        TextPart addrText = new TextPart();
        addrText.setText("震级：" + intensity + "级");
        addrText.offset(100, 2265);

        GeoText addrGeoText = new GeoText(addrText);

        TextStyle addrTextStyle = new TextStyle();
        addrTextStyle.setFontName("微软雅黑");
        addrTextStyle.setForeColor(new Color(0, 0, 0));
        addrTextStyle.setFontHeight(5);
        addrGeoText.setTextStyle(addrTextStyle);

        elements.addNew(addrGeoText);

        // 震级文本对象
        TextPart intensityText = new TextPart();
        intensityText.setText("位置：" + addr);
        intensityText.offset(100, 2165);

        GeoText intensityGeoText = new GeoText(intensityText);

        TextStyle intensityTextStyle = new TextStyle();
        intensityTextStyle.setFontName("微软雅黑");
        intensityTextStyle.setForeColor(new Color(0, 0, 0));
        intensityTextStyle.setFontHeight(5);
        intensityGeoText.setTextStyle(intensityTextStyle);

        elements.addNew(intensityGeoText);

    }

    // 绘制暴雨三要素（发生时间、降雨量、累计持续时间）
    public void rainThreeElementDrawer(LayoutElements elements, String time, String rainfall, String duration) {

        // 创建矩形面对象
        GeoRectangle rectangle = new GeoRectangle();
        // 设置矩形的位置和大小，这里以左上角坐标和宽高为例
        rectangle.setWidth(700);
        rectangle.setHeight(300);

        rectangle.offset(404, 2248);

        // 设置矩形框背景、线条颜色
        GeoStyle style = new GeoStyle();
        style.setLineColor(Color.BLACK);
        style.setFillForeColor(new Color(251, 213, 181));

        rectangle.setStyle(style);
        elements.addNew(rectangle);

        // 设置地震三要素文本对象
        TextPart timeText = new TextPart();
        timeText.setText("时间：" + time);
        timeText.offset(100, 2365);

        GeoText timeGeoText = new GeoText(timeText);

        TextStyle timeTextStyle = new TextStyle();
        timeTextStyle.setFontName("微软雅黑");
        timeTextStyle.setForeColor(new Color(0, 0, 0));
        timeTextStyle.setFontHeight(5);
        timeGeoText.setTextStyle(timeTextStyle);

        elements.addNew(timeGeoText);

        TextPart addrText = new TextPart();
        addrText.setText("累计降雨量：" + rainfall + "毫米");
        addrText.offset(100, 2265);

        GeoText addrGeoText = new GeoText(addrText);

        TextStyle addrTextStyle = new TextStyle();
        addrTextStyle.setFontName("微软雅黑");
        addrTextStyle.setForeColor(new Color(0, 0, 0));
        addrTextStyle.setFontHeight(5);
        addrGeoText.setTextStyle(addrTextStyle);

        elements.addNew(addrGeoText);

        // 震级文本对象
        TextPart intensityText = new TextPart();
        intensityText.setText("已持续：" + duration + "小时");
        intensityText.offset(100, 2165);

        GeoText intensityGeoText = new GeoText(intensityText);

        TextStyle intensityTextStyle = new TextStyle();
        intensityTextStyle.setFontName("微软雅黑");
        intensityTextStyle.setForeColor(new Color(0, 0, 0));
        intensityTextStyle.setFontHeight(5);
        intensityGeoText.setTextStyle(intensityTextStyle);

        elements.addNew(intensityGeoText);
    }

}
