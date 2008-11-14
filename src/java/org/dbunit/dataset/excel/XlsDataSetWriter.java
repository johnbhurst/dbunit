/*
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002-2008, DbUnit.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.dbunit.dataset.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes an {@link IDataSet} to an XLS file or OutputStream.
 * 
 * @author gommma (gommma AT users.sourceforge.net)
 * @author Last changed by: $Author$
 * @version $Revision$ $Date$
 * @since 2.4.0
 */
public class XlsDataSetWriter 
{
    public static final String ZEROS = "0000000000000000000000000000000000000000000000000000";

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(XlsDataSetWriter.class);

    /**
     * Write the specified dataset to the specified Excel document.
     */
    public void write(IDataSet dataSet, OutputStream out)
            throws IOException, DataSetException
    {
        logger.debug("write(dataSet={}, out={}) - start", dataSet, out);

        HSSFWorkbook workbook = new HSSFWorkbook();

        int index = 0;
        ITableIterator iterator = dataSet.iterator();
        while(iterator.next())
        {
            // create the table i.e. sheet
            ITable table = iterator.getTable();
            ITableMetaData metaData = table.getTableMetaData();
            HSSFSheet sheet = workbook.createSheet(metaData.getTableName());

            // write table metadata i.e. first row in sheet
            workbook.setSheetName(index, metaData.getTableName());

            HSSFRow headerRow = sheet.createRow(0);
            Column[] columns = metaData.getColumns();
            for (int j = 0; j < columns.length; j++)
            {
                Column column = columns[j];
                HSSFCell cell = headerRow.createCell(j);
                cell.setCellValue(new HSSFRichTextString(column.getColumnName()));
            }
            
            // write table data
            for (int j = 0; j < table.getRowCount(); j++)
            {
                HSSFRow row = sheet.createRow(j + 1);
                for (int k = 0; k < columns.length; k++)
                {
                    Column column = columns[k];
                    Object value = table.getValue(j, column.getColumnName());
                    if (value != null)
                    {
                        HSSFCell cell = row.createCell(k);
                        if(value instanceof Date){
                            setDateCell(cell, (Date)value, workbook);
                        }
                        else if(value instanceof BigDecimal){
                            setNumericCell(cell, (BigDecimal)value, workbook);
                        }
                        else {
                            cell.setCellValue(new HSSFRichTextString(DataType.asString(value)));
                        }
                    }
                }
            }

            index++;
        }

        // write xls document
        workbook.write(out);
        out.flush();
    }
    
    protected void setDateCell(HSSFCell cell, Date value, HSSFWorkbook workbook) 
    {
        double excelDateValue = HSSFDateUtil.getExcelDate(value);
        cell.setCellValue(excelDateValue);
        cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
        
//      System.out.println(HSSFDataFormat.getBuiltinFormats());
        // TODO Find out correct cell styles for date objects
//        HSSFCellStyle cellStyleDate = workbook.createCellStyle();
//        cellStyleDate.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
//
//        HSSFCellStyle cellStyleDateTimeWithSeconds = workbook.createCellStyle();
//        cellStyleDateTimeWithSeconds.setDataFormat(HSSFDataFormat.getBuiltinFormat("h:mm:ss"));
//
//        HSSFDataFormat dataFormat = workbook.createDataFormat();
//        HSSFCellStyle cellStyle = workbook.createCellStyle();
//        cellStyle.setDataFormat(dataFormat.getFormat("dd/mm/yyyy hh:mm:ss"));
//
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat formatter3 = new SimpleDateFormat("HH:mm:ss.SSS");
//
//        
//        Date dateValue = (Date)value;
//        Calendar cal = null;
//        
//        // If it is a date value that does not have seconds
//        if(dateValue.getTime() % 60000 == 0){
////            cellStyle = cellStyleDate;
//            cal=Calendar.getInstance();
//            cal.setTimeInMillis(dateValue.getTime());
//
//            cell.setCellValue(cal);
//            cell.setCellStyle(cellStyle);
////            cell.setCellValue(cal);
//        }
//        else {
////            HSSFDataFormatter formatter = new HSSFDataFormatter();
//            
//            // If we have seconds assume that it is only h:mm:ss without date
//            // TODO Clean implementation where user can control date formats would be nice
////            double dateDouble = dateValue.getTime() % (24*60*60*1000);
//            cal = get1900Cal(dateValue);
//            
//            String formatted = formatter3.format(dateValue);
//            //TODO Format ...
////            cellStyle = cellStyleDateTimeWithSeconds;
//            System.out.println("date formatted:"+formatted);
////            HSSFRichTextString s = new HSSFRichTextString(formatted);
////            cell.setCellValue(s);
//            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//            cell.setCellValue((double)dateValue.getTime());
//            cell.setCellStyle(cellStyleDateTimeWithSeconds);
//        }

    }

    protected void setNumericCell(HSSFCell cell, BigDecimal value, HSSFWorkbook workbook)
    {
        if(logger.isDebugEnabled())
            logger.debug("setNumericCell(cell={}, value={}, workbook={}) - start", 
                new Object[] {cell, value, workbook} );

        cell.setCellValue( ((BigDecimal)value).doubleValue() );

        HSSFDataFormat df = workbook.createDataFormat();
        int scale = ((BigDecimal)value).scale();
        short format;
        if(scale <= 0){
            format = df.getFormat("####");
        }
        else {
            String zeros = createZeros(((BigDecimal)value).scale());
            format = df.getFormat("####." + zeros);
        }
        if(logger.isDebugEnabled())
            logger.debug("Using format '{}' for value '{}'.", String.valueOf(format), value);
        
        HSSFCellStyle cellStyleNumber = workbook.createCellStyle();
        cellStyleNumber.setDataFormat(format);
        cell.setCellStyle(cellStyleNumber);
    }

    
//    public static Date get1900(Date date) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(date.getTime() % (24*60*60*1000));
//        cal.set(1900, 0, 1); // 1.1.1900
//        return cal.getTime();
//    }
//
//    public static Calendar get1900Cal(Date date) {
//        Calendar cal = Calendar.getInstance();
//        cal.clear();
////        long hoursInMillis = date.getTime() % (24*60*60*1000);
////        long smallerThanDays = date.getTime() % (24*60*60*1000);
////        cal.setTimeInMillis(date.getTime() % (24*60*60*1000));
//        cal.set(Calendar.SECOND, (int) (date.getTime() % (24*60*60*1000)) / (1000) );
//        cal.set(Calendar.MINUTE, (int) (date.getTime() % (24*60*60*1000)) / (1000*60) );
//        cal.set(Calendar.HOUR, (int) (date.getTime() % (24*60*60*1000)) / (1000*60*60) );
////        cal.set(1900, 0, 1); // 1.1.1900
//        System.out.println(cal.isSet(Calendar.DATE));
//        return cal;
//    }

    private static String createZeros(int count) {
        return ZEROS.substring(0, count);
    }

}
