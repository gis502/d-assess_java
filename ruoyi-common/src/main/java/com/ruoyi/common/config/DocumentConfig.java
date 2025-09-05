package com.ruoyi.common.config;

/**
 * @author wzy
 * @description: TODO(生成文档配置信息)
 * @date 2025/9/1 上午10:20
 */
public class DocumentConfig {

    // 基础字体大小（磅值）
    public static final Integer FONT_BASE_SIZE = 16;

    // 字体大小（磅值）
    public static final Integer FONT_SIZE_THREE = 16;         // 三号字体
    public static final Integer FONT_SIZE_SMALL_THREE = 15;   // 小三字体
    public static final Integer FONT_SIZE_FOUR = 14;          // 四号字体
    public static final Integer FONT_SIZE_SMALL_FOUR = 12;    // 小四字体

    // 字体类型
    public static final String FONT_HEI_TI = "黑体";                  // 黑体
    public static final String FONT_FANG_SONG_GB2312 = "仿宋_GB2312";  // 仿宋_GB2312

    // 颜色（十六进制RGB值，不带#号）
    public static final String COLOR_RED = "FF0000";    // 红色
    public static final String COLOR_YELLOW = "FFFF00"; // 黄色

    // 图片放大倍数
    public static final Double IMAGE_MAGNIFICATION = 25D;

    // 缩进字符（默认两个字符，基于基础字体大小计算）
    private Integer IndentCharacters = 40 * FONT_BASE_SIZE;

    // 表格行高度
    private Integer tableRowHeight = 567;

    public Integer getIndentCharacters() {
        return IndentCharacters;
    }

    public void setIndentCharacters(Integer indentCharacters) {
        IndentCharacters = indentCharacters;
    }

    public Integer getTableRowHeight() {
        return tableRowHeight;
    }

    public void setTableRowHeight(Integer tableRowHeight) {
        this.tableRowHeight = tableRowHeight;
    }
}