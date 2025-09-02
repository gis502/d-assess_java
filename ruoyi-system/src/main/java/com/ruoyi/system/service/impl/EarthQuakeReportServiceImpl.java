package com.ruoyi.system.service.impl;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.domain.EarthQuakeReportEntity;
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
    public R<String> generateEarthQuakeReport(Integer disasterId) throws IOException {
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
            new CreateRainReport().createEarthQuakeReport(wordPath, earthQuakeReportEntity);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return R.ok(wordName);
    }

    public static void main(String[] args) {
        try {
            EarthQuakeReportEntity earthQuakeReportEntity = new EarthQuakeReportEntity();
            earthQuakeReportEntity.setReportTime("08月30日16时24分");
            earthQuakeReportEntity.setEarthQuakeTime("2025年08月30日17时20分");
            earthQuakeReportEntity.setEarthQuakeLat("99");
            earthQuakeReportEntity.setEarthQuakeLon("109");
            earthQuakeReportEntity.setEarthQuakeMagnitude("7");
            earthQuakeReportEntity.setEarthQuakePosition("陕西省西安市长安区");
            earthQuakeReportEntity.setEarthQuakeSourceDepth("10km");

            double flg = Math.random() * 50000;

            earthQuakeReportEntity.setEarthQuakeCountry("长安区");
            earthQuakeReportEntity.setEarthQuakePopulationDensity(flg + "/km²");
            earthQuakeReportEntity.setEarthQuakeIntensity("9");
            earthQuakeReportEntity.setEarthQuakeDisasterArea("50km²");
            earthQuakeReportEntity.setEarthQuakeSumGDP("4");
            earthQuakeReportEntity.setEarthQuakeInfluencePopulation(flg * 0.8 + "");
            earthQuakeReportEntity.setEarthQuakeDeath(flg * 0.4 + "");
            earthQuakeReportEntity.setEarthQuakeFaultZone("饶峰-麻柳坝断裂");

            earthQuakeReportEntity.setEarthQuakeEmergencyLevel("一级");

            // 创建表格数据
            for (int j = 0; j < 5; j++) {
                EarthQuakeReportEntity.Hospital hospital = new EarthQuakeReportEntity().new Hospital();
                hospital.setHospitalName("西安市第一医院");
                hospital.setHospitalBeds(Math.random() * 100 + "%");
                hospital.setHospitalAddress("陕西省西安市碑林区南院门街道南大街粉巷30号");
                hospital.setHospitalLevel("甲级");
                earthQuakeReportEntity.getEarthQuakeHospital().add(hospital);
            }

            new CreateRainReport().createEarthQuakeReport("D:/test.docx", earthQuakeReportEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
class CreateRainReport {
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
            createBlankLine(document, 3);
            // 创建标题
            createTitle(document, "地震应急预评估报告");
            // 一个空行
            createBlankLine(document, 1);
            // 创建部门信息
            createDept(document, "西安市应急管理局               " + earthQuakeReportEntity.getReportTime());
            // 两个空行
            createBlankLine(document, 2);
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
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();

        run.setText(tip);
        run.setFontSize(TEXT_FONT_SIZE);
        run.setFontFamily(TEXT_FONT_FAMILY);

        paragraph.setAlignment(ParagraphAlignment.RIGHT);
    }

    /**
     * 创建空行
     *
     * @param doc - 文档对象
     * @param n   - 行数
     */
    private void createBlankLine(XWPFDocument doc, int n) {
        for (int i = 0; i < n; i++) {
            doc.createParagraph();
        }
    }

    /**
     * 创建标题
     *
     * @param doc
     * @param title
     */
    private void createTitle(XWPFDocument doc, String title) {
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();

        run.setText(title);
        run.setFontFamily("方正小标宋简体");
        run.setFontSize(44);
        run.setColor("FF0000");

        paragraph.setAlignment(ParagraphAlignment.CENTER);
    }

    /**
     * 创建部门信息
     *
     * @param doc
     * @param dept
     */
    private void createDept(XWPFDocument doc, String dept) {
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(dept);
        run.setFontSize(TEXT_FONT_SIZE);
        run.setFontFamily(FONT_FANG_SONG);

        paragraph.setAlignment(ParagraphAlignment.CENTER);

        // 添加边框线
        // 获取段落的底层 XML 对象
        CTP ctp = paragraph.getCTP();
        CTPPr ppr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();

        // 确保段落边框属性存在
        CTBorder border = ppr.isSetPBdr() ? ppr.getPBdr().getBottom() : ppr.addNewPBdr().addNewBottom();

        // 设置边框样式为单实线
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(12));
        // 设置边框颜色为黑色
        border.setColor("000000");
    }

    /**
     * 创建段落并移除编号属性
     *
     * @param doc 文档对象
     * @return 配置好的段落
     */
    private XWPFParagraph createParagraphWithoutNumbering(XWPFDocument doc) {
        XWPFParagraph paragraph = doc.createParagraph();
        CTPPr ppr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();

        // 移除numPr（编号属性）如果存在
        if (ppr.isSetNumPr()) {
            ppr.unsetNumPr();
        }
        paragraph.setIndentationFirstLine(INDENTATION_DISTANCE);
        return paragraph;
    }

    /**
     * 创建降雨概述部分
     *
     * @param doc
     * @param earthQuakeReportEntity
     */
    private void createEarthQuakeOverview(XWPFDocument doc, EarthQuakeReportEntity earthQuakeReportEntity) {
        XWPFParagraph titleParagraph = createParagraphWithoutNumbering(doc);

        // 标题
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("一、地震概况");
        titleRun.setFontFamily(TEXT_FONT_FAMILY);
        titleRun.setFontSize(TEXT_FONT_SIZE);

        // 内容
        XWPFParagraph contentParagraph = createParagraphWithoutNumbering(doc);
        XWPFRun contentRun = contentParagraph.createRun();

        String content = String.format("据地震台网测定，%s（北京时间）在xxxx市xxxx县xxxx镇（北纬xx度，东经xx度）发生xx级地震, 震源深度xx千米。",
                earthQuakeReportEntity.getRainTime(),
                list2Str(earthQuakeReportEntity.getRainAreaPosition(), null),
                list2Str(earthQuakeReportEntity.getRainAreaQuantity(), "毫米"),
                earthQuakeReportEntity.getConcentratedAreaPosition(),
                earthQuakeReportEntity.getConcentratedAreaQuantity(),
                earthQuakeReportEntity.getConcentratedAreaAverageQuantity(),
                list2Str(earthQuakeReportEntity.getConcentratedAreaDetailStreet(), null),
                earthQuakeReportEntity.getConcentratedAreaDetailQuantity(),
                earthQuakeReportEntity.getConcentratedAreaDetailGrade(),
                streetPlusRainfall(earthQuakeReportEntity.getExtremelyHeavyRainstormStreet(), earthQuakeReportEntity.getExtremelyHeavyRainQuantity()),
                streetPlusRainfall(earthQuakeReportEntity.getRainstormStreet(), earthQuakeReportEntity.getRainstormQuantity())
        );

        contentRun.setText(content);
        contentRun.setFontFamily(TEXT_FONT_FAMILY);
        contentRun.setFontSize(TEXT_FONT_SIZE);
    }

    /**
     * 创建风险评估
     *
     * @param doc
     * @param earthQuakeReportEntity
     */
    private void createRiskAssessment(XWPFDocument doc, EarthQuakeReportEntity earthQuakeReportEntity) {
        XWPFParagraph titleParagraph = createParagraphWithoutNumbering(doc);

        // 标题
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("二、风险评估");
        titleRun.setFontFamily(TEXT_FONT_FAMILY);
        titleRun.setFontSize(TEXT_FONT_SIZE);

        // 内容
        XWPFParagraph contentParagraph = createParagraphWithoutNumbering(doc);
        XWPFRun contentRun = contentParagraph.createRun();

        String content = String.format("受持续强降雨影响，灾害风险评估模型在%d个地质灾害风险区、%d个地质灾害在测隐患点的范围内，结合了%s这些致灾因子进行评估，" +
                        "评估得到%s%s的地质灾害风险显著上升，需高度警惕其中的%d个地质风险区和%d个地质灾害在测隐患点发生山洪、泥石流等次生灾害发生的可能性。",
                earthQuakeReportEntity.getRiskAreaQuantity(),
                earthQuakeReportEntity.getHideAreaQuantity(),
                list2Str(earthQuakeReportEntity.getHazards(), null),
                earthQuakeReportEntity.getSignificantIncreaseArea(),
                list2Str(earthQuakeReportEntity.getSignificantIncreaseAreaStreet(), null),
                earthQuakeReportEntity.getSignificantIncreaseAreaRiskQuantity(),
                earthQuakeReportEntity.getSignificantIncreaseAreaHideQuantity()
        );

        contentRun.setText(content);
        contentRun.setFontFamily(TEXT_FONT_FAMILY);
        contentRun.setFontSize(TEXT_FONT_SIZE);

        // 滑坡、泥石流、山洪、内涝分别处理
        for (int i = 0; i < earthQuakeReportEntity.getSecondaryDisasterReport().size(); i++) {
            RainReportEntity.SecondaryDisasterReportEntity disasterReport = earthQuakeReportEntity.getSecondaryDisasterReport().get(i);

            XWPFParagraph contentParagraph2 = createParagraphWithoutNumbering(doc);
            XWPFRun contentRun2 = contentParagraph2.createRun();

            StringBuilder contentBuilder = new StringBuilder();
            contentRun2.setFontFamily(TEXT_FONT_FAMILY);
            contentRun2.setFontSize(TEXT_FONT_SIZE);

            if (i == 0) {
                contentBuilder.append("测算结果根据降雨集中区域结合地形地势因素分析得出，");
            }

            // 灾害类型
            contentBuilder.append(
                    String.format("%s风险主要集中在%s附近区域，其中%s点位滑坡发生概率达%s。" +
                                    "预计受影响的人数为%d人，灾害等级将达特大型，为本轮强降雨期间%s风险最高区域，需要提前疏散居民，严加防范。" +
                                    "除此之外，在持续性降雨背景下，%s可能发生的灾害等级将达到大型、中型。",
                            disasterReport.getDisasterType().getDisasterName(),
                            disasterReport.getRiskStreet(),
                            disasterReport.getRiskPointName(),
                            disasterReport.getRiskPointProbability(),
                            disasterReport.getInfluencePeopleQuantity(),
                            disasterReport.getDisasterType().getDisasterName(),
                            list2Str(disasterReport.getSeriousArea(), null)
                    )
            );

            contentRun2.setText(contentBuilder.toString());

            // 设置表格标题
            XWPFParagraph tableTitleParagraph = doc.createParagraph();
            tableTitleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun tableTitleRun = tableTitleParagraph.createRun();

            tableTitleRun.setText(String.format("%s灾害预测概率统计表", disasterReport.getDisasterType().getDisasterName()));
            tableTitleRun.setFontFamily(FONT_FANG_SONG);
            tableTitleRun.setFontSize(14);

            // 生成表格
            createSecondaryDisastersTable(doc, disasterReport,
                    new String[]{"序号", "位置", disasterReport.getDisasterType().getDisasterName() + "发生概率", "灾害等级"});

            // 最后一部分
            XWPFParagraph contentParagraph3 = doc.createParagraph();
            XWPFRun contentRun3 = contentParagraph3.createRun();

            contentRun3.setFontFamily(TEXT_FONT_FAMILY);
            contentRun3.setFontSize(TEXT_FONT_SIZE);
            contentRun3.setText(
                    String.format(
                            "其中，%s%s可能的特大型/大型灾害附近涉及%d个风险区（村庄），预计影响%d人，附近居民和风险影响区域居民必须撤离。" +
                                    "%s%s可能的中/小型灾害，预计影响%d人，建议附近居民做好防护，风险影响区域居民建议撤离。",
                            disasterReport.getExtraLargeArea(),
                            disasterReport.getExtraLargeAreaPoint(),
                            disasterReport.getExtraLargeAreaRiskQuantity(),
                            disasterReport.getExtraLargeAreaRiskPeopleQuantity(),
                            disasterReport.getSmallArea(),
                            disasterReport.getSmallAreaPoint(),
                            disasterReport.getSmallAreaRiskPeopleQuantity()
                    )
            );
        }
    }

    /**
     * 设置表格数据
     *
     * @param doc
     * @param secondaryDisasterReportEntity
     * @param headers
     */
    private void createSecondaryDisastersTable(XWPFDocument doc,
                                               RainReportEntity.SecondaryDisasterReportEntity secondaryDisasterReportEntity, String[] headers) {
        int rows = secondaryDisasterReportEntity.getDisasterTableData().size() + 1;
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

            RainReportEntity.SecondaryDisasterReportEntity.SecondaryDisasterTableData rowData =
                    secondaryDisasterReportEntity.getDisasterTableData().get(i - 1);

            // 序号列
            setupTableCell(bodyRow.getCell(0), String.valueOf(i), false);
            // 位置列
            setupTableCell(bodyRow.getCell(1), rowData.getPosition(), false);
            // 概率列
            setupTableCell(bodyRow.getCell(2), rowData.getProbability(), false);
            // 等级列
            setupTableCell(bodyRow.getCell(3), rowData.getGrade(), false);
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
        XWPFParagraph titleParagraph = createParagraphWithoutNumbering(doc);

        // 标题
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("三、应急处置建议");
        titleRun.setFontFamily(TEXT_FONT_FAMILY);
        titleRun.setFontSize(TEXT_FONT_SIZE);

        // 段落内容数组
        String[] contents = {
                "防范总建议：",
                String.format("1. %s政府要做好地质灾害防范工作安排部署，组织镇街、村组开展地质灾害隐患点、风险区巡查排查监测，必要时组织受威胁群众转移避险。",
                        list2Str(earthQuakeReportEntity.getWorkScheduleArea(), null)),
                "2. 资源规划部门要加强与相关部门沟通衔接，及时叫应预警区内地质灾害隐患点、风险区监测员和巡查员，督促指导有关单位做好重点区域巡查排查技术指导。",
                "3.水务、交通、旅游等部门开展预警区内水库、公路、景区等重要基础设施周边地质灾害风险隐患巡查监测。",
                "4.应急管理部门做好可能发生的地质灾害应急准备工作。",
                "处置措施建议：",
                String.format("人员疏散方面，建议优先组织%s中紧邻河道的民房、农家乐等高风险区域居民转移，此类区域靠近水体，受山洪和泥石流突发影响最为显著。" +
                                "同时，应重点关注%s等城市内涝易发的低洼积水区域，确保上述重点区域人员能够及时、安全撤离，最大限度保障群众生命安全。",
                        list2Str(earthQuakeReportEntity.getEvacuateTheCrowdArea(), null),
                        list2Str(earthQuakeReportEntity.getFocusArea(), null)),
                "人员安置方面，应优先选择地势较高、远离河道且具备较好排水条件的安全区域，村内学校、村委会等公共设施也可作为临时安置点，具备一定容纳能力和生活配套条件。对于本地安置条件受限的村组，可组织跨区域转移，安排至周边安全城镇，利用当地酒店、学校等资源保障群众基本生活与应急避险需求。",
                "救援队伍准备方面：建议提前联系消防、交通和医疗三类专业救援力量：",
                "消防救援队准备生命探测设备、环境探测设备和破拆工具等，可以及时定位被困人员，评估现场环境。",
                "交通救援队预置挖掘机、装载机和道路抢修车等，开辟抗震救灾绿色通道。",
                "医疗救援队组织具备外科、内科、急救能力的医疗人员，配备心肺复苏仪、骨折固定夹板、担架等装备及绷带、消炎药、止痛药等应急药物。",
                "救援物资准备方面，应根据预测受灾人口数量，提前调配各类救援物资。"
        };

        // 创建所有段落
        for (String content : contents) {
            XWPFParagraph contentParagraph = createParagraphWithoutNumbering(doc);
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText(content);
            contentRun.setFontFamily(TEXT_FONT_FAMILY);
            contentRun.setFontSize(TEXT_FONT_SIZE);
        }
    }

    /**
     * List转为字符串
     *
     * @param list
     * @param unit
     * @return
     */
    private String list2Str(List<?> list, String unit) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            Object item = list.get(i);
            if (item != null) {
                sb.append(item.toString());
                if (unit != null) {
                    sb.append(unit);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 街道加降雨量
     *
     * @param street
     * @param rain
     * @return
     */
    private String streetPlusRainfall(List<?> street, List<?> rain) {
        if (street == null || rain == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int minSize = Math.min(street.size(), rain.size());

        for (int i = 0; i < minSize; i++) {
            if (i > 0) {
                sb.append("、");
            }

            Object streetItem = street.get(i);
            Object rainItem = rain.get(i);

            if (streetItem != null) {
                sb.append(streetItem.toString());
            }

            if (rainItem != null) {
                sb.append("(").append(rainItem.toString()).append("毫米)");
            }
        }
        return sb.toString();
    }
}
