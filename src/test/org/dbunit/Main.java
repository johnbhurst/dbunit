/*
 * Main.java   Mar 14, 2002
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002, Manuel Laflamme
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

package org.dbunit;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;
import java.util.Date;

import electric.xml.Document;

/**
 * This class is a scratchpad used to try new features.
 *
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        oldMain();
//        testWrite();
//        writeXls();
//        newSheet();
//        createCells();
//        createDateCells();
//        readWriteWorkbook();
//        cellTypes();
    }

    private static void testWrite() throws Exception
    {
        Writer out = new FileWriter("test.xml");

        Document document = new Document();
        document.write(out);
        out.flush();
    }

    private static void oldMain() throws Exception
    {
//        System.setProperty("dbunit.name.escapePattern", "\"?\"");
        IDatabaseConnection connection =
                DatabaseEnvironment.getInstance().getConnection();
//        IDataSet dataSet = new XmlDataSet(new FileReader("dataSetTest.xml"));
//        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

//        String[] tableNames = connection.createDataSet().getTableNames();
//        Arrays.sort(tableNames);
//        FlatXmlDataSet.writeDtd(new FilteredDataSet(tableNames,
//                connection.createDataSet()),
//                new FileOutputStream("test.dtd"));
//
//
        Writer out = new FileWriter("test.xml");
//        FlatXmlDataSet.write(connection.createDataSet(), out, "ISO-8859-1");
        FlatXmlDataSet.write(connection.createDataSet(), out);
//        out.flush();
//        out.close();


//        ////////////////////////////////
//        Document document = new Document(new File("src/xml/flatXmlDataSetTest.xml"));
//        DocType docType = document.getDocType();
//        System.out.println(docType);
//
//        // display children of DocType
//        for (Children decls = docType.getChildren(); decls.hasMoreElements();)
//        {
//            Child decl = decls.next();
//            String type = decl.getClass().getName();
//            System.out.println("decl = " + decl + ", class: " + type);
//        }

//        IDataSet dataSet = new FlatXmlDataSet(
//                new FileInputStream("flatXmlDataSetTest.xml"));
//        FlatDtdDataSet.write(new FlatXmlDataSet(
//                new FileInputStream("src/xml/flatXmlDataSetTest.xml")),
//                new FileOutputStream("src/dtd/flatXmlDataSetTest.dtd"));
    }

/*
    private static void writeXls() throws IOException, DataSetException
    {
        Reader in = new FileReader(
                "P:/dbunit-cvs/dbunit/src/xml/dataSetTest.xml");
        FileOutputStream out = new FileOutputStream(
                "P:/dbunit-cvs/dbunit/src/xml/dataSetTest.xls");
        XlsDataSet.write(new XmlDataSet(in), out);
        out.close();
    }

    public static void newSheet() throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("new sheet");
        HSSFSheet sheet2 = wb.createSheet("second sheet");
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }

    public static void readWriteWorkbook() throws Exception
    {
        POIFSFileSystem fs      =
                new POIFSFileSystem(new FileInputStream("workbook.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(2);
        HSSFCell cell = row.getCell((short)3);
        if (cell == null)
            cell = row.createCell((short)3);
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        cell.setCellValue("a test");

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }

    public static void cellTypes() throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");
        HSSFRow row = sheet.createRow((short)2);
        row.createCell((short) 0).setCellValue(1.1);
        row.createCell((short) 1).setCellValue(new Date());
        row.createCell((short) 2).setCellValue("a string");
        row.createCell((short) 3).setCellValue(true);
        row.createCell((short) 4).setCellType(HSSFCell.CELL_TYPE_ERROR);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }

    public static void createCells() throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");

        // Create a row and put some cells in it. Rows are 0 based.
        HSSFRow row = sheet.createRow((short)0);
        // Create a cell and put a value in it.
        HSSFCell cell = row.createCell((short)0);
        cell.setCellValue(1);

        // Or do it on one line.
        row.createCell((short)1).setCellValue(1.2);
        row.createCell((short)2).setCellValue("This is a string");
        row.createCell((short)3).setCellValue(true);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }

    public static void createDateCells() throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");

        // Create a row and put some cells in it. Rows are 0 based.
        HSSFRow row = sheet.createRow((short)0);

        // Create a cell and put a date value in it.  The first cell is not styled as a date.
        HSSFCell cell = row.createCell((short)0);
        cell.setCellValue(new Date());

        // we style the second cell as a date (and time).  It is important to create a new cell style from the workbook
        // otherwise you can end up modifying the built in style and effecting not only this cell but other cells.
        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
        cell = row.createCell((short)1);
        cell.setCellValue(new Date());
        cell.setCellStyle(cellStyle);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }
*/

}

















