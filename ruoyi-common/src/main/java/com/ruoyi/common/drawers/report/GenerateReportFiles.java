package com.ruoyi.common.drawers.report;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.LayoutConstants;
import com.ruoyi.common.core.domain.AssessmentBTO;
import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.springframework.scheduling.Trigger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 19:58
 * @description: 制作报告文件
 */

@Slf4j
public class GenerateReportFiles {


    // 灾情报告一文档制作
    public static String WordExporter1()
    {

        return null;
    }



    // 灾情报告二文档制作
    public static String WordExporter2(String title, String result, String fuJinTownResult, String judge,
                                    String suggestion, String measure, String eqTime,
                                    AssessmentBTO params) throws IOException {

        log.info("正在制作word文档中...");

        // 创建一个 XWPFDocument 对象
        XWPFDocument document = new XWPFDocument();
        // 制作文档抬头
        drawerDocumentHeader(document, eqTime, title);
        // 制作内容一 震区基本情况
        drawerEarthquakeAreaSituation(document, result);
        // 制作内容二 市区震情基本情况
        drawerCitySituation(document, fuJinTownResult);
        // 制作内容三 应急处置建议
        drawerEmergencyResponseSuggestion(document, judge, suggestion, measure);
        // 制作文档结尾信息 值班联系信息、附件提示信息...
        drawerDocumentEnd(document);
        // 制作文档附件
        drawerDocumentAttachments(document);
        // 设置页面边距
        setPageMargins(document);
        // 写入文件
        String filePath = writeToDocument(document, params.getEqId());

        return filePath;
    }

    // 制作文档附件
    private static void drawerDocumentAttachments(XWPFDocument document) {
        // 空1行，仿宋_GB2312的16号字体，段落居中
        XWPFParagraph emptyParagraphSong4 = document.createParagraph();
        emptyParagraphSong4.setAlignment(ParagraphAlignment.LEFT); // 段落居左
        // 模拟单倍行距（一般情况下1倍行距是240 TWIPS，适当设置行距）
        emptyParagraphSong4.setSpacingBefore(240); // 设置段前行距为240 TWIPS（1倍行距）
        emptyParagraphSong4.setSpacingAfter(240);  // 设置段后行距为240 TWIPS（1倍行距）

        // 启用孤行控制
        emptyParagraphSong4.setWordWrap(true); // 启用孤行控制

        // 设置字体样式
        XWPFRun newRunSong4 = emptyParagraphSong4.createRun();
        newRunSong4.setText(" "); // 设置文本内容
        newRunSong4.setFontFamily("仿宋_GB2312"); // 字体
        newRunSong4.setFontSize(16); // 字号16

        // 第九段正文内容
        XWPFParagraph oneText9 = document.createParagraph();
        oneText9.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置段前分页
        oneText9.setPageBreak(true); // 设置段前分页
        // 设置首行缩进为2字符宽
        //int indentSize9 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText9.setIndentationFirstLine(indentSize9); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText9.setSpacingBetween(1.2);// 设置1.2倍行距
        oneText9.setSpacingBefore(0); // 段前0倍行距
        oneText9.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun9 = oneText9.createRun();
        newRun9.setText("附件"); // 设置文本内容
        newRun9.setFontFamily("黑体"); // 字体
        newRun9.setFontSize(16); // 三号字体（16磅）

        // 第十段正文内容
        XWPFParagraph emptyParagraphSong5 = document.createParagraph();
        emptyParagraphSong5.setAlignment(ParagraphAlignment.CENTER); // 段落居左
        // 模拟单倍行距（一般情况下1倍行距是240 TWIPS，适当设置行距）
        emptyParagraphSong5.setSpacingBefore(240); // 设置段前行距为240 TWIPS（1倍行距）
        emptyParagraphSong5.setSpacingAfter(240);  // 设置段后行距为240 TWIPS（1倍行距）

        // 设置字体样式
        XWPFRun newRunSong5 = emptyParagraphSong4.createRun();
        newRunSong5.setText(" "); // 设置文本内容
        newRunSong5.setFontFamily("仿宋_GB2312"); // 字体
        newRunSong5.setFontSize(16); // 字号16

        log.info("word文档制作完成...");

    }

