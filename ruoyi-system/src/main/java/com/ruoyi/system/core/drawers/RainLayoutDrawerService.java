package com.ruoyi.system.core.drawers;

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
import com.ruoyi.system.domain.dto.RainAssessmentDTO;
import com.ruoyi.system.domain.dto.RainAssessmentOutputDTO;
import com.ruoyi.system.mapper.RainAssessmentBatchMapper;
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
public class RainLayoutDrawerService {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private LayoutsDrawer layoutsDrawer;
    @Resource
    private MapDrawer mapDrawer;
    private Map map;
    private Workspace workspace;
    private MapLayoutControl mapLayoutControl;

    public RainLayoutDrawerService(MapDrawer mapDrawer) {
        this.mapDrawer = mapDrawer;
        this.map = new Map();
        this.mapLayoutControl = new MapLayoutControl();
        init();
    }

    private void init() {
        this.workspace = WorkSpaceUtils.open(BaseConstants.XI_AN_STORM_WORKSPACE_PATH);
        this.mapLayoutControl.getMapLayout().setWorkspace(workspace);
        this.map.setWorkspace(workspace);
    }

    // 创建震中点、烈度圈、烈度圈文本数据集
    public void createSeismicPictureInit(RainAssessmentDTO dto) {
        log.info("开始创建暴雨数据集...");

        // 处理超图中命名格式问题
        String rainTime = dto.getOccurrenceTime().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        // 持续时间
        String duration = String.valueOf(dto.getDuration());
        // 降雨量
        String rainfall = String.valueOf(dto.getRainfall());
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        // 创建暴雨中心点名称2022年西安突发 12 小时 502 mm 降雨量
        String rainPointName = "R" + rainTime + dto.getPosition() + "发生" + duration + "小时" + rainfall + "降雨量";
        // 设置保存的数据集
        String datasetsName = BaseConstants.XI_AN_STORM_DATASETS_NAME;
        // 设置震中位置
        Point2D center = new Point2D(dto.getLongitude(), dto.getLatitude());

        try {
            // 创建暴雨中点
            DatasetVector datasetVector = mapDrawer.createCenterPoint(workspace, datasetsName, rainPointName, center);
            // 出图
            initMap(datasetVector, rainPointName, center, dto);
        } catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    @Async("taskExecutor")
    // 初始化每张地图
    public void initMap(DatasetVector datasetVector, String rainPointName, Point2D center, RainAssessmentDTO dto) {
        log.info("暴雨数据集正在被加载到地图...");

        // 记录下每个图的名称
        for (int index = 0; index < BaseConstants.XIAN_STORM_MAPS.length; index++) {
            try {
                // 打开地图
                map.open(BaseConstants.XIAN_STORM_MAPS[index]);
                // 获取地图图层
                Layers layers = map.getLayers();
                // 添加震中点
                layers.add(datasetVector, true);
                // 获取最新的数据集图层
                Layer layer = layers.get(rainPointName + "@" + BaseConstants.XI_AN_STORM_DATASETS_NAME);
                // 设置图层的样式
                LayerSettingVector vector = mapDrawer.drawerRainCenterPointStyle();
                // 将样式添加到图层中
                layer.setAdditionalSetting(vector);
                // 设置地图中心点
                map.setCenter(center);
                // 保存地图
                workspace.getMaps().setMapXML(map.getName(), map.toXML());
                // 设置布局信息
                DrawersRainInfoBO info = buildDrawersRainInfoBO(dto, index);
                // 获取出图信息
                RainAssessmentOutputDTO outputDTO = initLayouts(map, info, BaseConstants.XIAN_STORM_MAPS[index]);

                outputDTO.setRainId(dto.getRainId());
                outputDTO.setRainQueueId(dto.getRainQueueId());

                // 送入专题图队列
                rabbitTemplate.convertAndSend(RabbitConfig.DISASTER_EXCHANGE, RabbitConfig.RAIN_MAP, outputDTO);
                log.info("{} 已放入消息队列...", outputDTO.getFileName());

            } catch (Exception ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
            } finally {
                map.close();
            }
        }
    }

    // 初始化布局
    public RainAssessmentOutputDTO initLayouts(Map map, DrawersRainInfoBO info, String mapName) {
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

        combineElem(elements, info, mapName);

        CompletableFuture<String> future = outputImages(info); // 出图
        String outputImagePath = future.join(); // 使用 join 不抛出异常

        elements.deleteAll();   // 每个布局制作完后都需要进行清理布局中的元素对象

        // TODO 创建专题图产品编码 (需要根据专题图国家标准文件)

        // 计算图片尺寸
        Double v = ImageSizeCalculator(BaseConstants.DPI);
        // 设置出图信息
        RainAssessmentOutputDTO output = RainAssessmentOutputDTO.builder()
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

    public void combineElem(LayoutElements elements, DrawersRainInfoBO info, String mapName) {

        double pageWidth = mapLayoutControl.getMapLayout().getBounds().getWidth();   // 页面宽度（mm）
        // 创建地震三要素文本对象（面对象）
        layoutsDrawer.rainThreeElementDrawer(elements, info.getRainTime(), info.getRainfall(), info.getDuration());
        // 设置比例尺对象
        layoutsDrawer.madeScaleDrawer(elements, pageWidth, mapName);
        // 创建制图单位对象
        layoutsDrawer.madeUnitDrawer(elements, pageWidth);
        // 创建制图时间对象
        layoutsDrawer.madeTimeDrawer(elements, info.getMakeTime(), pageWidth);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> outputImages(DrawersRainInfoBO infoBO) {

        // 处理多层级文件夹名称
        int version = Integer.parseInt(StringUtils.substring(infoBO.getRainQueueId(), infoBO.getRainQueueId().length() - 2));
        String rainId = StringUtils.substring(infoBO.getRainQueueId(), 0, infoBO.getRainQueueId().length() - 2);
        // 路径格式：/upload/专题图/rainId/批次/ XX图 +".jpg"
        String folderPath = BaseConstants.STORM_PICTURE_PREFIX + rainId + "/" + version + "/";
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

    private DrawersRainInfoBO buildDrawersRainInfoBO(RainAssessmentDTO dto, int index) {
        DrawersRainInfoBO info = new DrawersRainInfoBO();
        // 设置标题、暴雨时间、暴雨地点、持续时间、制作时间
        String title = dto.getPosition() + BaseConstants.XIAN_STORM_MAPS[index];
        String makeTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        String rainTime = dto.getOccurrenceTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分"));
        info.setPicName(BaseConstants.XIAN_STORM_MAPS[index]);
        info.setTitle(title);
        info.setRainfall(dto.getRainfall());
        info.setDuration(dto.getDuration());
        info.setRainAddr(dto.getPosition());
        info.setRainTime(rainTime);
        info.setMakeTime(makeTime);
        info.setLayoutId(index);    // 设置布局Id
        info.setRainQueueId(dto.getRainQueueId());

        return info;
    }


}
