/*
 * FlatDtdDataSetTest.java   Apr 4, 2002
 *
 * Copyright (c)2002 Manuel Laflamme. All Rights Reserved.
 *
 * This software is the proprietary information of Manuel Laflamme.
 * Use is subject to license terms.
 *
 */

package org.dbunit.dataset.xml;

import org.dbunit.DatabaseEnvironment;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.AbstractDataSetTest;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.util.FileAsserts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class FlatDtdDataSetTest extends AbstractDataSetTest
{
    private static final File DTD_FILE =
            new File("src/dtd/flatDtdDataSetTest.dtd");
    private static final File DUPLICATE_FILE =
            new File("src/dtd/flatDtdDataSetDuplicateTest.dtd");

    public FlatDtdDataSetTest(String s)
    {
        super(s);
    }

    ////////////////////////////////////////////////////////////////////////////
    // AbstractDataSetTest class

    protected IDataSet createDataSet() throws Exception
    {
        return new FlatDtdDataSet(new FileReader(DTD_FILE));
    }

    protected IDataSet createDuplicateDataSet() throws Exception
    {
        return new FlatDtdDataSet(new FileReader(DUPLICATE_FILE));
    }

    protected int[] getExpectedDuplicateRows()
    {
        return new int[] {0, 0, 0};
    }

    ////////////////////////////////////////////////////////////////////////////
    // Test methods

    public void testGetDuplicateTable() throws Exception
    {
        String expectedTableName = getDuplicateTableName();

        IDataSet dataSet = createDuplicateDataSet();
        ITable table = dataSet.getTable(expectedTableName);
        String actualTableName = table.getTableMetaData().getTableName();
        assertEquals("table name", expectedTableName, actualTableName);
    }

    public void testGetDuplicateTableMetaData() throws Exception
    {
        String expectedTableName = getDuplicateTableName();

        IDataSet dataSet = createDuplicateDataSet();
        ITableMetaData metaData = dataSet.getTableMetaData(expectedTableName);
        String actualTableName = metaData.getTableName();
        assertEquals("table name", expectedTableName, actualTableName);
    }

    public void testWriteFromDtd() throws Exception
    {
        IDataSet dataSet = new FlatDtdDataSet(new FileReader(DTD_FILE));

        File tempFile = File.createTempFile("flatXmlDocType", ".dtd");

        try
        {
            Writer out = new FileWriter(tempFile);

            try
            {
                // write DTD in temp file
                FlatDtdDataSet.write(dataSet, out);
            }
            finally
            {
                out.close();
            }

            FileAsserts.assertEquals(
                    new BufferedReader(new FileReader(DTD_FILE)),
                    new BufferedReader(new FileReader(tempFile)));
        }
        finally
        {
            tempFile.delete();
        }

    }

    public void testWriteFromDatabase() throws Exception
    {
        IDatabaseConnection connection =
                DatabaseEnvironment.getInstance().getConnection();
        IDataSet dataSet = connection.createDataSet();

        File tempFile = File.createTempFile("flatXmlDocType", ".dtd");

        try
        {
            Writer out = new FileWriter(tempFile);

            try
            {
                // write DTD in temp file
                String[] tableNames = getExpectedNames();
                FlatDtdDataSet.write(new FilteredDataSet(
                        tableNames, dataSet), out);
            }
            finally
            {
                out.close();
            }

            FileAsserts.assertEquals(
                    new BufferedReader(new FileReader(DTD_FILE)),
                    new BufferedReader(new FileReader(tempFile)));
        }
        finally
        {
            tempFile.delete();
        }
    }


}