    // 制作文档结尾信息 值班联系信息、附件提示信息...
    private static void drawerDocumentEnd(XWPFDocument document) {

        // 空1行，仿宋_GB2312的16号字体，段落居中
        XWPFParagraph emptyParagraphSong1 = document.createParagraph();
        emptyParagraphSong1.setAlignment(ParagraphAlignment.LEFT); // 段落居中
        emptyParagraphSong1.setSpacingBetween(2.5);// 设置2.5倍行距
        emptyParagraphSong1.setSpacingAfter(0); // 段后0行距
        // 设置字体样式
        XWPFRun newRunSong1 = emptyParagraphSong1.createRun();
        newRunSong1.setText(" ");
        newRunSong1.setFontFamily("仿宋_GB2312"); // 字体
        newRunSong1.setFontSize(16); // 字号16

        // 第六段正文内容
        XWPFParagraph oneText6 = document.createParagraph();
        oneText6.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize6 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText6.setIndentationFirstLine(indentSize6); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText6.setSpacingBetween(1.2);// 设置1.2倍行距
        oneText6.setSpacingBefore(0);// 段前0倍行距
        // oneText6.setSpacingBefore((int) (16 * 20 * 1.5)); // 段前1.5倍行距（注释掉的代码保持一致）
        oneText6.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun6 = oneText6.createRun();
        // TODO 添加附件名称，这里应该有多个灾情图片
        newRun6.setText("    " + "附件：地震影响场分布图"); // 设置文本内容
        newRun6.setFontFamily("仿宋_GB2312"); // 字体
        newRun6.setFontSize(16); // 三号字体（16磅）

        // 空1行，仿宋_GB2312的16号字体，段落居中
        XWPFParagraph emptyParagraphSong2 = document.createParagraph();
        emptyParagraphSong2.setAlignment(ParagraphAlignment.LEFT); // 段落居中
        emptyParagraphSong2.setSpacingBetween(2.75);// 设置2.75倍行距
        emptyParagraphSong2.setSpacingAfter(0); // 段后0行距
        // 设置字体样式
        XWPFRun newRunSong2 = emptyParagraphSong2.createRun();
        newRunSong2.setText(" ");
        newRunSong2.setFontFamily("仿宋_GB2312"); // 字体
        newRunSong2.setFontSize(16); // 字号16

        // 第七段正文内容
        XWPFParagraph oneText7 = document.createParagraph();
        oneText7.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize7 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText7.setIndentationFirstLine(indentSize7); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText7.setSpacingBetween(1.25);// 设置1.25倍行距
        oneText7.setSpacingBefore(0); // 段前0倍行距
        // oneText7.setSpacingBefore((int) (16 * 20 * 1.5)); // 段前1.5倍行距（注释掉的代码保持一致）
        oneText7.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun7 = oneText7.createRun();
        newRun7.setText("    " + "雅安市应急管理局值班电话：0835-2220001，卫星电话：17406544731。"); // 设置文本内容
        newRun7.setFontFamily("仿宋_GB2312"); // 字体
        newRun7.setFontSize(16); // 三号字体（16磅）

        // 空1行，仿宋_GB2312的16号字体，段落居中
        XWPFParagraph emptyParagraphSong3 = document.createParagraph();
        emptyParagraphSong3.setAlignment(ParagraphAlignment.LEFT); // 段落居中
        // 模拟单倍行距（一般情况下1倍行距是240 TWIPS，适当设置行距）
        emptyParagraphSong3.setSpacingBetween(2.1);// 设置2.1倍行距
        // 设置字体样式
        XWPFRun newRunSong3 = emptyParagraphSong3.createRun();
        newRunSong3.setText(" ");
        newRunSong3.setFontFamily("仿宋_GB2312"); // 字体
        newRunSong3.setFontSize(16); // 字号16

        // 第八段正文内容
        XWPFParagraph oneText8 = document.createParagraph();
        oneText8.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize8 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText8.setIndentationFirstLine(indentSize8); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText8.setSpacingBetween(1);// 设置1.2倍行距
        oneText8.setSpacingBefore(0); // 段前0倍行距
        // oneText8.setSpacingBefore((int) (16 * 20 * 1.5)); // 段前1.5倍行距（注释掉的代码保持一致）
        oneText8.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun8 = oneText8.createRun();
        newRun8.setText("    " + "（本期送：市政府领导、局领导、局机关各科室。）"); // 设置文本内容
        newRun8.setFontFamily("仿宋_GB2312"); // 字体
        newRun8.setFontSize(16); // 三号字体（16磅）

    }

