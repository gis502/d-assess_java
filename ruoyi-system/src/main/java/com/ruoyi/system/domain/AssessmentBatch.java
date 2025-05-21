package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author: xiaodemos
 * @date: 2025-04-04 10:16
 * @description: 地震评估表
 */

@Data
@TableName("assessment_batch")
public class AssessmentBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    // 唯一编码
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    // 地震唯一编码
    @TableField("eq_id")
    private String eqId;

    // 地震批次编码
    @TableField("eqqueue_id")
    private String eqqueueId;

    // 地震批次
    @TableField("batch")
    private Integer batch;

    // 启动类型（自动,手动）
    @TableField("type")
    private Integer type;

    // 评估状态（未开始，正在计算，正常完成，人工停止，异常结束，超时结束）
    @TableField("state")
    private String state;

    // 评估开始时间
    @TableField("begin_time")
    private Date beginTime;

    // 评估结束时间
    @TableField("end_time")
    private Date endTime;

    // 评估进度
    @TableField("progress")
    private Double progress;

    // 备注
    @TableField("remark")
    private String remark;

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
