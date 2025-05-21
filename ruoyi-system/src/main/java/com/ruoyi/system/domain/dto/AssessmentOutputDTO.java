package com.ruoyi.system.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: xiaodemos
 * @date: 2025-04-06 13:54
 * @description: 图件报告产出DTO类
 */


@Data
@Builder
public class AssessmentOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eqId;

    private String eqqueueId;

    private String code; // 产品编码

    private String fileType; // 文件类型

    private String fileName; // 文件名称

    private String fileExtension; // 扩展名

    private Double fileSize; // 文件大小

    private String sourceFile; // 文件路径

    private String localSourceFile; // 本地文件路径

    private String remark; // 备注

    private String size; // 专题图尺寸（A3）

    private Integer type; // 产出类型（1:专题图，2：报告）

}
