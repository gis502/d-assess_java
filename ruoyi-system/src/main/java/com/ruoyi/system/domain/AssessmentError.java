package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author: xiaodemos
 * @date: 2025-04-04 11:24
 * @description: 评估异常表
 */


@Data
@TableName("assessment_error")
public class AssessmentError implements Serializable {

    private static final long serialVersionUID = 1L;

    // 日志ID
    @TableId(value = "id", type = IdType.AUTO)
    private String logId;

    // 地震唯一编码
    @TableField("eq_id")
    private String eqId;

    // 地震批次编码
    @TableField("eqqueue_id")
    private String eqqueueId;

    // 失败原因（0人工停止，1异常停止，2超时结束）
    @TableField("fail_reason")
    private Integer failReason;

    // 异常信息
    @TableField("abnormal_info")
    private String abnormalInfo;

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
