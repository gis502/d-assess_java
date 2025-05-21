package com.ruoyi.system.core.drawers;

import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.constant.LayoutConstants;
import com.ruoyi.common.constant.MapConstants;
import com.ruoyi.common.drawers.map.LayoutsDrawer;
import com.ruoyi.common.drawers.map.MapDrawer;
import com.ruoyi.common.drawers.map.WorkSpaceUtils;
import com.ruoyi.common.exception.DownLoadException;
import com.ruoyi.common.exception.FileCreateException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.core.rabbitmq.RabbitConfig;
import com.ruoyi.system.domain.bo.DrawersInfoBO;
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
import java.util.concurrent.CompletableFuture;

/**
 * @author: xiaodemos
 * @date: 2025-04-03 9:21
 * @description: 布局服务类
 */

@Slf4j
@Service
public class LayoutDrawerService {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private LayoutsDrawer layoutsDrawer;
    @Resource
    private MapDrawer mapDrawer;
    private Map map;
    private Workspace workspace;
    private MapLayoutControl mapLayoutControl;

    public LayoutDrawerService(MapDrawer mapDrawer) {
        this.mapDrawer = mapDrawer;
        this.map = new Map();
        this.mapLayoutControl = new MapLayoutControl();
        init();
    }

    private void init() {
        this.workspace = WorkSpaceUtils.open(BaseConstants.WORKSPACE_PATH);
        this.mapLayoutControl.getMapLayout().setWorkspace(workspace);
        this.map.setWorkspace(workspace);
    }

