/*
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002-2004, DbUnit.org
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

package org.dbunit.dataset;

import org.dbunit.database.AmbiguousTableNameException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 * @since Feb 22, 2002
 */
public abstract class AbstractDataSetTest extends AbstractTest
{
    public AbstractDataSetTest(String s)
    {
        super(s);
    }

    protected int[] getExpectedDuplicateRows()
    {
        return new int[] {1, 0, 2};
    }

    /**
     * This method exclude BLOB_TABLE and CLOB_TABLE from the specified dataset
     * because BLOB and CLOB are not supported by all database vendor.  It also excludes
     * tables with Identity columns (MSSQL) because they are specific to MSSQL.
     * TODO : should be refactored into the various DatabaseEnvironments!
     */
    public static IDataSet removeExtraTestTables(IDataSet dataSet) throws Exception
    {
        String[] names = dataSet.getTableNames();

        // exclude BLOB_TABLE and CLOB_TABLE from test since not supported by
        // all database vendor
        List nameList = new ArrayList(Arrays.asList(names));
        nameList.remove("BLOB_TABLE");
        nameList.remove("CLOB_TABLE");
        nameList.remove("DBUNIT.BLOB_TABLE");
        nameList.remove("DBUNIT.CLOB_TABLE");
        /*
        this table shows up on MSSQLServer.  It is a user table for storing diagram information
        that really should be considered a system table.
        */
        nameList.remove("DBUNIT.dtproperties");
        nameList.remove("dtproperties");
        /*
        These tables are created specifically for testing identity columns on MSSQL server.
        They should be ignored on other platforms.
        */
        nameList.remove("DBUNIT.IDENTITY_TABLE");
        nameList.remove("IDENTITY_TABLE");
        nameList.remove("DBUNIT.TEST_IDENTITY_NOT_PK");
        nameList.remove("TEST_IDENTITY_NOT_PK");

        names = (String[])nameList.toArray(new String[0]);

        return new FilteredDataSet(names, dataSet);
    }

    protected abstract IDataSet createDataSet() throws Exception;

    protected abstract IDataSet createDuplicateDataSet() throws Exception;

    /**
     * Create a dataset with duplicate tables having different char case in name
     * @return
     */
    protected abstract IDataSet createMultipleCaseDuplicateDataSet() throws Exception;

    protected void assertEqualsTableName(String mesage, String expected,
            String actual)
    {
        assertEquals(mesage, expected, actual);
    }

    public void testGetTableNames() throws Exception
    {
        String[] expected = getExpectedNames();
        assertContainsIgnoreCase("minimal names subset",
                super.getExpectedNames(), expected);

        IDataSet dataSet = createDataSet();
        String[] names = dataSet.getTableNames();

        assertEquals("table count", expected.length, names.length);
        for (int i = 0; i < expected.length; i++)
        {
            assertEqualsTableName("name " + i, expected[i], names[i]);
        }
    }

    public void testGetTableNamesDefensiveCopy() throws Exception
    {
        IDataSet dataSet = createDataSet();
        assertTrue("Should not be same intance",
                dataSet.getTableNames() != dataSet.getTableNames());
    }

    public void testGetTable() throws Exception
    {
        String[] expected = getExpectedNames();

        IDataSet dataSet = createDataSet();
        for (int i = 0; i < expected.length; i++)
        {
            ITable table = dataSet.getTable(expected[i]);
            assertEqualsTableName("name " + i, expected[i], table.getTableMetaData().getTableName());
        }
    }

    public void testGetUnknownTable() throws Exception
    {
        IDataSet dataSet = createDataSet();
        try
        {
            dataSet.getTable("UNKNOWN_TABLE");
            fail("Should throw a NoSuchTableException");
        }
        catch (NoSuchTableException e)
        {
        }
    }

    public void testGetTableMetaData() throws Exception
    {
        String[] expected = getExpectedNames();

        IDataSet dataSet = createDataSet();
        for (int i = 0; i < expected.length; i++)
        {
            ITableMetaData metaData = dataSet.getTableMetaData(expected[i]);
            assertEqualsTableName("name " + i, expected[i], metaData.getTableName());
        }
    }

