package com.ruoyi.common.enums;

/**
 * @author wzy
 * @description: TODO(图片类型枚举)
 * @date 2025/9/1 下午5:20
 */
public enum ImageTypeEnum {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif"),
    BMP("bmp"),
    VMF("vmf"),
    EMF("emf"),
    PICT("pict");

    private String value;

    ImageTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}