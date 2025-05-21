package com.ruoyi.common.drawers.map;

import com.ruoyi.common.constant.LayoutConstants;
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

    public int computeDistance(Workspace workspace, DatasetVector datasetVector) {
        // 震中点数据集
        Recordset center = datasetVector.getRecordset(false, CursorType.DYNAMIC);
        // 断裂带数据集
        DatasetVector datasource = WorkSpaceUtils.getDatasource(workspace, "专题图数据源", "雅安断裂带数据");
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

    }

    public void madeUnitDrawer(LayoutElements elements) {
        // 创建文本对象
        TextPart unitText = new TextPart();
        unitText.setText("制图单位：" + LayoutConstants.UNIT);
        unitText.offset(1200, -20);

        GeoText unitGeoText = new GeoText(unitText);

        // 创建文本格式
        TextStyle unitTextStyle = new TextStyle();
        unitTextStyle.setFontName("宋体");
        unitTextStyle.setForeColor(new Color(0, 0, 0));
        unitTextStyle.setFontHeight(3.5);
        unitGeoText.setTextStyle(unitTextStyle);

        // 添加到布局中
        elements.addNew(unitGeoText);

    }

    public void madeTimeDrawer(LayoutElements elements, String makeTime) {
        // 创建文本对象
        TextPart makeTimeText = new TextPart();
        makeTimeText.setText("制图时间：" + makeTime);
        makeTimeText.offset(2200, -20);

        GeoText makeTimeGeoText = new GeoText(makeTimeText);

        TextStyle makeTimeTextStyle = new TextStyle();
        makeTimeTextStyle.setFontName("宋体");
        makeTimeTextStyle.setForeColor(new Color(0, 0, 0));
        makeTimeTextStyle.setFontHeight(3.5);
        makeTimeGeoText.setTextStyle(makeTimeTextStyle);

        // 添加到布局
        elements.addNew(makeTimeGeoText);
    }

    public void seismicTitleTextDrawer(LayoutElements elements, String title) {
        // 设置文本位置
        Point2D textPosition = new Point2D(600.0, 1900.0);
        TextPart textPart = new TextPart(title, textPosition);

        // 创建一个文本对象
        GeoText titleGeoText = new GeoText(textPart);

        // 设置文本风格
        TextStyle textStyle = new TextStyle();
        textStyle.setFontName("微软雅黑");
        textStyle.setFontHeight(9);
        textStyle.setForeColor(new Color(0, 0, 0));

        titleGeoText.setTextStyle(textStyle);

        // 将元素加入到当前的布局中
        elements.addNew(titleGeoText);
    }

    public void seismicThreeElementDrawer(LayoutElements elements, String time, String addr, double intensity) {
        // 创建矩形面对象
        GeoRectangle rectangle = new GeoRectangle();
        // 设置矩形的位置和大小，这里以左上角坐标和宽高为例
        rectangle.setWidth(550);
        rectangle.setHeight(200);
        rectangle.offset(377, 1663);

        // 设置矩形框背景、线条颜色
        GeoStyle style = new GeoStyle();
        style.setLineColor(Color.BLACK);
        style.setFillForeColor(new Color(251, 213, 181));

        rectangle.setStyle(style);

        elements.addNew(rectangle);

        // 设置地震三要素文本对象
        TextPart timeText = new TextPart();
        timeText.setText("时间：" + time);
        timeText.offset(120, 1750);
        GeoText timeGeoText = new GeoText(timeText);
        TextStyle timeTextStyle = new TextStyle();
        timeTextStyle.setFontName("微软雅黑");
        timeTextStyle.setForeColor(new Color(0, 0, 0));
        timeTextStyle.setFontHeight(4);
        timeGeoText.setTextStyle(timeTextStyle);
        elements.addNew(timeGeoText);

        TextPart addrText = new TextPart();
        addrText.setText("震级：" + intensity + "级");
        addrText.offset(120, 1680);
        GeoText addrGeoText = new GeoText(addrText);
        TextStyle addrTextStyle = new TextStyle();
        addrTextStyle.setFontName("微软雅黑");
        addrTextStyle.setForeColor(new Color(0, 0, 0));
        addrTextStyle.setFontHeight(4);
        addrGeoText.setTextStyle(addrTextStyle);
        elements.addNew(addrGeoText);

        // 震级文本对象
        TextPart intensityText = new TextPart();
        intensityText.setText("位置：" + addr);
        intensityText.offset(120, 1610);
        GeoText intensityGeoText = new GeoText(intensityText);
        TextStyle intensityTextStyle = new TextStyle();
        intensityTextStyle.setFontName("微软雅黑");
        intensityTextStyle.setForeColor(new Color(0, 0, 0));
        intensityTextStyle.setFontHeight(4);
        intensityGeoText.setTextStyle(intensityTextStyle);
        elements.addNew(intensityGeoText);

    }

}
