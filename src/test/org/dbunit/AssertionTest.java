/*
 * AssertionTest.java   Mar 22, 2002
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

import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.io.FileInputStream;
import java.io.FileReader;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class AssertionTest extends TestCase
{
    public AssertionTest(String s)
    {
        super(s);
    }

    private IDataSet getDataSet() throws Exception
    {
        return new FlatXmlDataSet(new FileReader(
                "src/xml/assertionTest.xml"));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Test methods

    public void testAssertTablesEquals() throws Exception
    {
        IDataSet dataSet = getDataSet();
        Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                dataSet.getTable("TEST_TABLE_WITH_SAME_VALUE"));
    }

    public void testAssertTablesEqualsColumnNamesCaseInsensitive() throws Exception
    {
        IDataSet dataSet = getDataSet();
        Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                dataSet.getTable("TEST_TABLE_WITH_LOWER_COLUMN_NAMES"));
    }

    public void testAssertTablesAndNamesNotEquals() throws Exception
    {
        IDataSet dataSet = getDataSet();
        Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                dataSet.getTable("TEST_TABLE_WITH_DIFFERENT_NAME"));
    }

    public void testAssertTablesAndColumnCountNotEquals() throws Exception
    {
        IDataSet dataSet = getDataSet();

        try
        {
            Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                    dataSet.getTable("TEST_TABLE_WITH_3_COLUMNS"));
            throw new IllegalStateException("Should throw an AssertionFailedError");
        }
        catch (AssertionFailedError e)
        {
        }
    }

    public void testAssertTablesAndColumnSequenceNotEquals() throws Exception
    {
        IDataSet dataSet = getDataSet();

        Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                dataSet.getTable("TEST_TABLE_WITH_DIFFERENT_COLUMN_SEQUENCE"));
    }

    public void testAssertTablesAndColumnNamesNotEquals() throws Exception
    {
        IDataSet dataSet = getDataSet();

        try
        {
            Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                    dataSet.getTable("TEST_TABLE_WITH_DIFFERENT_COLUMN_NAMES"));
            throw new IllegalStateException("Should throw an AssertionFailedError");
        }
        catch (AssertionFailedError e)
        {
        }
    }

    public void testAssertTablesAndRowCountNotEquals() throws Exception
    {
        IDataSet dataSet = getDataSet();

        try
        {
            Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                    dataSet.getTable("TEST_TABLE_WITH_ONE_ROW"));
            throw new IllegalStateException("Should throw an AssertionFailedError");
        }
        catch (AssertionFailedError e)
        {
        }
    }

    public void testAssertTablesAndValuesNotEquals() throws Exception
    {
        IDataSet dataSet = getDataSet();

        try
        {
            Assertion.assertEquals(dataSet.getTable("TEST_TABLE"),
                    dataSet.getTable("TEST_TABLE_WITH_WRONG_VALUE"));
            throw new IllegalStateException("Should throw an AssertionFailedError");
        }
        catch (AssertionFailedError e)
        {
        }
    }

    public void testAssertDataSetsEquals() throws Exception
    {
        IDataSet dataSet1 = getDataSet();

        // change table names order
        String[] names = DataSetUtils.getReverseTableNames(dataSet1);
        IDataSet dataSet2 = new FilteredDataSet(names, dataSet1);

        assertTrue("assert not same", dataSet1 != dataSet2);
        Assertion.assertEquals(dataSet1, dataSet2);
    }

    public void testAssertDataSetsEqualsTableNamesCaseInsensitive() throws Exception
    {
        IDataSet dataSet1 = getDataSet();

        // change table names case
        String[] names = dataSet1.getTableNames();
        for (int i = 0; i < names.length; i++)
        {
            names[i] = names[i].toLowerCase();
        }
        IDataSet dataSet2 = new FilteredDataSet(names, dataSet1);

        assertTrue("assert not same", dataSet1 != dataSet2);
        Assertion.assertEquals(dataSet1, dataSet2);
    }

    public void testAssertDataSetsAndTableCountNotEquals() throws Exception
    {
        IDataSet dataSet1 = getDataSet();

        // only one table
        String[] names = new String[]{dataSet1.getTableNames()[0]};
        IDataSet dataSet2 = new FilteredDataSet(names, dataSet1);

        assertTrue("assert not same", dataSet1 != dataSet2);

        try
        {
            Assertion.assertEquals(dataSet1, dataSet2);
            throw new IllegalStateException("Should throw an AssertionFailedError");
        }
        catch (AssertionFailedError e)
        {
        }
    }

    public void testAssertDataSetsAndTableNamesNotEquals() throws Exception
    {
        IDataSet dataSet1 = getDataSet();

        // reverse table names
        String[] names = dataSet1.getTableNames();
        ITable[] tables = new ITable[names.length];
        for (int i = 0; i < names.length; i++)
        {
            String reversedName = new StringBuffer(names[i]).reverse().toString();
            tables[i] = new CompositeTable(reversedName,
                    dataSet1.getTable(names[i]));
        }
        IDataSet dataSet2 = new DefaultDataSet(tables);

        assertTrue("assert not same", dataSet1 != dataSet2);
        assertEquals("table count", dataSet1.getTableNames().length,
                dataSet2.getTableNames().length);

        try
        {
            Assertion.assertEquals(dataSet1, dataSet2);
            throw new IllegalStateException("Should throw an AssertionFailedError");
        }
        catch (AssertionFailedError e)
        {
        }
    }

    public void testAssertDataSetsAndTablesNotEquals() throws Exception
    {
        IDataSet dataSet1 = getDataSet();

        // different row counts (double)
        IDataSet dataSet2 = new CompositeDataSet(dataSet1, dataSet1);

        assertTrue("assert not same", dataSet1 != dataSet2);
        assertEquals("table count", dataSet1.getTableNames().length,
                dataSet2.getTableNames().length);

        try
        {
            Assertion.assertEquals(dataSet1, dataSet2);
            throw new IllegalStateException("Should throw an AssertionFailedError");
        }
        catch (AssertionFailedError e)
        {
        }
    }
}





