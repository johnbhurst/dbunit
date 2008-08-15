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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a ITable decorator that provide a sorted view of the decorated table.
 * This implementation does not keep a separate copy of the decorated table data.
 *
 * @author Manuel Laflamme
 * @author Last changed by: $Author$
 * @version $Revision$ $Date$
 * @since Feb 19, 2003
 */
public class SortedTable extends AbstractTable
{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(SortedTable.class);

    private final ITable _table;
    private final Column[] _columns;
    private Integer[] _indexes;
    
    /**
     * The row comparator which is used for sorting
     */
    private AbstractRowComparator rowComparator;

    /**
     * Sort the decorated table by specified columns order.
     * @param table decorated table
     * @param columns columns to be used for sorting
     * @throws DataSetException
     */
    public SortedTable(ITable table, Column[] columns) throws DataSetException
    {
        _table = table;
        _columns = columns;
        validateColumns();
        initialize();
    }

	/**
     * Sort the decorated table by specified columns order.
     * @param table decorated table
     * @param columnNames names of columns to be used for sorting
     * @throws DataSetException
     */
    public SortedTable(ITable table, String[] columnNames) throws DataSetException
    {
        _table = table;
        _columns = new Column[columnNames.length];

        ITableMetaData tableMetaData = table.getTableMetaData();
        // Initialize columns and validate if they exist
        Column[] columns = tableMetaData.getColumns();
        for (int i = 0; i < columnNames.length; i++)
        {
            String columnName = columnNames[i];
            _columns[i] = Columns.getColumn(columnName, columns);
            // If the column does not exist in the table, throw an exception
            // TODO Check if common method from Columns class can be reused -> "Columns.getColumnValidated()"
            if (_columns[i] == null)
            {
				throw new NoSuchColumnException(tableMetaData.getTableName(), columnName);
			}
        }
        
        initialize();
    }

    /**
	 * Sort the decorated table by specified metadata columns order. All
	 * metadata columns will be used.
     * @param table The decorated table
     * @param metaData The metadata used to retrieve all columns which in turn are used 
     * for sorting the table
     * @throws DataSetException
     */
    public SortedTable(ITable table, ITableMetaData metaData) throws DataSetException
    {
        this(table, metaData.getColumns());
    }

    /**
     * Sort the decorated table by its own columns order which is defined by {@link ITable#getTableMetaData()}. 
     * All table columns will be used.
     * @param table The decorated table
     * @throws DataSetException
     */
    public SortedTable(ITable table) throws DataSetException
    {
        this(table, table.getTableMetaData());
    }

    /**
	 * Verifies that all columns that are currently set really exist in the
	 * given table
	 * 
	 * @throws DataSetException
	 */
    private void validateColumns() throws DataSetException 
    {
        Column[] columnsOfTable = _table.getTableMetaData().getColumns();
        // Create a list for easy containment check
        List columnsOfTableList = Arrays.asList(columnsOfTable);
        for (int i = 0; i < _columns.length; i++) 
        {
        	Column sortColumn = _columns[i];
        	if (!columnsOfTableList.contains(sortColumn))
        	{
				throw new NoSuchColumnException(_table.getTableMetaData().getTableName(), sortColumn.getColumnName());
			}
        }
	}

	private void initialize() 
	{
        logger.debug("initialize() - start");
        
        // The default comparator is the one that sorts by string - for backwards compatibility
		this.rowComparator = new RowComparatorByString(this._table, this._columns);		
	}

    private int getOriginalRowIndex(int row) throws DataSetException
    {
    	if(logger.isDebugEnabled())
    		logger.debug("getOriginalRowIndex(row={}) - start", Integer.toString(row));

        if (_indexes == null)
        {
            Integer[] indexes = new Integer[getRowCount()];
            for (int i = 0; i < indexes.length; i++)
            {
                indexes[i] = new Integer(i);
            }

            try
            {
                Arrays.sort(indexes, rowComparator);
            }
            catch (DatabaseUnitRuntimeException e)
            {
                throw (DataSetException)e.getException();
            }

            _indexes = indexes;
        }

        return _indexes[row].intValue();
    }
    
    /**
	 * Whether or not the comparable interface should be used of the compared columns
	 * instead of the plain strings
	 * Default value is <code>false</code> for backwards compatibility
     * Set whether or not to use the Comparable implementation of the corresponding column
     * DataType for comparing values or not. Default value is <code>false</code> 
     * which means that the old string comparison is used.
     * <br>
     * Fixes bug [ 1176380 ] Bad comparison in SortedTable
     * @param useComparable
	 * @since 2.3.0
     */
    public void setUseComparable(boolean useComparable)
    {
    	if(logger.isDebugEnabled())
    		logger.debug("setUseComparable(useComparable={}) - start", Boolean.valueOf(useComparable));
        
        if(_indexes != null)
        {
        	// TODO this is an ugly design to avoid increasing the number of constructors from 4 to 8. To be discussed how to implement it the best way.
        	throw new IllegalStateException("Do not use this method after the table has been used (i.e. #getValue() has been called). " +
        			"Please invoke this method immediately after the intialization of this object.");
        }
        
    	if(useComparable)
    	{
    		this.rowComparator = new RowComparator(this._table, this._columns);
    	}
    	else 
    	{
    		this.rowComparator = new RowComparatorByString(this._table, this._columns);
    	}
    }
    

