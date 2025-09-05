package com.ruoyi.common.utils.file;

import com.ruoyi.common.config.DocumentConfig;
import com.ruoyi.common.enums.ImagePositionEnum;
import com.ruoyi.common.enums.ImageTypeEnum;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wzy
 * @description: TODO(文档工具类)
 * @date 2025/9/1 上午10:17
 */
public class DocumentUtils {
    private static final DocumentConfig documentConfig = new DocumentConfig();

    /**
     * 添加一个普通的段落
     *
     * @param doc  - 文档对象
     * @param text - 添加默认内容，为null时不添加run
     * @return 段落对象
     */
    public static XWPFParagraph addRegularParagraph(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = doc.createParagraph();
        CTPPr ppr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();

        // 移除numPr（编号属性）如果存在
        if (ppr.isSetNumPr()) {
            ppr.unsetNumPr();
        }
        paragraph.setIndentationFirstLine(documentConfig.getIndentCharacters());

        if (text != null) {
            // 添加一个run
            addRegularRun(paragraph, text);
        }
        return paragraph;
    }

    /**
     * 创建空行
     *
     * @param doc - 文档对象
     * @param n   - 行数
     */
    public static void createBlankLine(XWPFDocument doc, int n) {
        for (int i = 0; i < n; i++) {
            doc.createParagraph();
        }
    }

    /**
     * 添加突出显示的run
     *
     * @param paragraph - 段落字段
     * @param text      - 文本
     * @return
     */
    public static XWPFRun addAnnotationRun(XWPFParagraph paragraph, String text) {
        XWPFRun run = addRegularRun(paragraph, text);
        run.setColor(DocumentConfig.COLOR_RED);
        return run;
    }

    /**
     * 添加一个普通的run
     *
     * @param paragraph - 段落对象
     * @param text      - 文字
     * @return {XWPFRun} - run对象
     */
    public static XWPFRun addRegularRun(XWPFParagraph paragraph, String text) {
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily(DocumentConfig.FONT_HEI_TI);
        run.setFontSize(DocumentConfig.FONT_SIZE_THREE);
        return run;
    }

    /**
     * 创建标题
     *
     * @param doc
     * @param title
     */
    public static void createTitle(XWPFDocument doc, String title) {
        XWPFParagraph paragraph = addRegularParagraph(doc, null);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setFirstLineIndent(0);

        XWPFRun run = addAnnotationRun(paragraph, title);
        run.setFontFamily("方正小标宋简体");
        run.setFontSize(44);
    }


    /**
     * 添加边框线
     *
     * @param paragraph - 段落
     * @param size      - 边框线尺寸
     */
    public static void addParagraphBorderLine(XWPFParagraph paragraph, BigInteger size) {
        // 获取段落的底层 XML 对象
        CTP ctp = paragraph.getCTP();
        CTPPr ppr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();

        // 确保段落边框属性存在
        CTBorder border = ppr.isSetPBdr() ? ppr.getPBdr().getBottom() : ppr.addNewPBdr().addNewBottom();

        // 设置边框样式为单实线
        border.setVal(STBorder.SINGLE);
        border.setSz(size == null ? BigInteger.valueOf(12) : size);
        // 设置边框颜色为黑色
        border.setColor("000000");
    }