    // 制作内容三 应急处置建议
    private static void drawerEmergencyResponseSuggestion(XWPFDocument document, String judge, String suggestion, String measure) {

        // 创建一个新的段落--------第三个标题
        XWPFParagraph emergencyResponseParagraph = document.createParagraph();
        emergencyResponseParagraph.setAlignment(ParagraphAlignment.BOTH);       // 设置段落为两端对齐
        //设置段落格式：单倍行距，段前段后为0
        emergencyResponseParagraph.setSpacingBetween(1.75);// 设置1.75倍行距
        emergencyResponseParagraph.setSpacingBefore(0);//设置段前间距为0
        emergencyResponseParagraph.setSpacingAfter(0);//设置段后间距为0
        XWPFRun emergencyResponseRun = emergencyResponseParagraph.createRun();
        emergencyResponseRun.setText("    " + "三、应急处置建议");
        emergencyResponseRun.setFontFamily("黑体");
        emergencyResponseRun.setFontSize(16); // 设置字号为16磅（注意：三号字号通常对应16磅,这里直接指定了磅值）

        // 第三段正文内容
        XWPFParagraph oneText3 = document.createParagraph();
        oneText3.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize3 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText3.setIndentationFirstLine(indentSize3); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText3.setSpacingBetween(1.5);// 设置1.5倍行距
        oneText3.setSpacingBefore(0); // 段前0倍行距
        // oneText3.setSpacingBefore((int) (16 * 20 * 1.5)); // 段前1.5倍行距（注释掉的代码保持一致）
        oneText3.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun3 = oneText3.createRun();
        newRun3.setText("    " + judge); // 设置文本内容
        newRun3.setFontFamily("仿宋_GB2312"); // 字体
        newRun3.setFontSize(16); // 三号字体（16磅）

        // 第四段正文内容
        XWPFParagraph oneText4 = document.createParagraph();
        oneText4.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize4 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText4.setIndentationFirstLine(indentSize4); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText4.setSpacingBetween(1.5);// 设置1.5倍行距
        oneText4.setSpacingBefore(0); // 段前0倍行距
        // oneText4.setSpacingBefore((int) (16 * 20 * 1.5)); // 段前1.5倍行距（注释掉的代码保持一致）
        oneText4.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun4 = oneText4.createRun();
        newRun4.setText("    " + suggestion); // 设置文本内容
        newRun4.setFontFamily("仿宋_GB2312"); // 字体
        newRun4.setFontSize(16); // 三号字体（16磅）

        // 第五段正文内容
        XWPFParagraph oneText5 = document.createParagraph();
        oneText5.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize5 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText5.setIndentationFirstLine(indentSize5); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText5.setSpacingBetween(1.2);// 设置1.2倍行距
        oneText5.setSpacingBefore(0); // 段前0倍行距
        // oneText5.setSpacingBefore((int) (16 * 20 * 1.5)); // 段前1.5倍行距（注释掉的代码保持一致）
        oneText5.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun5 = oneText5.createRun();
        newRun5.setText("    " + measure); // 设置文本内容
        newRun5.setFontFamily("仿宋_GB2312"); // 字体
        newRun5.setFontSize(16); // 三号字体（16磅）

    }

