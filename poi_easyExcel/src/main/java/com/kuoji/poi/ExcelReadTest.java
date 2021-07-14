package com.kuoji.poi;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Date;

public class ExcelReadTest {

    public static final String PATH = "D:\\Work-Space\\ProjectsFile\\poi_excel_demo\\";

    @Test
    public void testRead03() throws Exception{
        // 获取文件流
        FileInputStream fileInputStream = new FileInputStream(PATH + "观众统计表03.xls");

        // 1. 读取工作簿
        Workbook workbook = new HSSFWorkbook(fileInputStream);
        // 2. 得到表
        Sheet sheet = workbook.getSheetAt(0);
        // 3. 得到行
        Row row = sheet.getRow(0);
        // 4. 得到列
        Cell cell = row.getCell(0);

        // 读取值的时候，一定需要注意类型
        // 获取字符串类型
        System.out.println(cell.getStringCellValue());

        fileInputStream.close();
    }

    @Test
    public void testRead07() throws Exception{
        // 获取文件流
        FileInputStream fileInputStream = new FileInputStream(PATH + "观众统计表07.xlsx");

        // 1. 读取工作簿
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        // 2. 得到表
        Sheet sheet = workbook.getSheetAt(0);
        // 3. 得到行
        Row row = sheet.getRow(0);
        // 4. 得到列
        Cell cell = row.getCell(0);

        // 读取值的时候，一定需要注意类型
        // 获取字符串类型
        System.out.println(cell.getStringCellValue());

        fileInputStream.close();
    }

    // 读取不同类型数据
    @Test
    public void testCellType() throws Exception{
        // 获取文件流
        FileInputStream fileInputStream = new FileInputStream(PATH + "明细表.xls");

        // 1. 读取工作簿
        Workbook workbook = new HSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        // 获取标题内容
        Row rowTitle = sheet.getRow(0);
        if (rowTitle != null){
            // 统计有多少列
            int cellCount = rowTitle.getPhysicalNumberOfCells();
            for (int cellNum = 0; cellNum < cellCount; cellNum++) {
                Cell cell = rowTitle.getCell(cellNum);
                if (cell != null){
                    String cellValue = cell.getStringCellValue();
                    System.out.print(cellValue + "|");
                }
            }
            System.out.println();
        }

        // 获取表中的内容
        // 统计有多少行
        int rowCount = sheet.getPhysicalNumberOfRows();
        for (int rowNum = 1; rowNum < rowCount; rowNum++) {
            Row rowDate = sheet.getRow(rowNum);
            if (rowDate != null){
                // 读取列
                int cellCount = rowTitle.getPhysicalNumberOfCells();
                for (int cellNum = 0; cellNum < cellCount; cellNum++) {
                    System.out.print("[" + (rowNum + 1) + "-" + (cellNum + 1) + "]");

                    Cell cell = rowDate.getCell(cellNum);
                    // 匹配列的数据类型
                    if (cell != null){
                        int cellType = cell.getCellType();
                        String cellValue = "";

                        switch (cellType){
                            case HSSFCell.CELL_TYPE_STRING: // 字符串
                                cellValue = cell.getStringCellValue();
                                break;
                            case HSSFCell.CELL_TYPE_BOOLEAN: // 布尔
                                cellValue = String.valueOf(cell.getBooleanCellValue());
                                break;
                            case HSSFCell.CELL_TYPE_BLANK: // 字符串
                                break;
                            case HSSFCell.CELL_TYPE_NUMERIC: // 数字 (日期、普通数字)
                                if (HSSFDateUtil.isCellDateFormatted(cell)){
                                    // 日期
                                    Date date = cell.getDateCellValue();
                                    cellValue = new DateTime(date).toString("yyyy-MM-dd");

                                }else {
                                    // 转换为字符串输出
                                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                                    cellValue = cell.toString();
                                }
                                break;
                            case HSSFCell.CELL_TYPE_ERROR: // 错误
                                 break;
                            default: break;
                        }
                        System.out.println(cellValue);
                    }

                    fileInputStream.close();
                }
            }
        }


    }

    // 公式
    @Test
    public void testFormula() throws Exception{
        FileInputStream inputStream = new FileInputStream(PATH + "公式.xls");
        Workbook workbook = new HSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Row row = sheet.getRow(4);
        Cell cell = row.getCell(0);

        // 拿到计算公式 eval
        FormulaEvaluator formulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);

        // 输入单元格的内容
        int cellType = cell.getCellType();
        switch (cellType){
            case Cell.CELL_TYPE_FORMULA: // 公式
                String formula = cell.getCellFormula();
                System.out.println(formula);

                // 计算
                CellValue evaluate = formulaEvaluator.evaluate(cell);
                String cellValue = evaluate.formatAsString();
                System.out.println(cellValue);
                break;

            default: break;
        }



    }


}
