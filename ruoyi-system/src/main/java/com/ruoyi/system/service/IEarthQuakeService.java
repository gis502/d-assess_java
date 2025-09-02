package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.domain.EarthQuakeReportEntity;
import com.ruoyi.system.domain.dto.TriggerDTO;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IEarthQuakeService {
    public R<String> generateEarthQuakeReport(EarthQuakeReportEntity earthQuakeReportEntity) throws IOException, InvalidFormatException;
}