    // 制作内容二 市区震情基本情况
    private static void drawerCitySituation(XWPFDocument document, String fuJinTownResult) {

        // 创建一个新的段落--------第二个标题
        XWPFParagraph disasterInfoParagraph = document.createParagraph(); // 假设document是您的XWPFDocument对象
        disasterInfoParagraph.setAlignment(ParagraphAlignment.BOTH);         // 设置段落为两端对齐
        //设置段落格式：单倍行距，段前段后为0
        disasterInfoParagraph.setSpacingBetween(1.45);// 设置1.5倍行距
        disasterInfoParagraph.setSpacingBefore(0);//设置段前间距为0
        disasterInfoParagraph.setSpacingAfter(0);//设置段后间距为0
        XWPFRun disasterInfoRun = disasterInfoParagraph.createRun();
        disasterInfoRun.setText("    " + "二、雅安震情灾情");
        disasterInfoRun.setFontFamily("黑体");
        disasterInfoRun.setFontSize(16); // 设置字号为16磅（注意：三号字号通常对应16磅，但这里直接使用了16磅）

        // 第二段正文内容
        XWPFParagraph oneText2 = document.createParagraph();
        oneText2.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize2 = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText2.setIndentationFirstLine(indentSize2); // 设置首行缩进

        // 设置段前间距为1.5倍行距，段后间距为0
        oneText2.setSpacingBetween(1.5);// 设置1.2倍行距
        oneText2.setSpacingBefore(0); // 段前0倍行距
        //oneText2.setSpacingBefore((int) (16 * 20 * 1.5)); // 段前1.5倍行距
        oneText2.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun2 = oneText2.createRun();
        newRun2.setText("    " + fuJinTownResult); // 设置文本内容
        newRun2.setFontFamily("仿宋_GB2312"); // 字体
        newRun2.setFontSize(16); // 三号字体（16磅）
        newRun2.setColor("FF0000"); // 字体颜色：红色

    }

    // 制作内容一 震区基本情况
    private static void drawerEarthquakeAreaSituation(XWPFDocument document, String result) {

        // 创建一个新的段落--------第一个标题
        XWPFParagraph earthquakeInfoParagraph = document.createParagraph();
        earthquakeInfoParagraph.setAlignment(ParagraphAlignment.BOTH);  // 设置段落为两端对齐
        //设置段落格式：单倍行距，段前段后为0
        earthquakeInfoParagraph.setSpacingBetween(1.5);// 设置1.5倍行距
        earthquakeInfoParagraph.setSpacingBefore(0);//设置段前间距为0
        earthquakeInfoParagraph.setSpacingAfter(0);//设置段后间距为0
        XWPFRun earthquakeInfoRun = earthquakeInfoParagraph.createRun();
        earthquakeInfoRun.setText("    " + "一、震区基本情况");  //前面有4个空格
        earthquakeInfoRun.setFontFamily("黑体");
        earthquakeInfoRun.setFontSize(16); // 设置字号为三号（对应16磅）

        // 第一段正文内容
        XWPFParagraph oneText = document.createParagraph();
        oneText.setAlignment(ParagraphAlignment.BOTH); // 两端对齐

        // 设置首行缩进为2字符宽
        //int indentSize = 16 * 2 * 20; // 每字符宽按字号16磅计算，2字符宽
        //oneText.setIndentationFirstLine(indentSize); // 设置首行缩进

        // 设置段前间距为0倍行距，段后间距为0
        oneText.setSpacingBetween(1.5);// 设置1.2倍行距
        oneText.setSpacingBefore(0); // 段前0倍行距
        oneText.setSpacingAfter(0); // 段后0行距

        // 设置字体样式
        XWPFRun newRun = oneText.createRun();
        newRun.setText("    " + result); // 设置文本内容
        newRun.setFontFamily("仿宋_GB2312"); // 字体
        newRun.setFontSize(16); // 三号字体（16磅）

    }

