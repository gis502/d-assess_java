package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2025-05-13 9:52
 * @description: 经济损失、人员伤亡、建筑物破坏等评估结果类
 */


@Data
@TableName("assessment_result")
public class AssessmentResult {
    @TableField("id")
    private String id; // 编码
    @TableField("eq_id")
    private String eqId; // 地震唯一标识符
    @TableField("eqqueue_id")
    private String eqqueueId; // 地震评估批次编码
    @TableField("batch")
    private String batch; // 计算批次
    @TableField("eq_name")
    private String eqName; // 地震名称
    @TableField("intensity")
    private Integer inty; // 烈度值
    @TableField("pac_code")
    private String pac; // 乡镇代码
    @TableField("pac_name")
    private String pacName; // 乡镇名称
    @TableField("building_damage")
    private String buildingDamage; // 建筑破坏面积（万平方米）
    @TableField("pop_count")
    private Integer pop; // 受灾人数(人)

    @TableField("death_count")
    private Integer death; // 死亡人数（人）

    @TableField("missing_count")
    private Integer missing; // 失踪人数（人）

    @TableField("injury_count")
    private Integer injury; // 受伤人数（人）

    @TableField("buried_count")
    private Integer buriedCount; // 压埋人数（人）

    @TableField("reset_number")
    private Integer resetNumber; // 需紧急安置人员（人）

    @TableField("economic_loss")
    private String economicLoss; // 经济损失（万元）

    @TableField(value = "is_deleted")
    private Integer isDeleted;
}
