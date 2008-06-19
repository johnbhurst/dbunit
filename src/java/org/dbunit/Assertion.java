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

package org.dbunit;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.Assert;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.UnknownDataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 * @since Mar 22, 2002
 */
public class Assertion
{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(Assertion.class);

    private static final ColumnComparator COLUMN_COMPARATOR = new ColumnComparator();

    private Assertion()
    {
    }

    /**
	 * Compare a one table present in two datasets ignoring specified columns.
	 * 
	 * @param expectedDataset First dataset.
	 * @param actualDataset Second dataset.
	 * @param tableName Table name of the table to be compared.
	 * @param ignoreCols Columns to ignore while comparing.
	 * @throws org.dbunit.DatabaseUnitException
	 *             If an error occurs.
	 */
	public static void assertEqualsIgnoreCols(final IDataSet expectedDataset, final IDataSet actualDataset, 
			final String tableName,	final String[] ignoreCols) throws DatabaseUnitException 
	{
        logger.debug("assertEqualsIgnoreCols(expectedDataset={}, actualDataset={}, tableName={}, ignoreCols={}) - start", 
        		new Object[] {expectedDataset, actualDataset, tableName, Arrays.asList(ignoreCols)} );

        Assertion.assertEqualsIgnoreCols(
				expectedDataset.getTable(tableName), 
				actualDataset.getTable(tableName),
				ignoreCols);
	}

    /**
	 * Compare a one table present in two datasets ignoring specified columns.
	 * 
	 * @param expectedTable First table.
	 * @param actualTable Second table.
	 * @param ignoreCols Columns to ignore while comparing.
	 * @throws org.dbunit.DatabaseUnitException If an error occurs.
	 */
	public static void assertEqualsIgnoreCols(final ITable expectedTable, final ITable actualTable, 
			final String[] ignoreCols) throws DatabaseUnitException 
	{
        logger.debug("assertEquals(expectedTable={}, actualTable={}, ignoreCols={}) - start", 
        		new Object[] {expectedTable, actualTable, Arrays.asList(ignoreCols)} );

        final ITable expectedTableFiltered = DefaultColumnFilter.excludedColumnsTable(expectedTable, ignoreCols);
		final ITable actualTableFiltered = DefaultColumnFilter.excludedColumnsTable(actualTable, ignoreCols);
		Assertion.assertEquals(expectedTableFiltered, actualTableFiltered);
	}

	/**
	 * Compare a table from a dataset with a table generated from an sql query.
	 * 
	 * @param expectedDataset Dataset to retrieve the first table from.
	 * @param connection Connection to use for the SQL statement.
	 * @param sqlQuery SQL query that will build the data in returned second table rows.
	 * @param tableName Table name of the table to compare
	 * @param ignoreCols Columns to ignore while comparing.
	 * @throws DatabaseUnitException If an error occurs while performing the comparison.
	 * @throws java.sql.SQLException If an SQL error occurs.
	 */
	public static void assertEqualsByQuery(final IDataSet expectedDataset, 
			final IDatabaseConnection connection, final String sqlQuery,
			final String tableName, final String[] ignoreCols)
    	throws DatabaseUnitException, SQLException
    {
		ITable expectedTable = expectedDataset.getTable(tableName);
		Assertion.assertEqualsByQuery(expectedTable, connection, tableName, sqlQuery, ignoreCols);
    }
	
	/**
	 * Compare a table with a table generated from an sql query.
	 * 
	 * @param expectedTable Table containing all expected results.
	 * @param connection Connection to use for the SQL statement.
	 * @param tableName The name of the table to query from the database
	 * @param sqlQuery SQL query that will build the data in returned second table rows.
	 * @param ignoreCols Columns to ignore while comparing.
	 * @throws DatabaseUnitException If an error occurs while performing the comparison.
	 * @throws java.sql.SQLException If an SQL error occurs.
	 */
	public static void assertEqualsByQuery(final ITable expectedTable, 
			final IDatabaseConnection connection, final String tableName, final String sqlQuery, 
			final String[] ignoreCols)
    	throws DatabaseUnitException, SQLException
    {
		ITable expected = DefaultColumnFilter.excludedColumnsTable(expectedTable, ignoreCols);
		ITable queriedTable = connection.createQueryTable(tableName, sqlQuery);
		ITable actual = DefaultColumnFilter.excludedColumnsTable(queriedTable, ignoreCols);
		Assertion.assertEquals(expected, actual);
    }
	
