package com.ruoyi.system.core.drawers;

import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.drawers.map.LayoutsDrawer;
import com.ruoyi.common.drawers.map.MapDrawer;
import com.ruoyi.common.drawers.map.WorkSpaceUtils;
import com.ruoyi.common.exception.DownLoadException;
import com.ruoyi.common.exception.FileCreateException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.core.rabbitmq.RabbitConfig;
import com.ruoyi.system.domain.bo.DrawersInfoBO;
import com.ruoyi.system.domain.bo.DrawersRainInfoBO;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;
import com.supermap.data.DatasetVector;
import com.supermap.data.Layouts;
import com.supermap.data.Point2D;
import com.supermap.data.Workspace;
import com.supermap.layout.LayoutElements;
import com.supermap.layout.MapLayout;
import com.supermap.mapping.Layer;
import com.supermap.mapping.LayerSettingVector;
import com.supermap.mapping.Layers;
import com.supermap.mapping.Map;
import com.supermap.ui.MapLayoutControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author: xiaodemos
 * @date: 2025-04-03 9:21
 * @description: 布局服务类
 */

@Slf4j
@Service
public class SeismicLayoutDrawerService {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private LayoutsDrawer layoutsDrawer;
    @Resource
    private MapDrawer mapDrawer;

    // 创建震中点、烈度圈、烈度圈文本数据集
    public void createSeismicPictureInit(AssessmentDTO dto) {
        log.info("开始创建震中、烈度圈数据集...");

        Workspace workspace = null;

        // 处理超图中命名格式问题
        String eqTime = dto.getEqTime().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        String mag = String.valueOf(dto.getMagnitude()).replace(".", "_");
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        // 创建震中点名称
        String seismicPointName = "T" + eqTime + dto.getEqAddr() + mag + BaseConstants.SEISMIC_POINT + now;
        // 创建烈度圈名称
        String seismicIntensityName = "T" + eqTime + dto.getEqAddr() + mag + BaseConstants.SEISMIC_INTENSITY + now;
        // 创建烈度圈影响范围名称
        String seismicIntensityAffectedAreaName = "T" + eqTime + dto.getEqAddr() + mag + BaseConstants.SEISMIC_INTENSITY_AFFECTED_AREA + now;
        // 创建烈度圈文本名称
        String seismicIntensityTextName = "T" + eqTime + dto.getEqAddr() + mag + BaseConstants.SEISMIC_INTENSITY_TEXT + now;
        // 设置保存的数据集
        String datasetsName = BaseConstants.XI_AN_SEISMIC_DATASETS_NAME;
        // 设置震中位置
        Point2D center = new Point2D(dto.getLongitude(), dto.getLatitude());
        // 地震震级
        double magnitude = dto.getMagnitude();

        try {
            // 打开工作空间
            workspace = WorkSpaceUtils.open(BaseConstants.XI_AN_SEISMIC_WORKSPACE_PATH);
            // 创建震中点
            DatasetVector datasetVector = mapDrawer.createCenterPoint(workspace, datasetsName, seismicPointName, center);
            // 计算离震中最近的断裂带的距离
            int rotation = layoutsDrawer.computeDistance(workspace, datasetVector);
            // 生成烈度圈
            DatasetVector intensityCircle = mapDrawer.createIntensity(workspace, datasetsName, seismicIntensityName, center, magnitude, rotation);
            // 生成地震影响场
            DatasetVector intensityAffectedArea = mapDrawer.createIntensityAffectedArea(workspace, datasetsName, seismicIntensityAffectedAreaName, center, magnitude, rotation);
            // 生成烈度圈文本
            DatasetVector intensityTextInfo = mapDrawer.createIntensityTextInfo(workspace, seismicIntensityTextName, center, magnitude);
            // 出图
            initMap(workspace, datasetVector, intensityCircle, intensityAffectedArea, intensityTextInfo, seismicPointName, center, dto);
        } catch (Exception ex) {
            log.error("创建震中、烈度圈数据集失败...", ex);
            Thread.currentThread().interrupt();
        } finally {
            // 关闭工作空间
            if (workspace != null) {
                // 关闭工作空间
                workspace.close();
                log.info("工作空间关闭成功...");
            }
        }
    }