    ////////////////////////////////////////////////////////////////////////////
    // ITable interface

    public ITableMetaData getTableMetaData()
    {
        logger.debug("getTableMetaData() - start");

        return _table.getTableMetaData();
    }

    public int getRowCount()
    {
        logger.debug("getRowCount() - start");

        return _table.getRowCount();
    }

    public Object getValue(int row, String columnName) throws DataSetException
    {
        if(logger.isDebugEnabled())
            logger.debug("getValue(row={}, columnName={}) - start", Integer.toString(row), columnName);

        assertValidRowIndex(row);

        return _table.getValue(getOriginalRowIndex(row), columnName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Comparator interface

    /**
     * Abstract class for sorting the table rows of a given table in a specific order
     */
    protected static abstract class AbstractRowComparator implements Comparator
    {
        /**
         * Logger for this class
         */
        private final Logger logger = LoggerFactory.getLogger(AbstractRowComparator.class);
        private ITable _table;
        private Column[] _sortColumns;

		/**
		 * @param table The wrapped table to be sorted
		 * @param sortColumns The columns to be used for sorting in the given order
		 */
		public AbstractRowComparator(ITable table, Column[] sortColumns)
		{
			this._table = table;
			this._sortColumns = sortColumns;
		}

		public int compare(Object o1, Object o2)
        {
            logger.debug("compare(o1={}, o2={}) - start", o1, o2);

            Integer i1 = (Integer)o1;
            Integer i2 = (Integer)o2;

            try
            {
                for (int i = 0; i < _sortColumns.length; i++)
                {
                    String columnName = _sortColumns[i].getColumnName();
                    
                    Object value1 = _table.getValue(i1.intValue(), columnName);
                    Object value2 = _table.getValue(i2.intValue(), columnName);

                    if (value1 == null && value2 == null)
                    {
                        continue;
                    }

                    if (value1 == null && value2 != null)
                    {
                        return -1;
                    }

                    if (value1 != null && value2 == null)
                    {
                        return 1;
                    }

                    // Compare the two values with each other for sorting
                    int result = compare(_sortColumns[i], value1, value2);
                    
                    if (result != 0)
                    {
                        return result;
                    }
                }
            }
            catch (DataSetException e)
            {
                throw new DatabaseUnitRuntimeException(e);
            }

            return 0;
        }

		/**
		 * @param column The column to be compared
		 * @param value1 The first value of the given column
		 * @param value2 The second value of the given column
		 * @return 0 if both values are considered equal.
		 * @throws TypeCastException
		 */
		protected abstract int compare(Column column, Object value1, Object value2) throws TypeCastException;

    }
    
    
    /**
     * Compares the rows with each other in order to sort them in the correct order using the
     * data type and the Comparable implementation the current column has.
     */
    protected static class RowComparator extends AbstractRowComparator
    {
        /**
         * Logger for this class
         */
        private final Logger logger = LoggerFactory.getLogger(RowComparator.class);

		public RowComparator(ITable table, Column[] sortColumns)
		{
			super(table, sortColumns);
		}

		protected int compare(Column column, Object value1, Object value2) throws TypeCastException 
		{
		    if(logger.isDebugEnabled())
		        logger.debug("compare(column={}, value1={}, value2={}) - start", 
		                new Object[]{column, value1, value2} );
			
			DataType dataType = column.getDataType();
			int result = dataType.compare(value1, value2);
			return result;
		}
		
    }
    
    
    /**
     * Compares the rows with each other in order to sort them in the correct order using
     * the string value of both values for the comparison.
     */
    protected static class RowComparatorByString extends AbstractRowComparator
    {
        /**
         * Logger for this class
         */
        private final Logger logger = LoggerFactory.getLogger(RowComparatorByString.class);

		public RowComparatorByString(ITable table, Column[] sortColumns)
		{
			super(table, sortColumns);
		}

		protected int compare(Column column, Object value1, Object value2) throws TypeCastException 
		{
		    if(logger.isDebugEnabled())
		        logger.debug("compare(column={}, value1={}, value2={}) - start", 
		                new Object[]{column, value1, value2} );

            // Default behavior since ever
			String stringValue1 = DataType.asString(value1);
			String stringValue2 = DataType.asString(value2);
			int result = stringValue1.compareTo(stringValue2);
			return result;
		}
    }
    
}