    /**
     * Asserts that the two specified dataset are equals. This method ignore
     * the tables order.
     */
    public static void assertEquals(IDataSet expectedDataSet,
            IDataSet actualDataSet) throws DatabaseUnitException
    {
        logger.debug("assertEquals(expectedDataSet={}, actualDataSet={}) - start", expectedDataSet, actualDataSet);

        // do not continue if same instance
        if (expectedDataSet == actualDataSet)
        {
            return;
        }

        String[] expectedNames = getSortedUpperTableNames(expectedDataSet);
        String[] actualNames = getSortedUpperTableNames(actualDataSet);

        // tables count
        Assert.assertEquals("table count", expectedNames.length, actualNames.length);


        // table names in no specific order
        for (int i = 0; i < expectedNames.length; i++)
        {
            if (!actualNames[i].equals(expectedNames[i]))
            {
                Assert.fail("expected tables " + Arrays.asList(expectedNames) +
                        " but was " + Arrays.asList(actualNames));
            }

        }

        // tables
        for (int i = 0; i < expectedNames.length; i++)
        {
            String name = expectedNames[i];
            assertEquals(expectedDataSet.getTable(name),
                    actualDataSet.getTable(name));
        }

    }

    /**
	 * Asserts that the two specified tables are equals. This method ignores the
	 * table names, the columns order, the columns data type and which columns
	 * are composing the primary keys.
	 * @param expectedTable Table containing all expected results.
	 * @param actualTable Table containing all actual results.
     * @throws DatabaseUnitException
	 */
    public static void assertEquals(ITable expectedTable, ITable actualTable)
            throws DatabaseUnitException
    {
        logger.debug("assertEquals(expectedTable={}, actualTable={}) - start", expectedTable, actualTable);
        assertEquals(expectedTable, actualTable, (Column[])null);
    }
    
    /**
     * Asserts that the two specified tables are equals. This method ignores the
     * table names, the columns order, the columns data type and which columns
     * are composing the primary keys.
     * 
	 * @param expectedTable Table containing all expected results.
	 * @param actualTable Table containing all actual results.
     * @param additionalColumnInfo The columns to be printed out if the assert fails because of a data any mismatch.
     * Provides some additional column values that can be needed to quickly identify the columns for which the mismatch
     * occurred (for example a primary key column). 
     * @throws DatabaseUnitException
     */
    public static void assertEquals(ITable expectedTable, ITable actualTable, Column[] additionalColumnInfo)
            throws DatabaseUnitException
    {
        logger.debug("assertEquals(expectedTable={}, actualTable={}, rowValueProvider) - start", expectedTable, actualTable);

        // Do not continue if same instance
        if (expectedTable == actualTable)
        {
            return;
        }

        ITableMetaData expectedMetaData = expectedTable.getTableMetaData();
        ITableMetaData actualMetaData = actualTable.getTableMetaData();
        String expectedTableName = expectedMetaData.getTableName();

//        // verify table name
//        Assert.assertEquals("table name", expectedMetaData.getTableName(),
//                actualMetaData.getTableName());

        // Verify columns
        Column[] expectedColumns = getSortedColumns(expectedMetaData);
        Column[] actualColumns = getSortedColumns(actualMetaData);
        Assert.assertEquals("column count (table=" + expectedTableName + ")",
                expectedColumns.length, actualColumns.length);

        for (int i = 0; i < expectedColumns.length; i++)
        {
            String expectedName = expectedColumns[i].getColumnName();
            String actualName = actualColumns[i].getColumnName();
            if (!expectedName.equalsIgnoreCase(actualName))
            {
                Assert.fail("expected columns " + getColumnNamesAsString(expectedColumns) +
                        " but was " + getColumnNamesAsString(actualColumns) +
                        " (table=" + expectedTableName + ")");
            }
        }

        // Verify row count
        Assert.assertEquals("row count (table=" + expectedTableName + ")",
                expectedTable.getRowCount(), actualTable.getRowCount());

        // values as strings
        for (int i = 0; i < expectedTable.getRowCount(); i++)
        {
            for (int j = 0; j < expectedColumns.length; j++)
            {
                Column expectedColumn = expectedColumns[j];
                Column actualColumn = actualColumns[j];

                String columnName = expectedColumn.getColumnName();
                Object expectedValue = expectedTable.getValue(i, columnName);
                Object actualValue = actualTable.getValue(i, columnName);

                DataType dataType = getComparisonDataType(
                        expectedTableName, expectedColumn, actualColumn);
                if (dataType.compare(expectedValue, actualValue) != 0)
                {
                	// add custom column values information for better identification of mismatching rows
                	String additionalInfo = buildAdditionalColumnInfo(expectedTable, actualTable, i, additionalColumnInfo);
                	
                    // example message: "value (table=MYTAB, row=232, column=MYCOL): expected:<123> but was:<1234>. Additional row info: (column=MyIdCol, expected=444, actual=555)"
                	String msg = "value (table=" + expectedTableName + ", row=" + i +
                    ", col=" + columnName + "): expected:<" +
                    expectedValue + "> but was:<" + actualValue + ">" + additionalInfo;
                    Assert.fail(msg);
                }
                
            }
        }
    }


