package com.ruoyi.system.service.impl;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.config.DocumentConfig;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.common.enums.ImagePositionEnum;
import com.ruoyi.common.enums.ImageTypeEnum;
import com.ruoyi.common.exception.ThematicReceiveException;
import com.ruoyi.common.utils.file.DocumentUtils;
import com.ruoyi.system.core.rabbitmq.RabbitConfig;
import com.ruoyi.system.domain.AssessmentOutput;
import com.ruoyi.system.domain.EarthQuakeReportEntity;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;
import com.ruoyi.system.mapper.AssessmentOutputMapper;
import com.ruoyi.system.service.IEarthQuakeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EarthQuakeReportServiceImpl implements IEarthQuakeService {


    @Resource
    private RabbitTemplate rabbitTemplate;
    private static final String FONT_FANG_SONG = "仿宋_GB2312";
    private static final int TABLE_ROW_HEIGHT = 567;
    private static final int TABLE_FONT_SIZE = 12;

    //生成报告
    @Override
    public R<String> generateEarthQuakeReport(EarthQuakeReportEntity earthQuakeReportEntity) throws IOException {

        // 生成 Word 路径
        Path wordDir = Paths.get(BaseConstants.REPORTS_PREFIX);
        if (!Files.exists(wordDir)) {
            Files.createDirectories(wordDir);
        }
        //earthQuakeReportEntity.getEarthQuakeTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+"地震评估报告"
        String wordName = earthQuakeReportEntity.getEarthQuakeTime().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + System.currentTimeMillis() + "地震评估报告.docx";
        String wordPath = wordDir.resolve(wordName).toAbsolutePath().toString();

        // 表头宽度
        // int[] widths = {1500, 3000, 8000, 3000};
        try {
            createEarthQuakeReport(wordPath, earthQuakeReportEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("开始将报告路径插入数据库...");
        saveEarthQuakeReport(wordPath, wordName, earthQuakeReportEntity.getEqId(), earthQuakeReportEntity.getEqqueueId());

        return R.ok(wordName);
    }

    @DataSource(value = DataSourceType.MASTER)
    public void saveEarthQuakeReport(String wordPath, String wordName, String eqId, String eqqueueId) {
        try {

            // 设置图件产出信息
            AssessmentOutputDTO outputDTO = AssessmentOutputDTO.builder()
                    .eqId(eqId)
                    .eqqueueId(eqqueueId)
                    .fileName(wordName)
                    .fileType(BaseConstants.WORD_TYPE)
                    .fileExtension(BaseConstants.REPORTS_EXTENSION_TYPE)
                    .type(BaseConstants.DOCUMENT_TYPE)
                    .localSourceFile(wordPath)
                    .build();

            rabbitTemplate.convertAndSend(RabbitConfig.DISASTER_EXCHANGE, RabbitConfig.DISASTER_REPORT, outputDTO);

        } catch (Exception ex) {
            ex.printStackTrace();
            // 抛出异常
            throw new ThematicReceiveException(BaseConstants.THEMATIC_MAP_ERROR);
        }
    }


    /**
     * 创建word文件
     *
     * @param filePath
     * @param earthQuakeReportEntity
     * @throws IOException
     */
    public void createEarthQuakeReport(String filePath, EarthQuakeReportEntity earthQuakeReportEntity) throws IOException {
        // 创建一个新的Word文档
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(filePath)) {
            // 创建保密提示
            createConfidentialityTip(document, "对内掌握");
            // 三个空行
            DocumentUtils.createBlankLine(document, 3);
            // 创建标题
            DocumentUtils.createTitle(document, "地震应急预评估报告");
            // 一个空行
            DocumentUtils.createBlankLine(document, 1);
            // 创建部门信息
            createDept(document, "西安市应急管理局               " + earthQuakeReportEntity.getEarthQuakeTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            // 两个空行
            DocumentUtils.createBlankLine(document, 2);
            // 第一部分，地震概况
            createEarthQuakeOverview(document, earthQuakeReportEntity);
            // 第二部分，风险评估
            createRiskAssessment(document, earthQuakeReportEntity);
            // 第三部分，救援需求
            createRescueNeed(document, earthQuakeReportEntity);
            // 第四部分，应急处置建议
            createEmergencyResponseSuggestions(document, earthQuakeReportEntity);
            // 保存文档
            document.write(out);
        }
    }

    /**
     * 创建保密提示
     *
     * @param doc - 文档对象
     * @param tip - 提示文本
     */
    private void createConfidentialityTip(XWPFDocument doc, String tip) {
        XWPFParagraph paragraph = DocumentUtils.addRegularParagraph(doc, tip);
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
    }

    /**
     * 创建部门信息
     *
     * @param doc
     * @param dept
     */
    private void createDept(XWPFDocument doc, String dept) {
        XWPFParagraph paragraph = DocumentUtils.addRegularParagraph(doc, null);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setFirstLineIndent(0);

        XWPFRun run = DocumentUtils.addRegularRun(paragraph, dept);
        run.setFontFamily(DocumentConfig.FONT_FANG_SONG_GB2312);

        // 添加边框线
        DocumentUtils.addParagraphBorderLine(paragraph, null);
    }

    /**
     * 创建降雨概述部分
     *
     * @param doc
     * @param earthQuakeReportEntity
     */
    private void createEarthQuakeOverview(XWPFDocument doc, EarthQuakeReportEntity earthQuakeReportEntity) {
        DocumentUtils.addRegularParagraph(doc, "一、地震概况");
        // 内容
        XWPFParagraph paragraph = DocumentUtils.addRegularParagraph(doc, null);


        String content = String.format("据地震台网测定，%s（北京时间）在%s（北纬%s，东经%s）发生%.1f级地震, 震源深度%.1f千米。",
                earthQuakeReportEntity.getEarthQuakeTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时mm分ss秒")),
                earthQuakeReportEntity.getEarthQuakePosition(),
                earthQuakeReportEntity.getEarthQuakeLon(),
                earthQuakeReportEntity.getEarthQuakeLat(),
                earthQuakeReportEntity.getEarthQuakeMagnitude(),
                earthQuakeReportEntity.getEarthQuakeSourceDepth()
        );
        DocumentUtils.addRegularRun(paragraph, content);
    }

    /**
     * 创建风险评估
     *
     * @param doc
     * @param earthQuakeReportEntity
     */
    private void createRiskAssessment(XWPFDocument doc, EarthQuakeReportEntity earthQuakeReportEntity) {
        DocumentUtils.addRegularParagraph(doc, "二、风险评估");

        // 内容
        XWPFParagraph paragraph1 = DocumentUtils.addRegularParagraph(doc, null);

        int roundedResult = (int) Math.round(Double.parseDouble(earthQuakeReportEntity.getEarthQuakeDisasterArea()) / 1000000.0);
        int Area = roundedResult * ((int)earthQuakeReportEntity.getEarthQuakeSourceDepth()+3);
        int roundedToTen = (int) (Math.round(Area / 10.0) * 10);

        int deathMax = Integer.parseInt(earthQuakeReportEntity.getEarthQuakeDeathMax());
        if (deathMax==0){
            String content1 = String.format("本次地震震中所在地区%s。" + "地震影响人口约%s-%s人。",
                    earthQuakeReportEntity.getEarthQuakePosition(),
                    earthQuakeReportEntity.getEarthQuakeInfluencePopulationMin(),
                    earthQuakeReportEntity.getEarthQuakeInfluencePopulationMax()
            );
            DocumentUtils.addRegularRun(paragraph1, content1);

        } else {
            String content1 = String.format("本次地震震中所在地区%s。" +
                            "本次地震重灾区烈度预计超过%d度，重灾区面积约%d平方公里；地震影响人口约%s-%s人，预计伤亡人数约%s-%s人。",
                    earthQuakeReportEntity.getEarthQuakePosition(),
                    (int)(earthQuakeReportEntity.getEarthQuakeMagnitude() + 2),
                    roundedToTen,
                    earthQuakeReportEntity.getEarthQuakeInfluencePopulationMin(),
                    earthQuakeReportEntity.getEarthQuakeInfluencePopulationMax(),
                    earthQuakeReportEntity.getEarthQuakeDeathMin(),
                    earthQuakeReportEntity.getEarthQuakeDeathMax()
            );
            DocumentUtils.addRegularRun(paragraph1, content1);
        }
        DocumentUtils.insertImageWithCaption(doc,
                earthQuakeReportEntity.getEarthQuakeInfluenceGraph(),
//                "http://sv25gsrnh.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E4%BA%A4%E9%80%9A%E5%9B%BE?e=1755938078&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:vDr49kWfxDngsOQRyi92MGCVxS0=",
                ImageTypeEnum.JPG ,null, null, "图1：影响估计范围分布图", ImagePositionEnum.AFTER);

        XWPFParagraph xwpfParagraph = DocumentUtils.addRegularParagraph(doc, null);
        XWPFRun run = DocumentUtils.addRegularRun(xwpfParagraph, "1.震中附近活动断裂分布");
        run.setFontSize(DocumentConfig.FONT_SIZE_FOUR);
        run.setBold(true);

        String cotent2 = String.format("震中距离震中最近的断裂是%s，震中附近活动断裂分布如图所示。",
                (earthQuakeReportEntity.getEarthQuakeFaultZone() == null || earthQuakeReportEntity.getEarthQuakeFaultZone().trim().isEmpty())
                        ? "临潼-长安断裂带"
                        : earthQuakeReportEntity.getEarthQuakeFaultZone());
        xwpfParagraph = DocumentUtils.addRegularParagraph(doc, cotent2);
        xwpfParagraph.setIndentationFirstLine(0);

        DocumentUtils.insertImageWithCaption(doc,
                earthQuakeReportEntity.getEarthQuakeFaultZoneGraph(),
//                "http://sv25gsrnh.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E4%BA%A4%E9%80%9A%E5%9B%BE?e=1755938078&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:vDr49kWfxDngsOQRyi92MGCVxS0=",
                ImageTypeEnum.JPG, null, null, "图2：震中附近活动断裂分布", ImagePositionEnum.AFTER);

        XWPFParagraph xwpfParagraph1 = DocumentUtils.addRegularParagraph(doc, null);
        XWPFRun run1 = DocumentUtils.addRegularRun(xwpfParagraph1, "2.震中附近医院分布");
        run1.setFontSize(DocumentConfig.FONT_SIZE_FOUR);
        run1.setBold(true);

        if(!earthQuakeReportEntity.getEarthQuakeHospital().isEmpty()){
            String cotent3 = String.format("八度区内的医院有%d个，其中一级及以上医院的相关信息如表格所示。",
                    earthQuakeReportEntity.getEarthQuakeHospital().size()
            );
            xwpfParagraph = DocumentUtils.addRegularParagraph(doc, cotent3);
            xwpfParagraph.setIndentationFirstLine(0);

            DocumentUtils.insertImageWithCaption(doc,
                    earthQuakeReportEntity.getEarthQuakeHospitalGraph(),
//                    "http://sv25gsrnh.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E4%BA%A4%E9%80%9A%E5%9B%BE?e=1755938078&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:vDr49kWfxDngsOQRyi92MGCVxS0=",
                    ImageTypeEnum.JPG, null, null, "图3：震中附近医院分布", ImagePositionEnum.AFTER);


            // 设置表格标题
            XWPFParagraph tableTitleParagraph = doc.createParagraph();
            tableTitleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun tableTitleRun = tableTitleParagraph.createRun();

            tableTitleRun.setText("表1：八度区内医院统计表");
            tableTitleRun.setFontFamily(FONT_FANG_SONG);
            tableTitleRun.setFontSize(DocumentConfig.FONT_SIZE_SMALL_FOUR);

            // 生成表格
            createTable(doc, earthQuakeReportEntity,
                    new String[]{ "序号", "医院名称", "地址", "医院等级", "总床位"});
        }

    }

    /**
     * 创建救援需求
     *
     * @param doc
     * @param earthQuakeReportEntity
     */
    private void createRescueNeed(XWPFDocument doc, EarthQuakeReportEntity earthQuakeReportEntity){
        DocumentUtils.addRegularParagraph(doc, "三、救援需求");
        // 内容
        XWPFParagraph xwpfParagraph = DocumentUtils.addRegularParagraph(doc, null);

        String content3 = String.format("建议提前组织救援人员，开展救援。经系统计算，距离震中附件100公里范围内救援队伍有%d个如表2所示，震中附件救援队伍分布如图4所示。",
                earthQuakeReportEntity.getEarthQuakeFireFighter().size()
        );
        xwpfParagraph = DocumentUtils.addRegularParagraph(doc, content3);
        xwpfParagraph.setIndentationFirstLine(0);

        // 设置表格标题
        XWPFParagraph tableTitleParagraph = doc.createParagraph();
        tableTitleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun tableTitleRun = tableTitleParagraph.createRun();

        tableTitleRun.setText("表2：救援队伍信息表");
        tableTitleRun.setFontFamily(FONT_FANG_SONG);
        tableTitleRun.setFontSize(DocumentConfig.FONT_SIZE_SMALL_FOUR);

        // 生成表格
        createTableRescueTeam(doc, earthQuakeReportEntity,
                new String[]{ "序号", "队伍名称", "队伍类型", "详细地址", "总人数"});

        DocumentUtils.insertImageWithCaption(doc,
                earthQuakeReportEntity.getEarthQuakeFireFighterGraph(),
//                "http://sv25gsrnh.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E4%BA%A4%E9%80%9A%E5%9B%BE?e=1755938078&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:vDr49kWfxDngsOQRyi92MGCVxS0=",
                ImageTypeEnum.JPG, null, null, "图4：震中附近救援队伍分布", ImagePositionEnum.AFTER);

        String content4 = String.format("为快速安置灾情，建议紧急调集救援物资。经系统计算，距离震中附件100公里范围内救援物资有%d个如表3所示，震中附件救援物资分布如图5所示。",
                earthQuakeReportEntity.getEarthQuakeStorePoint().size()
        );
        xwpfParagraph = DocumentUtils.addRegularParagraph(doc, content4);
        xwpfParagraph.setIndentationFirstLine(0);
        // 设置表格标题
        XWPFParagraph tableTitleParagraph1 = doc.createParagraph();
        tableTitleParagraph1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun tableTitleRun1 = tableTitleParagraph1.createRun();

        tableTitleRun1.setText("表3：救援物资信息表");
        tableTitleRun1.setFontFamily(FONT_FANG_SONG);
        tableTitleRun1.setFontSize(DocumentConfig.FONT_SIZE_SMALL_FOUR);

        // 生成表格
        createTableRescueMaterials(doc, earthQuakeReportEntity,
                new String[]{ "序号", "储备库名称", "详细地址", "所属部门", "有效库容"});

        DocumentUtils.insertImageWithCaption(doc,
                earthQuakeReportEntity.getEarthQuakeStorePointGraph(),
//                "http://sv25gsrnh.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E4%BA%A4%E9%80%9A%E5%9B%BE?e=1755938078&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:vDr49kWfxDngsOQRyi92MGCVxS0=",
                ImageTypeEnum.JPG, null, null, "图5：震中附近救援物资分布", ImagePositionEnum.AFTER);
    }

    /**
     * 设置救援队伍表格数据
     *
     * @param doc
     * @param earthQuakeReportEntity
     * @param headers
     */
    private void createTableRescueTeam(XWPFDocument doc,EarthQuakeReportEntity earthQuakeReportEntity, String[] headers){
        log.info("生成救援队伍表格信息");

        List<EarthQuakeReportEntity.FireFighter> fireFighters = earthQuakeReportEntity.getEarthQuakeFireFighter();
        int rows = fireFighters.size() + 1;
        int cols = headers.length;
        XWPFTable table = doc.createTable(rows, cols);

        // 设置表格宽度
        table.setWidth("100%");

        // 设置表头
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.setHeight(TABLE_ROW_HEIGHT);

        for (int i = 0; i < cols; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            if (cell == null) {
                cell = headerRow.createCell();
            }
            setupTableCell(cell, headers[i], true);
        }

        // 设置表格数据行
        for (int i = 1; i < rows; i++) {
            XWPFTableRow bodyRow = table.getRow(i);
            bodyRow.setHeight(TABLE_ROW_HEIGHT);

            EarthQuakeReportEntity.FireFighter fireFighterData = fireFighters.get(i - 1);

            // 序号列
            setupTableCell(bodyRow.getCell(0), String.valueOf(i), false);
            // 救援队伍名称列
            setupTableCell(bodyRow.getCell(1), fireFighterData.getFireFighterName(), true);
            // 队伍类型列
            setupTableCell(bodyRow.getCell(2), fireFighterData.getFireFighterType(), false);
            // 详细地址列
            setupTableCell(bodyRow.getCell(3), fireFighterData.getFireFighterAddress(), false);
            // 总人数列
            setupTableCell(bodyRow.getCell(4), fireFighterData.getFireFighterNum(), false);
        }
    }

    /**
     * 设置救援物资表格数据
     *
     * @param doc
     * @param earthQuakeReportEntity
     * @param headers
     */
    private void createTableRescueMaterials(XWPFDocument doc,EarthQuakeReportEntity earthQuakeReportEntity, String[] headers){
        log.info("生成救援物资表格信息");

        List<EarthQuakeReportEntity.StorePoint> storePoints = earthQuakeReportEntity.getEarthQuakeStorePoint();
        int rows = storePoints.size() + 1;
        int cols = headers.length;
        XWPFTable table = doc.createTable(rows, cols);

        // 设置表格宽度
        table.setWidth("100%");

        // 设置表头
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.setHeight(TABLE_ROW_HEIGHT);

        for (int i = 0; i < cols; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            if (cell == null) {
                cell = headerRow.createCell();
            }
            setupTableCell(cell, headers[i], true);
        }

        // 设置表格数据行
        for (int i = 1; i < rows; i++) {
            XWPFTableRow bodyRow = table.getRow(i);
            bodyRow.setHeight(TABLE_ROW_HEIGHT);

            EarthQuakeReportEntity.StorePoint storePointData = storePoints.get(i - 1);

            // 序号列
            setupTableCell(bodyRow.getCell(0), String.valueOf(i), false);
            // 救援物资名称列
            setupTableCell(bodyRow.getCell(1), storePointData.getStorePointName(), true);
            // 详细地址列
            setupTableCell(bodyRow.getCell(2), storePointData.getStorePointAddress(), false);
            // 所属部门列
            setupTableCell(bodyRow.getCell(3), storePointData.getStorePointDep(), false);
            // 有效库容列
            setupTableCell(bodyRow.getCell(4), storePointData.getStorePointNum(), false);
        }
    }

    /**
     * 设置表格数据
     *
     * @param doc
     * @param earthQuakeReportEntity
     * @param headers
     */
    private void createTable(XWPFDocument doc,
                                               EarthQuakeReportEntity earthQuakeReportEntity, String[] headers) {
        log.info("生成医院表格数据");
        // 先筛选符合条件的医院数据（非未定等和非未定级）
        List<EarthQuakeReportEntity.Hospital> filteredHospitals = earthQuakeReportEntity.getEarthQuakeHospital().stream()
                .filter(hospital -> {
                    String levelStr = hospital.getHospitalLevel();
                    // 解析字符串为列表
                    List<String> levels = parseLevelsFromString(levelStr);
                    // 检查是否包含需要排除的等级
                    if (levels.isEmpty() || levels.size() < 2) {
                        return false;
                    }
                    return !(levels.get(0).contains("未定等") || levels.get(0).contains("未定级") || levels.get(1).contains("未定级") || levels.get(1).contains("未定等"));
                })
                .collect(Collectors.toList());



        int rows = filteredHospitals.size() + 1;
        int cols = headers.length;
        XWPFTable table = doc.createTable(rows, cols);

        // 设置表格宽度
        table.setWidth("100%");

        // 设置表头
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.setHeight(TABLE_ROW_HEIGHT);

        for (int i = 0; i < cols; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            if (cell == null) {
                cell = headerRow.createCell();
            }
            setupTableCell(cell, headers[i], true);
        }

        // 设置表格数据行
        for (int i = 1; i < rows; i++) {
            XWPFTableRow bodyRow = table.getRow(i);
            bodyRow.setHeight(TABLE_ROW_HEIGHT);

            EarthQuakeReportEntity.Hospital hospitalData = filteredHospitals.get(i - 1);

            // 处理医院等级字符串，转换为逗号分隔的字符串
            String levelStr = hospitalData.getHospitalLevel();
            List<String> levelList = parseLevelsFromString(levelStr);
            String displayLevel = "";
            if (levelList != null && !levelList.isEmpty()) {
                displayLevel = String.join("", levelList);
            }

            // 序号列
            setupTableCell(bodyRow.getCell(0), String.valueOf(i), false);
            // 医院名称列
            setupTableCell(bodyRow.getCell(1), hospitalData.getHospitalName(), true);
            // 位置列
            setupTableCell(bodyRow.getCell(2), hospitalData.getHospitalAddress(), false);
            // 等级列
            setupTableCell(bodyRow.getCell(3), displayLevel, false);
            // 床位列
            setupTableCell(bodyRow.getCell(4), hospitalData.getHospitalBeds(), false);
        }
    }

    /**
     * 解析医院等级字符串
     * @param levelStr
     * @return
     */
    private List<String> parseLevelsFromString(String levelStr) {
        if (levelStr == null || levelStr.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 简单处理：去除前后的[]和引号，再按逗号分割
        try {
            String cleaned = levelStr.trim().replaceAll("^\\[|\\]$", "") // 去除前后的[]
                    .replaceAll("\"", ""); // 去除引号
            if (cleaned.isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.asList(cleaned.split(","));
        } catch (Exception e) {
            // 解析失败时返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 设置表格单元格内容
     *
     * @param cell     表格单元格
     * @param text     单元格文本
     * @param isHeader 是否为表头单元格
     */
    private void setupTableCell(XWPFTableCell cell, String text, boolean isHeader) {
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

        // 清除单元格中的所有现有段落
        for (int k = cell.getParagraphs().size() - 1; k >= 0; k--) {
            cell.removeParagraph(k);
        }

        // 创建段落
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingAfter(0);

        // 创建文本对象
        XWPFRun run = paragraph.createRun();
        run.setText(text);

        if (isHeader) {
            run.setBold(true); // 设置加粗
        }

        run.setFontFamily(FONT_FANG_SONG);
        run.setFontSize(TABLE_FONT_SIZE);
    }

    /**
     * 应急处置建议
     *
     * @param doc
     * @param earthQuakeReportEntity
     */
    private void createEmergencyResponseSuggestions(XWPFDocument doc, EarthQuakeReportEntity earthQuakeReportEntity) {
        DocumentUtils.addRegularParagraph(doc, "四、应急处置建议");

        // 段落内容数组
        String[] contents = {
                String.format("本次地震初步判定本次地震事件属于较大地震灾害，建议启动%s级地震应急响应。",
                        earthQuakeReportEntity.getEarthQuakeEmergencyLevel()),
                String.format("目前灾区灾情信息未知，据初步估计本次地震极震区为%1$s度。" +
                                "建议由市抗震救灾指挥部提出%1$s级应急响应建议，经市委、市政府同意，由市政府宣布启动地震%1$s级应急响应。"
                                + "在国务院、省抗震救灾指挥部的统一领导、指挥和协调下，市抗震救灾指挥部负责组织实施全市抗震救灾工作。",
                        earthQuakeReportEntity.getEarthQuakeEmergencyLevel()),
                "先期处置：",
                "（1）市防震减灾服务中心迅速对地震影响进行评估，通报有关领导和部门,并立即向市应急管理局报告震情、预估烈度、灾情初步判断意见，提出抗震救灾工作建议。",
                "（2）市经济和信息化局、市教育局、市公安局、市自然资源和规划局、市生态环境局、市住房和城乡建设局、市交通运输局、市水利局、市卫生健康委、市通信发展办、国网雅电集团等按照职责及时收集灾情信息通报市应急管理局。",
                "（3）市应急管理局迅速汇总灾情，组织开展灾情评估，并按规定向市委、市政府、市应急委、应急厅报告，提出应急响应建议。集结抢险救援队伍，调集本地救灾物资，组织支援力量进入备战状态。",
                "（4）市抗震救灾指挥部成员单位迅速按照职能分工和应急预案规定进行先期处置，及时向市委、市政府、市应急委、市抗震救灾指挥部报告工作进展，相关单位负责同志立即前往市应急管理局参加紧急会议。",
                "处置措施建议：",
                "人员安置方面，应优先选择地势较高、远离河道且具备较好排水条件的安全区域，村内学校、村委会等公共设施也可作为临时安置点，具备一定容纳能力和生活配套条件。对于本地安置条件受限的村组，可组织跨区域转移，安排至周边安全城镇，利用当地酒店、学校等资源保障群众基本生活与应急避险需求。",
                "救援队伍准备方面：建议提前联系消防、交通和医疗三类专业救援力量：",
                "消防救援队准备生命探测设备、环境探测设备和破拆工具等，可以及时定位被困人员，评估现场环境。",
                "交通救援队预置挖掘机、装载机和道路抢修车等，开辟抗震救灾绿色通道。",
                "医疗救援队组织具备外科、内科、急救能力的医疗人员，配备心肺复苏仪、骨折固定夹板、担架等装备及绷带、消炎药、止痛药等应急药物。",
                "救援物资准备方面，应根据预测受灾人口数量，提前调配各类救援物资。"
        };

        // 创建所有段落
        for (String content : contents) {
            DocumentUtils.addRegularParagraph(doc, content);
        }
    }

}