    // 制作文档头部信息
    private static void drawerDocumentHeader(XWPFDocument document, String eqTime, String title) {

        // 第一行：对内掌握，黑体三号，右对齐
        XWPFParagraph firstParagraph = document.createParagraph();
        firstParagraph.setAlignment(ParagraphAlignment.RIGHT); // 右对齐
        firstParagraph.setSpacingBefore((int) (16 * 20 * 0.5));//设置段前间距为0
        firstParagraph.setSpacingAfter(0); // 段后0行距
        XWPFRun firstRun = firstParagraph.createRun();
        firstRun.setText("对内掌握");
        firstRun.setFontFamily("黑体");  // 黑体
        firstRun.setFontSize(16);  // 三号字体

        // 空3行，黑体16号字体，段落居左
        for (int i = 0; i < 3; i++) {
            XWPFParagraph emptyParagraph = document.createParagraph();
            emptyParagraph.setAlignment(ParagraphAlignment.RIGHT); // 段落居右
            emptyParagraph.setSpacingBetween(1.85);// 设置1.85倍行距
            emptyParagraph.setSpacingBefore(0); // 段前0倍行距
            emptyParagraph.setSpacingAfter(0); // 段后0行距

            // 设置字体样式
            XWPFRun newRun = emptyParagraph.createRun();
            newRun.setText(" ");
            newRun.setFontFamily("黑体"); // 字体
            newRun.setFontSize(16); // 字号16
        }

        // 第二行：地震应急辅助决策信息，居中，方正小标宋简体，44号，红色
        XWPFParagraph secondParagraph = document.createParagraph();
        secondParagraph.setAlignment(ParagraphAlignment.CENTER); // 居中
        secondParagraph.setSpacingAfter(0); // 段后0行距
        XWPFRun secondRun = secondParagraph.createRun();
        secondRun.setText("地震应急辅助决策信息");
        secondRun.setFontFamily("方正小标宋简体");  // 方正小标宋简体
        secondRun.setFontSize(44);  // 44号字体
        secondRun.setColor("FF0000");  // 红色

        // 空1行，仿宋_GB2312的16号字体，段落居中
        XWPFParagraph emptyParagraphSong = document.createParagraph();
        emptyParagraphSong.setAlignment(ParagraphAlignment.CENTER); // 段落居中
        emptyParagraphSong.setSpacingBetween(2);// 设置2倍行距
        emptyParagraphSong.setSpacingBefore(0); // 段前0倍行距
        emptyParagraphSong.setSpacingAfter(0); // 段后0行距
        // 设置字体样式
        XWPFRun newRunSong = emptyParagraphSong.createRun();
        newRunSong.setText(" ");
        newRunSong.setFontFamily("仿宋_GB2312"); // 字体
        newRunSong.setFontSize(16); // 字号16

        // 第三行：雅安市应急管理局 + 15个空格（用String.format控制空格数量）+ formattedTime
        // 注意：这里使用String.format来确保空格数量正确，但实际上在Word中，不同字体的空格宽度可能不同
        // 因此，这里的15个空格可能不是视觉上的精确15个空格宽度
        // 如果需要精确控制，可以考虑使用制表符或表格布局
        XWPFParagraph thirdParagraph = document.createParagraph();
        thirdParagraph.setAlignment(ParagraphAlignment.CENTER); // 段落居中（根据您的要求）
        thirdParagraph.setSpacingAfter(0); // 段后0行距
        XWPFRun thirdRun1 = thirdParagraph.createRun();
        thirdRun1.setText("雅安市应急管理局");
        thirdRun1.setFontFamily("仿宋_GB2312");
        thirdRun1.setFontSize(16);  // 三号字体

        // 添加空格
        String spaces = "               ";  // 15个空格
        XWPFRun run = thirdParagraph.createRun();
        run.setText(spaces); // 设置空格内容
        run.setFontFamily("仿宋_GB2312");
        run.setFontSize(16); // 设置字号为三号字体

        XWPFRun thirdRun2 = thirdParagraph.createRun();
        thirdRun2.setText(eqTime);
        thirdRun2.setFontSize(16);  // 三号字体
        thirdRun2.setFontFamily("仿宋_GB2312");

        // 添加红色分隔符
        XWPFParagraph redLineParagraph = document.createParagraph();
        // 设置段落居中对齐
        redLineParagraph.setAlignment(ParagraphAlignment.CENTER);
        redLineParagraph.setSpacingAfter(0);        // 段后0行距
        redLineParagraph.setSpacingBetween(1.2);    // 设置2倍行距
        XWPFRun redLineRun = redLineParagraph.createRun();
        redLineRun.setText("________________________________________");
        redLineRun.setBold(true);
        redLineRun.setFontSize(22);  // 2号字体
        redLineRun.setColor("FF0000");  // 红色
        redLineRun.setFontFamily("华文行楷");  // 华文行楷字体

        // 空1行，华文行楷22号字体，段落居中
        XWPFParagraph emptyParagraphHua = document.createParagraph();
        emptyParagraphHua.setSpacingBetween(1);// 设置1倍行距
        emptyParagraphHua.setAlignment(ParagraphAlignment.CENTER); // 段落居中
        emptyParagraphHua.setSpacingBefore((int) (16 * 20 * 1.1)); // 段前1.1倍行距
        emptyParagraphHua.setSpacingAfter(0); // 段后0行距
        // 设置字体样式
        XWPFRun newRunHua = emptyParagraphHua.createRun();
        newRunHua.setText(" ");
        newRunHua.setFontFamily("华文行楷"); // 字体
        newRunHua.setFontSize(22); // 字号22


        // 添加标题段落
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER); // 段落居中
        titleParagraph.setSpacingAfter(0); // 段后0行距