    public void testGetUnknownTableMetaData() throws Exception
    {
        IDataSet dataSet = createDataSet();
        try
        {
            dataSet.getTableMetaData("UNKNOWN_TABLE");
            fail("Should throw a NoSuchTableException");
        }
        catch (NoSuchTableException e)
        {
        }
    }

    public void testGetTables() throws Exception
    {
        String[] expected = getExpectedNames();
        assertContainsIgnoreCase("minimal names subset",
                super.getExpectedNames(), expected);

        IDataSet dataSet = createDataSet();
        ITable[] tables = dataSet.getTables();

        assertEquals("table count", expected.length, tables.length);
        for (int i = 0; i < expected.length; i++)
        {
            assertEqualsTableName("name " + i, expected[i],
                    tables[i].getTableMetaData().getTableName());
        }
    }

    public void testGetTablesDefensiveCopy() throws Exception
    {
        IDataSet dataSet = createDataSet();
        assertTrue("Should not be same instance",
                dataSet.getTables() != dataSet.getTables());
    }

    public void testCreateDuplicateDataSet() throws Exception
    {
        try
        {
            /*IDataSet dataSet = */createDuplicateDataSet();
            fail("Should throw AmbiguousTableNameException in creation phase");
        }
        catch (AmbiguousTableNameException expected)
        {
            assertEquals("DUPLICATE_TABLE", expected.getMessage());
        }
    }

    public void testCreateMultipleCaseDuplicateDataSet() throws Exception
    {
        try
        {
            /*IDataSet dataSet = */createMultipleCaseDuplicateDataSet();
            fail("Should throw AmbiguousTableNameException in creation phase");
        }
        catch (AmbiguousTableNameException expected)
        {
            assertEquals("DUPLICATE_TABLE", expected.getMessage());
        }
    }


    public void testGetCaseInsensitiveTable() throws Exception
    {
        String[] expectedNames = getExpectedLowerNames();

        IDataSet dataSet = createDataSet();
        for (int i = 0; i < expectedNames.length; i++)
        {
            String expected = expectedNames[i];
            ITable table = dataSet.getTable(expected);
            String actual = table.getTableMetaData().getTableName();

            if (!expected.equalsIgnoreCase(actual))
            {
                assertEquals("name " + i, expected, actual);
            }
        }
    }

    public void testGetCaseInsensitiveTableMetaData() throws Exception
    {
        String[] expectedNames = getExpectedLowerNames();
        IDataSet dataSet = createDataSet();

        for (int i = 0; i < expectedNames.length; i++)
        {
            String expected = expectedNames[i];
            ITableMetaData metaData = dataSet.getTableMetaData(expected);
            String actual = metaData.getTableName();

            if (!expected.equalsIgnoreCase(actual))
            {
                assertEquals("name " + i, expected, actual);
            }
        }
    }

    public void testIterator() throws Exception
    {
        String[] expected = getExpectedNames();
        assertContainsIgnoreCase("minimal names subset",
                super.getExpectedNames(), expected);

        int i = 0;
        ITableIterator iterator = createDataSet().iterator();
        while(iterator.next())
        {
            assertEqualsTableName("name " + i, expected[i],
                    iterator.getTableMetaData().getTableName());
            i++;
        }

        assertEquals("table count", expected.length, i);
    }

    public void testReverseIterator() throws Exception
    {
        String[] expected = DataSetUtils.reverseStringArray(getExpectedNames());
        assertContainsIgnoreCase("minimal names subset",
                super.getExpectedNames(), expected);

        int i = 0;
        ITableIterator iterator = createDataSet().reverseIterator();
        while(iterator.next())
        {
            assertEqualsTableName("name " + i, expected[i],
                    iterator.getTableMetaData().getTableName());
            i++;
        }

        assertEquals("table count", expected.length, i);
    }

    protected ITable[] createDuplicateTables(boolean multipleCase) throws AmbiguousTableNameException 
    {
        ITable table1 = new DefaultTable("DUPLICATE_TABLE");
        ITable table2 = new DefaultTable("EMPTY_TABLE");
        ITable table3;
        if(!multipleCase){
            table3 = new DefaultTable("DUPLICATE_TABLE");
        }
        else {
            table3 = new DefaultTable("duplicate_TABLE");
        }
        ITable[] tables = new ITable[]{table1, table2, table3};
        return tables;
    }
}