    /**
     * List转为字符串
     *
     * @param list
     * @param unit
     * @return
     */
    public static String list2Str(List<?> list, String unit) {
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
    public static String streetPlusRainfall(List<?> street, List<?> rain) {
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

    /**
     * 创建表格
     *
     * @param doc        文档对象
     * @param dataList   数据列表
     * @param headers    表头数组
     * @param fieldNames 与表头对应的实体类字段名数组
     * @param <T>        数据列表中元素的类型
     * @Param containSerialNumber 是否包含序号
     */
    public static <T> void createGenericTable(XWPFDocument doc, List<T> dataList, List<String> headers, List<String> fieldNames, boolean containSerialNumber) {
        // 验证参数
        if (headers == null || fieldNames == null || headers.size() != fieldNames.size()) {
            throw new IllegalArgumentException("表头与字段名数组长度必须一致");
        }

        // 如果包含序号，就插入序号
        if (containSerialNumber) {
            headers.add(0, "序号");
        }

        int rows = dataList.size() + 1; // 数据行 + 表头行
        int cols = headers.size();

        XWPFTable table = doc.createTable(rows, cols);
        table.setWidth("100%"); // 设置表格宽度

        // 设置表头
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.setHeight(documentConfig.getTableRowHeight());
        for (int i = 0; i < cols; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            if (cell == null) {
                cell = headerRow.createCell();
            }
            setupTableCell(cell, headers.get(i), true);
        }

        // 设置数据行
        for (int i = 0; i < dataList.size(); i++) {
            XWPFTableRow dataRow = table.getRow(i + 1);
            dataRow.setHeight(documentConfig.getTableRowHeight());

            T data = dataList.get(i);
            // 为每一列设置值
            for (int j = 0; j < cols; j++) {
                String fieldValue = null;
                if (containSerialNumber && j == 0) {
                    fieldValue = i + 1 + "";
                } else if (containSerialNumber) {
                    fieldValue = getFieldValue(data, fieldNames.get(j - 1));
                } else {
                    fieldValue = getFieldValue(data, fieldNames.get(j));
                }
                XWPFTableCell cell = dataRow.getCell(j);
                if (cell == null) {
                    cell = dataRow.createCell();
                }
                setupTableCell(cell, fieldValue != null ? fieldValue : "", false);
            }
        }
    }

    /**
     * 清除单元格中所有段落
     *
     * @param cell
     */
    public static void removeCellParagraph(XWPFTableCell cell) {
        // 清除单元格中的所有现有段落
        for (int k = cell.getParagraphs().size() - 1; k >= 0; k--) {
            cell.removeParagraph(k);
        }
    }

    /**
     * 设置表格单元格内容
     *
     * @param cell     表格单元格
     * @param text     单元格文本
     * @param isHeader 是否为表头单元格
     */
    private static void setupTableCell(XWPFTableCell cell, String text, boolean isHeader) {
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

        // 清除单元格所有段落
        removeCellParagraph(cell);

        // 创建段落
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingAfter(0);       // 段后0

        // 创建文本对象
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily(DocumentConfig.FONT_FANG_SONG_GB2312);
        run.setFontSize(DocumentConfig.FONT_SIZE_SMALL_FOUR);

        if (isHeader) {
            run.setBold(true); // 设置加粗
        }
    }

    /**
     * 使用反射获取对象的字段值
     *
     * @param obj       目标对象
     * @param fieldName 字段名
     * @return 字段值的字符串表示
     */
    private static String getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        try {
            // 尝试获取字段（包括私有字段）
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : null;
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getFieldValue(obj, fieldName);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串数组转换成一个新的列表
     *
     * @param array
     * @return
     */
    public static List<String> stringArray2List(String... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        for (String item : array) {
            list.add(item);
        }
        return list;
    }

    /**
     * 在 Word 文档的指定段落文本后插入图片（支持本地和网络图片）
     *
     * @param doc             Word 文档对象
     * @param imagePath       图片路径（本地路径或URL）
     * @param imageType       图片类型
     * @param widthInCm       图片宽度（厘米），为null则使用文档宽度的80%
     * @param heightInCm      图片高度（厘米），为null则按宽度等比例缩放
     * @param caption         图片标注信息内容
     * @param captionPosition 标注位置（"before" 图前, "after" 图后）
     */
    public static void insertImageWithCaption(XWPFDocument doc, String imagePath, ImageTypeEnum imageType, Double widthInCm, Double heightInCm, String caption, ImagePositionEnum captionPosition) {
        try {
            // 获取图片输入流（本地或网络）
            try (InputStream imageStream = getImageStream(imagePath)) {
                // 使用 BufferedInputStream 包装以便支持 mark/reset
                BufferedInputStream bufferedImageStream = new BufferedInputStream(imageStream);
                bufferedImageStream.mark(Integer.MAX_VALUE); // 标记流的起始位置

                // 读取图片原始尺寸用于比例计算
                BufferedImage originalImage = ImageIO.read(bufferedImageStream);
                bufferedImageStream.reset(); // 重置流到起始位置

                // 获取文档宽度（厘米）
                double docWidthInCm = getDocumentWidthInCm(doc);

                // 处理宽度为null的情况
                if (widthInCm == null) {
                    widthInCm = docWidthInCm * 0.8; // 文档宽度的80%
                }

                // 处理高度为null的情况（按比例缩放）
                if (heightInCm == null && originalImage != null) {
                    double aspectRatio = originalImage.getHeight() / (double) originalImage.getWidth();
                    heightInCm = widthInCm * aspectRatio;
                }

                // 转换为EMU单位
                int width = Units.toEMU(widthInCm * DocumentConfig.IMAGE_MAGNIFICATION);
                int height = heightInCm != null ? Units.toEMU(heightInCm * DocumentConfig.IMAGE_MAGNIFICATION) : width;

                // 确定图片类型和文件名
                String fileName = getFileNameFromPath(imagePath);
                int pictureType = getPictureType(fileName, imageType);

                // 如果有标注且位置在图前，先添加标注
                if (caption != null && caption.trim().length() > 0 && captionPosition == ImagePositionEnum.BEFORE) {
                    addCaptionParagraph(doc, caption);
                }

                // 创建图片段落并插入图片
                XWPFParagraph imageParagraph = doc.createParagraph();
                XWPFRun run = imageParagraph.createRun();
                try {
                    run.addPicture(bufferedImageStream, // 使用重置后的流
                            pictureType, fileName, width, height);
                } catch (InvalidFormatException e) {
                    throw new RuntimeException(e);
                }

                // 图片对齐居中
                imageParagraph.setAlignment(ParagraphAlignment.CENTER);

                // 如果有标注且位置在图后，添加标注
                if (caption != null && caption.trim().length() > 0 && captionPosition == ImagePositionEnum.AFTER) {
                    addCaptionParagraph(doc, caption);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取图片输入流（支持本地文件和网络URL）
     */
    private static InputStream getImageStream(String imagePath) throws IOException {
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            // 网络图片
            URL url = new URL(imagePath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            return new BufferedInputStream(connection.getInputStream());
        } else {
            // 本地图片
            return new BufferedInputStream(new FileInputStream(imagePath));
        }
    }

    /**
     * 从路径中提取文件名
     */
    private static String getFileNameFromPath(String path) {
        if (path == null) return "image";

        // 处理URL情况
        if (path.contains("://")) {
            String[] parts = path.split("/");
            return parts[parts.length - 1];
        }

        // 处理本地文件路径
        return new File(path).getName();
    }

    /**
     * 获取文档宽度（厘米）
     */
    private static double getDocumentWidthInCm(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().getSectPr();
        if (sectPr == null) {
            sectPr = doc.getDocument().getBody().addNewSectPr();
        }
        CTPageSz pageSz = sectPr.getPgSz();

        // 默认A4纸宽度21厘米
        if (pageSz == null) {
            return 21.0;
        }

        // 转换twips为厘米 (1 twip = 0.017638889 厘米)
        return Double.parseDouble(pageSz.getW().toString()) * 0.017638889;
    }

    /**
     * 添加标注段落
     */
    private static void addCaptionParagraph(XWPFDocument doc, String caption) {
        XWPFParagraph captionPara = doc.createParagraph();
        captionPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun captionRun = captionPara.createRun();
        captionRun.setText(caption);
        captionRun.setFontSize(DocumentConfig.FONT_SIZE_FOUR);
    }

    /**
     * 根据文件名判断图片类型（POI 支持的类型）
     */
    private static int getPictureType(String fileName, ImageTypeEnum imageType) {

        if (fileName == null || (imageType == null && !fileName.contains("."))) {
            throw new IllegalArgumentException("无法识别图片格式");
        }

        String suffix = null;

        if (imageType != null) {
            suffix = imageType.getValue();
        }else {
            suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }

        switch (suffix) {
            case "jpg":
            case "jpeg":
                return XWPFDocument.PICTURE_TYPE_JPEG;
            case "png":
                return XWPFDocument.PICTURE_TYPE_PNG;
            case "gif":
                return XWPFDocument.PICTURE_TYPE_GIF;
            case "bmp":
                return XWPFDocument.PICTURE_TYPE_BMP;
            case "wmf":
                return XWPFDocument.PICTURE_TYPE_WMF;
            case "emf":
                return XWPFDocument.PICTURE_TYPE_EMF;
            case "pict":
                return XWPFDocument.PICTURE_TYPE_PICT;
            default:
                throw new IllegalArgumentException("不支持的图片格式：" + suffix);
        }
    }
}