        // 创建运行来设置标题文本
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(title);
        titleRun.setFontFamily("方正小标宋简体");  // 字体：方正小标宋简体
        titleRun.setFontSize(22);  // 字号：二号（在Apache POI中，字号是以半磅为单位的，所以二号大约是22*2=44磅的一半，即22）


        // 创建一个新的段落
        XWPFParagraph decisionInfoParagraph = document.createParagraph();
        decisionInfoParagraph.setAlignment(ParagraphAlignment.CENTER);        // 设置段落居中对齐
        emptyParagraphHua.setSpacingBefore((int) (16 * 20)); // 段前0.75倍行距
        decisionInfoParagraph.setSpacingAfter(0); // 段后0行距
        XWPFRun decisionInfoRun = decisionInfoParagraph.createRun();        // 创建一个运行来添加文本
        decisionInfoRun.setText("（辅助决策信息二）");
        decisionInfoRun.setFontFamily("方正小标宋简体");// 设置字体
        decisionInfoRun.setFontSize(22);     // 设置字号为二号（对应22磅）

        // 空1行
        XWPFParagraph emptyParagraph = document.createParagraph();
        emptyParagraph.setAlignment(ParagraphAlignment.RIGHT); // 段落居右
        emptyParagraph.setSpacingBetween(1.45);// 设置1.45倍行距
        // 设置字体样式
        XWPFRun newRunHei = emptyParagraph.createRun();
        newRunHei.setText(" ");
        newRunHei.setFontFamily("黑体"); // 字体
        newRunHei.setFontSize(16); // 字号16

    }

    // 设置文件页边距，纸张大小
    private static void setPageMargins(XWPFDocument document) {
        // 获取页面属性
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();

        // 设置页面边距（单位为 TWIPS，1厘米 ≈ 567 TWIPS）
        pageMar.setTop(BigInteger.valueOf((long) (3.7 * 567)));    // 上边距 3.7厘米
        pageMar.setBottom(BigInteger.valueOf((long) (3.5 * 567))); // 下边距 3.5厘米
        pageMar.setLeft(BigInteger.valueOf((long) (2.8 * 567)));   // 左边距 2.8厘米
        pageMar.setRight(BigInteger.valueOf((long) (2.6 * 567)));  // 右边距 2.6厘米

        // 设置装订线为 0厘米
        pageMar.setGutter(BigInteger.valueOf(0));

        // 设置纸张方向为纵向并设置A4大小
        CTPageSz pageSize = sectPr.getPgSz();
        if (pageSize == null) {
            pageSize = sectPr.addNewPgSz();
        }
        pageSize.setOrient(STPageOrientation.PORTRAIT); // 纵向
        pageSize.setW(BigInteger.valueOf((long) (21 * 567))); // 宽度 21厘米
        pageSize.setH(BigInteger.valueOf((long) (29.7 * 567))); // 高度 29.7厘米
    }

    // 将数据写入文档 下载到指定目录
    private static String writeToDocument(XWPFDocument document, String eqId) {
        // int version = Integer.parseInt(StringUtils.substring(infoBO.getEqqueueId(), infoBO.getEqqueueId().length() - 2));
        // 路径格式：E:/upload/灾情报告/eqId/批次/ 辅助决策信息二 +".docx"
        String filePath = LayoutConstants.REPORTS_PREFIX + eqId + "/1" + "/" + "（辅助决策信息二）.docx";

        try {
            // 创建父目录
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    log.info("目录创建成功: {}" + parentDir.getAbsolutePath());
                    // 将文件保存到文件夹下
                    document.getDocument().save(file);
                    log.info("文件保存到本地成功...");
                    // 写入文件
                    FileOutputStream out = new FileOutputStream(filePath);
                    document.write(out);
                    log.info("文件写入成功...");
                } else {
                    log.error("目录创建失败: {} " + parentDir.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回保存的报告路径
        return filePath;
    }
}
