package com.ruoyi.disaster.file.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 灾害文件管理实体类
 * 用于管理暴雨和地震灾害相关的文件信息
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
@Data
@TableName("disaster_files")
@AllArgsConstructor
@NoArgsConstructor
public class DisasterFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件 ID（主键）
     */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    /**
     * 灾害 ID（暴雨 rain_id 或地震 disaster_id）
     */
    @TableField("disaster_id")
    private String disasterId;

    /**
     * 灾害类型（rain:暴雨，earthquake:地震）
     */
    @TableField("disaster_type")
    private String disasterType;

    /**
     * 文件名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件路径（相对路径）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 本地文件路径（绝对路径）
     */
    @TableField("local_file_path")
    private String localFilePath;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型（后缀名）
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件描述
     */
    @TableField("file_description")
    private String fileDescription;

    /**
     * 上传人
     */
    @TableField("upload_by")
    private String uploadBy;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT, value = "create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE, value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志（0:未删除，1:已删除）
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
