package com.ruoyi.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.EqList;

public interface EqListMapper extends BaseMapper<EqList> {

    int insert(EqList eqList);
}