    @Async("taskExecutor")
    // 初始化每张地图
    public void initMap(Workspace workspace, DatasetVector datasetVector, DatasetVector intensityCircle,
                        DatasetVector intensityAffectedArea, DatasetVector intensityTextInfo,
                        String seismicPointName, Point2D center, AssessmentDTO dto) {   // 添加震中、烈度圈、烈度圈文本
        log.info("震中、烈度圈数据集正在被加载到地图...");
        int index = 0;
        Map map = null;
        MapLayoutControl mapLayoutControl = null;
        try {
            // 操作地图对象
            map = new Map();
            map.setWorkspace(workspace);
            mapLayoutControl = new MapLayoutControl();
            mapLayoutControl.getMapLayout().setWorkspace(workspace);
            // 记录每个图的名称
            for (; index < BaseConstants.XIAN_SEISMIC_MAPS.length; index++) {
                // 打开地图
                map.open(BaseConstants.XIAN_SEISMIC_MAPS[index]);
                // 获取地图图层
                Layers layers = map.getLayers();
                // 添加震中点
                layers.add(datasetVector, true);
                // 对影响场烈度圈做额外图层加载
                if (BaseConstants.XIAN_SEISMIC_MAPS[index].equals(BaseConstants.XIAN_SEISMIC_DISTRIBUTION)) {
                    // 添加地震影响场
                    layers.add(intensityAffectedArea, true);
                } else {
                    // 添加烈度圈
                    layers.add(intensityCircle, true);
                }
                // 添加烈度圈文本
                layers.add(intensityTextInfo, true);
                // 获取最新的数据集图层
                Layer layer = layers.get(seismicPointName + "@" + BaseConstants.XI_AN_SEISMIC_DATASETS_NAME);
                // 设置图层的样式
                LayerSettingVector vector = mapDrawer.drawerCenterPointStyle();
                // 将样式添加到图层中
                layer.setAdditionalSetting(vector);
                // 设置地图中心点
                map.setCenter(center);
                // 保存地图
                workspace.getMaps().setMapXML(map.getName(), map.toXML());

                // 设置布局信息
                DrawersInfoBO info = buildDrawersEqInfoBO(dto, index);

                // 获取出图信息
                AssessmentOutputDTO outputDTO = initLayouts(workspace, mapLayoutControl, map, info, BaseConstants.XIAN_SEISMIC_MAPS[index]);
                outputDTO.setEqId(dto.getEqId());
                outputDTO.setEqqueueId(dto.getEqqueueId());

                // 送入专题图队列
                rabbitTemplate.convertAndSend(RabbitConfig.DISASTER_EXCHANGE, RabbitConfig.THEMATIC_MAP, outputDTO);
                log.info("{} 已放入消息队列...", outputDTO.getFileName());
            }

        } catch (Exception ex) {
            log.error("震中、烈度圈数据集加载到地图失败...", ex);
            Thread.currentThread().interrupt();
        } finally {
            // 关闭地图
            if (map != null) {
                map.close();
                log.info("地图关闭成功...");
            }
        }

    }

    // 初始化布局
    public AssessmentOutputDTO initLayouts(Workspace workspace, MapLayoutControl mapLayoutControl, Map map, DrawersInfoBO info, String mapName) {
        // 获取布局元素对象
        MapLayout mapLayout = mapLayoutControl.getMapLayout();
        Layouts layouts = workspace.getLayouts();

        // 获取布局对象
        String layoutName = layouts.get(info.getLayoutId());
        // 打开布局
        mapLayout.open(layoutName);
        // 获取所有布局对象
        LayoutElements elements = mapLayoutControl.getMapLayout().getElements();

        double pageWidth = mapLayoutControl.getMapLayout().getBounds().getWidth();   // 页面宽度（mm）
        double pageHeight = mapLayoutControl.getMapLayout().getBounds().getHeight(); // 页面高度（mm）

        // 更换地图
        layoutsDrawer.gridDrawer(elements, map);
        // 创建一个标题对象
        layoutsDrawer.thematicTitleTextDrawer(elements, info.getTitle(), pageWidth, pageHeight);

        combineElem(mapLayoutControl, elements, info, mapName);

        CompletableFuture<String> future = outputImages(mapLayoutControl, info); // 出图
        String outputImagePath = future.join(); // 使用 join 不抛出异常

        elements.deleteAll();   // 每个布局制作完后都需要进行清理布局中的元素对象

        // TODO 创建专题图产品编码 (需要根据专题图国家标准文件)

        // 计算图片尺寸
        Double v = ImageSizeCalculator(BaseConstants.DPI);
        // 设置出图信息
        AssessmentOutputDTO output = AssessmentOutputDTO.builder()
                .code(null)
                .fileType(BaseConstants.IMAGE_TYPE)
                .fileName(info.getPicName())
                .fileExtension(BaseConstants.EXTENSION_TYPE)
                .fileSize(v)
                .sourceFile("")
                .localSourceFile(outputImagePath)
                .remark("")
                .size(BaseConstants.SIZE)
                .type(BaseConstants.THEMATIC_TYPE).build();

        return output;
    }

