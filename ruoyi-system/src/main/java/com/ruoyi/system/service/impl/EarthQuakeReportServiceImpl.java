package com.ruoyi.system.service.impl;

import com.ruoyi.common.config.DocumentConfig;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.ImagePositionEnum;
import com.ruoyi.common.enums.ImageTypeEnum;
import com.ruoyi.common.utils.file.DocumentUtils;
import com.ruoyi.system.domain.EarthQuakeReportEntity;
import com.ruoyi.system.domain.dto.TriggerDTO;
import com.ruoyi.system.service.IEarthQuakeService;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class EarthQuakeReportServiceImpl implements IEarthQuakeService {

    // word保存路径
    @Value("${document.path.rain.report}")
    private String wordPath;

    //生成报告
    @Override
    public R<String> generateEarthQuakeReport(TriggerDTO triggerDTO) throws IOException {
        // 获取报告数据
        EarthQuakeReportEntity earthQuakeReportEntity = new EarthQuakeReportEntity();

        // 生成 Word 路径
        Path wordDir = Paths.get(wordPath);
        if (!Files.exists(wordDir)) {
            Files.createDirectories(wordDir);
        }
        String wordName = "report_" + System.currentTimeMillis() + ".docx";
        String wordPath = wordDir.resolve(wordName).toAbsolutePath().toString();

        // 表头宽度
        // int[] widths = {1500, 3000, 8000, 3000};
        try {
            new CreateEarthQuakeReport().createEarthQuakeReport(wordPath, earthQuakeReportEntity);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return R.ok(wordName);
    }

//    public static void main(String[] args) {
//        try {
//            EarthQuakeReportEntity earthQuakeReportEntity = new EarthQuakeReportEntity();
//            earthQuakeReportEntity.setReportTime("08月30日16时24分");
//            earthQuakeReportEntity.setEarthQuakeTime("2025年08月30日17时20分");
//            earthQuakeReportEntity.setEarthQuakeLat("99");
//            earthQuakeReportEntity.setEarthQuakeLon("109");
//            earthQuakeReportEntity.setEarthQuakeMagnitude("7");
//            earthQuakeReportEntity.setEarthQuakePosition("陕西省西安市长安区");
//            earthQuakeReportEntity.setEarthQuakeSourceDepth("10km");
//
//            double flg = Math.random() * 50000;
//
//            earthQuakeReportEntity.setEarthQuakeCountry("长安区");
//            earthQuakeReportEntity.setEarthQuakePopulationDensity(flg + "");
//            earthQuakeReportEntity.setEarthQuakeIntensity("9");
//            earthQuakeReportEntity.setEarthQuakeDisasterArea("50");
//            earthQuakeReportEntity.setEarthQuakeSumGDP("4");
//            earthQuakeReportEntity.setEarthQuakeInfluencePopulation(flg * 0.8 + "");
//            earthQuakeReportEntity.setEarthQuakeDeath(flg * 0.4 + "");
//            earthQuakeReportEntity.setEarthQuakeFaultZone("饶峰-麻柳坝断裂");
//
//            earthQuakeReportEntity.setEarthQuakeEmergencyLevel("一级");
//
//            // 创建表格数据
//            for (int j = 0; j < 5; j++) {
//                EarthQuakeReportEntity.Hospital hospital = new EarthQuakeReportEntity().new Hospital();
//                hospital.setHospitalName("西安市第一医院");
//                hospital.setHospitalBeds(Math.random() * 100 + "%");
//                hospital.setHospitalAddress("陕西省西安市碑林区南院门街道南大街粉巷30号");
//                hospital.setHospitalLevel("甲级");
//                earthQuakeReportEntity.getEarthQuakeHospital().add(hospital);
//            }
//
//            new CreateEarthQuakeReport().createEarthQuakeReport("H:/test.docx", earthQuakeReportEntity);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //下载报告
    @Override
    public void downloadReport(String fileName, HttpServletResponse resp) throws IOException {
        Path file = Paths.get(wordPath).resolve(fileName).normalize();

        System.out.println("尝试下载文件: {}" + file.toString());
        System.out.println("文件是否存在: {}" + Files.exists(file));

        if (!Files.exists(file)) {
            System.out.println("文件不存在: {}" + file.toString());
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("文件不存在: " + fileName);
            return;
        }

        // 添加CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "*");
        resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8"));

        try {
            Files.copy(file, resp.getOutputStream());
            resp.flushBuffer();
        } catch (IOException e) {
            throw e;
        }
    }
}

/**
 * word文档生成
 */
class CreateEarthQuakeReport {
    private static final Integer TEXT_FONT_SIZE = 16;
    private static final String TEXT_FONT_FAMILY = "黑体";
    // 两个字符
    private static final Integer INDENTATION_DISTANCE = 40 * TEXT_FONT_SIZE;
    private static final String FONT_FANG_SONG = "仿宋_GB2312";
    private static final int TABLE_ROW_HEIGHT = 567;
    private static final int TABLE_FONT_SIZE = 12;

    /**
     * 创建word文件
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
            createDept(document, "西安市应急管理局               " + earthQuakeReportEntity.getReportTime());
            // 两个空行
            DocumentUtils.createBlankLine(document, 2);
            // 第一部分，地震概况
            createEarthQuakeOverview(document, earthQuakeReportEntity);
            // 第二部分，风险评估
            createRiskAssessment(document, earthQuakeReportEntity);
            // 第三部分，应急处置建议
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

        String content = String.format("据地震台网测定，%s（北京时间）在%s（北纬%s，东经%s）发生%s级地震, 震源深度%s千米。",
                earthQuakeReportEntity.getEarthQuakeTime(),
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

        String content1 = String.format("本次地震震中所在地区%s，人口密度为%s每平方公里。" +
                        "本次地震重灾区烈度预计达到%s度，重灾区面积为%s平方公里，灾区总GDP为%s亿元；地震影响人口约%s万人，预计伤亡人数%s人。",
                earthQuakeReportEntity.getEarthQuakeCountry(),
                earthQuakeReportEntity.getEarthQuakePopulationDensity(),
                earthQuakeReportEntity.getEarthQuakeIntensity(),
                earthQuakeReportEntity.getEarthQuakeDisasterArea(),
                earthQuakeReportEntity.getEarthQuakeSumGDP(),
                earthQuakeReportEntity.getEarthQuakeInfluencePopulation(),
                earthQuakeReportEntity.getEarthQuakeDeath()
        );

        DocumentUtils.addRegularRun(paragraph1, content1);
        DocumentUtils.insertImageWithCaption(doc,
                earthQuakeReportEntity.getEarthQuakeInfluenceGraph(),
//                "http://t1arte4v9.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E9%99%84%E8%BF%91%E5%8C%BB%E7%96%97%E6%9C%BA%E6%9E%84%E5%88%86%E5%B8%83%E5%9B%BE?e=1755938687&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:i6Ni-UdI8wmPLErW4fK8aLYLbEo=",
                ImageTypeEnum.JPG ,null, null, "图1", ImagePositionEnum.AFTER);

        XWPFParagraph xwpfParagraph = DocumentUtils.addRegularParagraph(doc, null);
        XWPFRun run = DocumentUtils.addRegularRun(xwpfParagraph, "1.震中附近活动断裂分布");
        run.setFontSize(DocumentConfig.FONT_SIZE_FOUR);
        run.setBold(true);

        String cotent2 = String.format("震中距离震中最近的断裂是%s，震中附近活动断裂分布如图所示。",
                earthQuakeReportEntity.getEarthQuakeFaultZone());
        xwpfParagraph = DocumentUtils.addRegularParagraph(doc, cotent2);
        xwpfParagraph.setIndentationFirstLine(0);

        DocumentUtils.insertImageWithCaption(doc,
                earthQuakeReportEntity.getEarthQuakeFaultZoneGraph(),
//                "http://t1arte4v9.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E9%99%84%E8%BF%91%E5%8C%BB%E7%96%97%E6%9C%BA%E6%9E%84%E5%88%86%E5%B8%83%E5%9B%BE?e=1755938687&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:i6Ni-UdI8wmPLErW4fK8aLYLbEo=",
                ImageTypeEnum.JPG, null, null, "图2", ImagePositionEnum.AFTER);

        XWPFParagraph xwpfParagraph1 = DocumentUtils.addRegularParagraph(doc, null);
        XWPFRun run1 = DocumentUtils.addRegularRun(xwpfParagraph1, "2.震中附近医院分布");
        run1.setFontSize(DocumentConfig.FONT_SIZE_FOUR);
        run1.setBold(true);

        DocumentUtils.insertImageWithCaption(doc,
                earthQuakeReportEntity.getEarthQuakeHospitalGraph(),
//                "http://t1arte4v9.hb-bkt.clouddn.com/T2024060117164151180001_%E9%9C%87%E5%8C%BA%E9%99%84%E8%BF%91%E5%8C%BB%E7%96%97%E6%9C%BA%E6%9E%84%E5%88%86%E5%B8%83%E5%9B%BE?e=1755938687&token=mheaTe3xRCkChSjwfueGYzB32yi7yk2sj8pemjvF:i6Ni-UdI8wmPLErW4fK8aLYLbEo=",
                ImageTypeEnum.JPG, null, null, "图3", ImagePositionEnum.AFTER);


        // 设置表格标题
        XWPFParagraph tableTitleParagraph = doc.createParagraph();
        tableTitleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun tableTitleRun = tableTitleParagraph.createRun();

        tableTitleRun.setText("50公里内医院统计表");
        tableTitleRun.setFontFamily(FONT_FANG_SONG);
        tableTitleRun.setFontSize(DocumentConfig.FONT_SIZE_SMALL_FOUR);

        // 生成表格
        createTable(doc, earthQuakeReportEntity,
                new String[]{ "序号", "医院名称", "地址", "医院等级", "总床位"});
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
        int rows = earthQuakeReportEntity.getEarthQuakeHospital().size();
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

            EarthQuakeReportEntity.Hospital hospitalData =
                    earthQuakeReportEntity.getEarthQuakeHospital().get(i - 1);

            // 序号列
            setupTableCell(bodyRow.getCell(0), String.valueOf(i), false);
            // 医院名称列
            setupTableCell(bodyRow.getCell(1), hospitalData.getHospitalName(), true);
            // 位置列
            setupTableCell(bodyRow.getCell(2), hospitalData.getHospitalAddress(), false);
            // 等级列
            setupTableCell(bodyRow.getCell(3), hospitalData.getHospitalLevel(), false);
            // 床位列
            setupTableCell(bodyRow.getCell(4), hospitalData.getHospitalBeds(), false);
        }
    }

    /**
     * 设置表格单元格内容
     *
     * @param cell 表格单元格
     * @param text 单元格文本
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
        DocumentUtils.addRegularParagraph(doc, "三、应急处置建议");

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