    // 创建震中点、烈度圈、烈度圈文本数据集
    public void createSeismicPictureInit(AssessmentDTO dto) {
        log.info("开始创建震中、烈度圈数据集...");

        // 处理超图中命名格式问题
        String eqTime = dto.getEqTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String mag = String.valueOf(dto.getMagnitude()).replace(".", "_");
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        // 创建震中点名称
        String seismicPointName = "T" + eqTime + dto.getEqAddr() + mag + MapConstants.SEISMIC_POINT + now;
        // 创建烈度圈名称
        String seismicIntensityName = "T" + eqTime + dto.getEqAddr() + mag + MapConstants.SEISMIC_INTENSITY + now;
        // 创建烈度圈影响范围名称
        String seismicIntensityAffectedAreaName = "T" + eqTime + dto.getEqAddr() + mag + MapConstants.SEISMIC_INTENSITY_AFFECTED_AREA + now;
        // 创建烈度圈文本名称
        String seismicIntensityTextName = "T" + eqTime + dto.getEqAddr() + mag + MapConstants.SEISMIC_INTENSITY_TEXT + now;
        // 设置保存的数据集
        String datasetsName = BaseConstants.DATASETS_NAME;
        // 设置震中位置
        Point2D center = new Point2D(dto.getLongitude(), dto.getLatitude());
        // 地震震级
        double magnitude = dto.getMagnitude();

        try {
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
            initMap(datasetVector, intensityCircle, intensityAffectedArea, intensityTextInfo, seismicPointName, center, dto);
        } catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Async("taskExecutor")
    // 初始化每张地图
    public void initMap(DatasetVector datasetVector, DatasetVector intensityCircle,
                        DatasetVector intensityAffectedArea, DatasetVector intensityTextInfo,
                        String seismicPointName, Point2D center, AssessmentDTO dto) {   // 添加震中、烈度圈、烈度圈文本
        log.info("震中、烈度圈数据集正在被加载到地图...");
        int index = 0;
        // 记录下每个图的名称
        for (; index < MapConstants.SEISMIC_MAPS.length; index++) {
            // 打开地图
            map.open(MapConstants.SEISMIC_MAPS[index]);
            // 获取地图图层
            Layers layers = map.getLayers();
            // 添加震中点
            layers.add(datasetVector, true);

            // 对影响场烈度圈做额外图层加载
            if (MapConstants.SEISMIC_MAPS[index].equals(MapConstants.SEISMIC_DISTRIBUTION)) {
                // 添加地震影响场
                layers.add(intensityAffectedArea, true);
            } else {
                // 添加烈度圈
                layers.add(intensityCircle, true);
            }
            // 添加烈度圈文本
            layers.add(intensityTextInfo, true);
            // 获取最新的数据集图层
            Layer layer = layers.get(seismicPointName + "@" + BaseConstants.DATASETS_NAME);
            // 设置图层的样式
            LayerSettingVector vector = mapDrawer.drawerCenterPointStyle();
            // 将样式添加到图层中
            layer.setAdditionalSetting(vector);
            // 设置地图中心点
            map.setCenter(center);
            // 保存地图
            workspace.getMaps().setMapXML(map.getName(), map.toXML());

            // 设置布局信息
            DrawersInfoBO info = new DrawersInfoBO();
            // 设置标题、地震时间、地震地址、地震级别、制作时间
            String title = dto.getEqAddr() + dto.getMagnitude() + MapConstants.GRADE + MapConstants.SEISMIC_MAPS[index];
            String makeTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            String eqTime = dto.getEqTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分"));
            info.setPicName(MapConstants.SEISMIC_MAPS[index]);
            info.setTitle(title);
            info.setMagnitude(dto.getMagnitude());
            info.setEqTime(eqTime);
            info.setMakeTime(makeTime);
            info.setLayoutId(index);    // 设置布局Id
            info.setEqqueueId(dto.getEqqueueId());

            // 获取出图信息
            AssessmentOutputDTO outputDTO = initLayouts(map, info);
            outputDTO.setEqId(dto.getEqId());
            outputDTO.setEqqueueId(dto.getEqqueueId());

            // 送入专题图队列
            rabbitTemplate.convertAndSend(RabbitConfig.DISASTER_EXCHANGE, RabbitConfig.THEMATIC_MAP, outputDTO);
            log.info("专题图 {} 已放入消息队列...", outputDTO.getFileName());
        }
    }

    // 初始化布局
    public AssessmentOutputDTO initLayouts(Map map, DrawersInfoBO info) {
        // 获取布局元素对象
        MapLayout mapLayout = mapLayoutControl.getMapLayout();
        Layouts layouts = workspace.getLayouts();

        // 获取布局对象
        String layoutName = layouts.get(info.getLayoutId());
        // 打开布局
        mapLayout.open(layoutName);
        // 获取所有布局对象
        LayoutElements elements = mapLayoutControl.getMapLayout().getElements();
        // 更换地图
        layoutsDrawer.gridDrawer(elements, map);
        // 创建一个标题对象
        layoutsDrawer.seismicTitleTextDrawer(elements, info.getTitle());
        combineElem(elements, info);

        CompletableFuture<String> future = outputImages(info); // 出图
        String outputImagePath = future.join(); // 使用 join 不抛出异常

        elements.deleteAll();   // 每个布局制作完后都需要进行清理布局中的元素对象

        // TODO 创建专题图产品编码 (需要根据专题图国家标准文件)

        // 计算图片尺寸
        Double v = ImageSizeCalculator(LayoutConstants.DPI);
        // 设置出图信息
        AssessmentOutputDTO output = AssessmentOutputDTO.builder()
                .code(null)
                .fileType(LayoutConstants.IMAGE_TYPE)
                .fileName(info.getPicName())
                .fileExtension(LayoutConstants.EXTENSION_TYPE)
                .fileSize(v)
                .sourceFile("")
                .localSourceFile(outputImagePath)
                .remark("")
                .size(LayoutConstants.SIZE)
                .type(LayoutConstants.THEMATIC_TYPE).build();

        return output;
    }

    public void combineElem(LayoutElements elements, DrawersInfoBO info) {
        // 创建地震三要素文本对象（面对象）
        layoutsDrawer.seismicThreeElementDrawer(elements, info.getEqTime(), info.getEqAddr(), info.getMagnitude());
        // 创建制图单位对象
        layoutsDrawer.madeUnitDrawer(elements);
        // 创建制图时间对象
        layoutsDrawer.madeTimeDrawer(elements, info.getMakeTime());
    }

    @Async("taskExecutor")
    public CompletableFuture<String> outputImages(DrawersInfoBO infoBO) {

        // 处理多层级文件夹名称
        int version = Integer.parseInt(StringUtils.substring(infoBO.getEqqueueId(), infoBO.getEqqueueId().length() - 2));
        String eqId = StringUtils.substring(infoBO.getEqqueueId(), 0, infoBO.getEqqueueId().length() - 2);
        // 路径格式：/upload/专题图/eqId/批次/ XX图 +".jpg"
        String folderPath = LayoutConstants.PICTURE_PREFIX + eqId + "/" + version + "/";
        String filePath = folderPath + infoBO.getPicName() + LayoutConstants.EXTENSION_TYPE;
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
        boolean layoutToJPG = mapLayoutControl.getMapLayout().outputLayoutToJPG(filePath, LayoutConstants.DPI, LayoutConstants.COMPRESS);
        // 图件下载失败 抛出异常
        if (!layoutToJPG) {
            String reason = infoBO.getPicName() + LayoutConstants.OUTPUT_FILED;
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
}
