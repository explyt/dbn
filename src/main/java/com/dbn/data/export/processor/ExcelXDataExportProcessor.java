package com.dbn.data.export.processor;

import com.dbn.data.export.DataExportFormat;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class ExcelXDataExportProcessor extends ExcelDataExportProcessor{

    @Override
    public DataExportFormat getFormat() {
        return DataExportFormat.EXCELX;
    }

    @Override
    public String getFileExtension() {
        return "xlsx";
    }

    @Override
    public String adjustFileName(String fileName) {
        if (!fileName.contains(".xlsx")) {
            fileName = fileName + ".xlsx";
        }
        return fileName;
    }

    @Override
    protected Workbook createWorkbook() {
        return new SXSSFWorkbook();
    }

    @Override
    protected RichTextString createRichText(String string) {
        return new XSSFRichTextString(string);
    }
}