package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-04-04 11:27
 * @description: 评估图件产出表
 */


@Data
@TableName("rain_assessment_output")
public class RainAssessmentOutput implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private String id; // 编码

    @TableField(value = "rain_id")
    private String rainId; // 地震唯一标识符

    @TableField(value = "rain_queue_id")
    private String rainQueueId; // 地震评估批次编码

    @TableField(value = "code")
    private String code; // 产品编码

    @TableField(value = "file_type")
    private String fileType; // 文件类型

    @TableField(value = "file_name")
    private String fileName; // 文件名称

    @TableField(value = "file_extension")
    private String fileExtension; // 扩展名

    @TableField(value = "file_size")
    private Double fileSize; // 文件大小

    @TableField(value = "source_file")
    private String sourceFile; // 文件路径

    @TableField(value = "local_source_file")
    private String localSourceFile; // 本地文件路径

    @TableField(value = "remark")
    private String remark; // 备注

    @TableField(value = "size")
    private String size; // 专题图尺寸（A3）

    @TableField(value = "type")
    private Integer type; // 产出类型（1:专题图，2：报告）

    // 创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT, value = "create_time")
    private LocalDateTime createTime;

    // 修改时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE, value = "update_time")
    private LocalDateTime updateTime;

    // 逻辑删除
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;



}