    public void combineElem(MapLayoutControl mapLayoutControl, LayoutElements elements, DrawersInfoBO info, String mapName) {

        double pageWidth = mapLayoutControl.getMapLayout().getBounds().getWidth();   // 页面宽度（mm）
        // 创建地震三要素文本对象（面对象）
        layoutsDrawer.seismicThreeElementDrawer(elements, info.getEqTime(), info.getEqAddr(), info.getMagnitude());
        // 设置比例尺对象
        layoutsDrawer.madeScaleDrawer(elements, pageWidth, mapName);
        // 创建制图单位对象
        layoutsDrawer.madeUnitDrawer(elements, pageWidth);
        // 创建制图时间对象
        layoutsDrawer.madeTimeDrawer(elements, info.getMakeTime(), pageWidth);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> outputImages(MapLayoutControl mapLayoutControl, DrawersInfoBO infoBO) {

        // 处理多层级文件夹名称
        int version = Integer.parseInt(StringUtils.substring(infoBO.getEqqueueId(), infoBO.getEqqueueId().length() - 2));
        String eqId = StringUtils.substring(infoBO.getEqqueueId(), 0, infoBO.getEqqueueId().length() - 2);
        // 路径格式：/upload/专题图/eqId/批次/ XX图 +".jpg"
        String folderPath = BaseConstants.PICTURE_PREFIX + eqId + "/" + version + "/";
        String filePath = folderPath + infoBO.getPicName() + BaseConstants.EXTENSION_TYPE;
        // 创建文件夹路径（如果不存在的话）
        Path path = Paths.get(folderPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);  // 创建多层级文件夹
            } catch (IOException e) {
                // 如果文件夹创建失败，抛出异常
                throw new FileCreateException(BaseConstants.FILE_CREATE_FILED);
            }
        }
        boolean layoutToJPG = mapLayoutControl.getMapLayout().outputLayoutToJPG(filePath, BaseConstants.DPI, BaseConstants.COMPRESS);
        // 图件下载失败 抛出异常
        if (!layoutToJPG) {
            String reason = infoBO.getPicName() + BaseConstants.OUTPUT_FILED;
            throw new DownLoadException(reason);
        }

        return CompletableFuture.completedFuture(filePath);
    }

    // 计算专题图尺寸大小
    private Double ImageSizeCalculator(int dpi) {
        // A3 横向尺寸（毫米）
        final double A3_WIDTH_MM = 420.0;   // A3 横向宽度
        final double A3_HEIGHT_MM = 297.0;  // A3 横向高度

        // 计算图片像素尺寸
        double widthInches = A3_WIDTH_MM / 25.4;
        double heightInches = A3_HEIGHT_MM / 25.4;
        int widthPixels = (int) Math.round(widthInches * dpi);
        int heightPixels = (int) Math.round(heightInches * dpi);

        // 计算未压缩位图大小（32位色深，4字节/像素）
        long rawSizeBytes = (long) widthPixels * heightPixels * 4;
        double rawSizeMB = rawSizeBytes / (1024.0 * 1024.0);

        return rawSizeMB;
    }


    private DrawersInfoBO buildDrawersEqInfoBO(AssessmentDTO dto, int index) {

        // 设置布局信息
        DrawersInfoBO info = new DrawersInfoBO();
        // 设置标题、地震时间、地震地址、地震级别、制作时间
        String title = dto.getEqAddr() + dto.getMagnitude() + BaseConstants.GRADE + "" + BaseConstants.XIAN_SEISMIC_MAPS[index];
        String makeTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        String eqTime = dto.getEqTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分"));
        info.setPicName(BaseConstants.XIAN_SEISMIC_MAPS[index]);
        info.setTitle(title);
        info.setMagnitude(dto.getMagnitude());
        info.setEqAddr(dto.getEqAddr());
        info.setEqTime(eqTime);
        info.setMakeTime(makeTime);
        info.setLayoutId(index);    // 设置布局Id
        info.setEqqueueId(dto.getEqqueueId());

        return info;
    }

}
