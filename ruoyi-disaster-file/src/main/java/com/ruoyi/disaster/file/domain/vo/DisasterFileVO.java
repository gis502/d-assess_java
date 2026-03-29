package com.ruoyi.disaster.file.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 灾害文件视图对象
 * 用于展示暴雨和地震灾害的文件信息
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisasterFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件 ID（对应 output 表的 id）
     */
    private String id;

    /**
     * 灾害 ID（暴雨 rain_id 或地震 eq_id）
     */
    private String disasterId;

    /**
     * 灾害类型（rain:暴雨，earthquake:地震）
     */
    private String disasterType;

    /**
     * 评估批次编码
     */
    private String queueId;

    /**
     * 产品编码
     */
    private String code;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件大小
     */
    private Double fileSize;

    /**
     * 文件路径（相对路径）
     */
    private String sourceFile;

    /**
     * 本地文件路径（绝对路径）
     */
    private String localSourceFile;

    /**
     * 备注
     */
    private String remark;

    /**
     * 专题图尺寸
     */
    private String size;

    /**
     * 产出类型（1:专题图，2:报告）
     */
    private Integer type;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 是否删除（0:未删除，1:已删除）
     */
    private Integer isDeleted;
}
