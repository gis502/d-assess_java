package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.EqList;
import com.ruoyi.system.domain.RainList;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RainListMapper extends BaseMapper<RainList> {

    int insert(RainList rainList);

}
