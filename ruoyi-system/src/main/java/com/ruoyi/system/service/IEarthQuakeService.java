package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.R;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IEarthQuakeService {
    public R<String> generateEarthQuakeReport(Integer eq_id) throws IOException, InvalidFormatException;

    public void downloadReport(String fileName, HttpServletResponse resp) throws IOException;
}