	private static String buildAdditionalColumnInfo(ITable expectedTable,
			ITable actualTable, int rowIndex, Column[] additionalColumnInfo) {
		
		String additionalInfo = "";
    	if(additionalColumnInfo != null && additionalColumnInfo.length>0) {
    		additionalInfo = " Additional row info:";
    		for (int j = 0; j < additionalColumnInfo.length; j++) {
    			String columnName = additionalColumnInfo[j].getColumnName();
    			try {
					Object expectedKeyValue = expectedTable.getValue(rowIndex, columnName);
					Object actualKeyValue = actualTable.getValue(rowIndex, columnName);
					additionalInfo += " (col '" + columnName + "' values: expected=<"+expectedKeyValue+">, actual=<"+actualKeyValue+">)";
				} catch (DataSetException e) {
					logger.debug("Exception while building additional info for column "+columnName, e);
				}
			}
    	}
    	return additionalInfo;
	}

	
	static DataType getComparisonDataType(String tableName, Column expectedColumn,
            Column actualColumn)
    {
        logger.debug("getComparisonDataType(tableName={}, expectedColumn={}, actualColumn={}) - start", new Object[] {tableName, expectedColumn, actualColumn});

        DataType expectedDataType = expectedColumn.getDataType();
        DataType actualDataType = actualColumn.getDataType();

        // The two columns have different data type
        if (!expectedDataType.getClass().isInstance(actualDataType))
        {
            // Expected column data type is unknown, use actual column data type
            if (expectedDataType instanceof UnknownDataType)
            {
                return actualDataType;
            }

            // Actual column data type is unknown, use expected column data type
            if (actualDataType instanceof UnknownDataType)
            {
                return expectedDataType;
            }

            // Impossible to determine which data type to use
            Assert.fail("Incompatible data types: " + expectedDataType + ", " +
                    actualDataType + " (table=" + tableName + ", col=" +
                    expectedColumn.getColumnName() + ")");
        }
//        // Both columns have unknown data type, use string comparison
//        else if (expectedDataType instanceof UnknownDataType)
//        {
//            return DataType.LONGVARCHAR;
//        }

        // Both columns have same data type, return any one of them
        return expectedDataType;
    }

    private static Column[] getSortedColumns(ITableMetaData metaData)
            throws DataSetException
    {
        logger.debug("getSortedColumns(metaData={}) - start", metaData);

        Column[] columns = metaData.getColumns();
        Column[] sortColumns = new Column[columns.length];
        System.arraycopy(columns, 0, sortColumns, 0, columns.length);
        Arrays.sort(sortColumns, COLUMN_COMPARATOR);
        return sortColumns;
    }

    private static String getColumnNamesAsString(Column[] columns)
    {
        logger.debug("getColumnNamesAsString(columns={}) - start", columns);

        String[] names = new String[columns.length];
        for (int i = 0; i < columns.length; i++)
        {
            Column column = columns[i];
            names[i] = column.getColumnName();
        }
        return Arrays.asList(names).toString();
    }

    private static String[] getSortedUpperTableNames(IDataSet dataSet)
            throws DataSetException
    {
        logger.debug("getSortedUpperTableNames(dataSet={}) - start", dataSet);

        String[] names = dataSet.getTableNames();
        for (int i = 0; i < names.length; i++)
        {
            names[i] = names[i].toUpperCase();
        }
        Arrays.sort(names);
        return names;
    }

    ////////////////////////////////////////////////////////////////////////////
    // ColumnComparator class

    private static class ColumnComparator implements Comparator
    {

        /**
         * Logger for this class
         */
        private static final Logger logger = LoggerFactory.getLogger(ColumnComparator.class);

        public int compare(Object o1, Object o2)
        {
            logger.debug("compare(o1={}, o2={}) - start", o1, o2);

            Column column1 = (Column)o1;
            Column column2 = (Column)o2;

            String columnName1 = column1.getColumnName();
            String columnName2 = column2.getColumnName();
            return columnName1.compareToIgnoreCase(columnName2);
        }
    }
}





