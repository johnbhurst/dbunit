/*
 * RefreshOperation.java   Feb 19, 2002
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

package org.dbunit.operation;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IPreparedBatchStatement;
import org.dbunit.database.statement.SimplePreparedStatement;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoPrimaryKeyException;
import org.dbunit.dataset.RowOutOfBoundsException;
import org.dbunit.dataset.datatype.DataType;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This operation literally refreshes dataset contents into the database. This
 * means that data of existing rows is updated and non-existing row get
 * inserted. Any rows which exist in the database but not in dataset stay
 * unaffected.
 *
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class RefreshOperation extends DatabaseOperation
{
    private final InsertOperation _insertOperation;
    private final UpdateOperation _updateOperation;

    RefreshOperation()
    {
        _insertOperation = (InsertOperation)DatabaseOperation.INSERT;
        _updateOperation = (UpdateOperation)DatabaseOperation.UPDATE;
    }

    private boolean isEmpty(ITable table) throws DataSetException
    {
        return AbstractBatchOperation.isEmpty(table);
    }

    private ITableMetaData getOperationMetaData(IDatabaseConnection connection,
            ITableMetaData metaData) throws DatabaseUnitException, SQLException
    {
        return AbstractBatchOperation.getOperationMetaData(
                            connection, metaData);
    }

    ////////////////////////////////////////////////////////////////////////////
    // DatabaseOperation class

    public void execute(IDatabaseConnection connection, IDataSet dataSet)
            throws DatabaseUnitException, SQLException
    {
        
        // for each table
        ITableIterator iterator = dataSet.iterator();
        while (iterator.next())
        {
            ITable table = iterator.getTable();

            // Do not process empty table
            if (isEmpty(table))
            {
                continue;
            }

            ITableMetaData metaData = getOperationMetaData(connection,
                    table.getTableMetaData());
            RowOperation updateRowOperation = createUpdateOperation(connection,
                    metaData);
            RowOperation insertRowOperation = new InsertRowOperation(connection,
                    metaData);

            try
            {
                // refresh all rows
                for (int i = 0; ; i++)
                {
                    if (!updateRowOperation.execute(table, i))
                    {
                        insertRowOperation.execute(table, i);
                    }
                }
            }
            catch (RowOutOfBoundsException e)
            {
                // end of table
            }
            finally
            {
                // cleanup
                updateRowOperation.close();
                insertRowOperation.close();
            }
        }

    }

    private RowOperation createUpdateOperation(IDatabaseConnection connection,
            ITableMetaData metaData)
            throws DataSetException, SQLException
    {
        // update only if columns are not all primary keys
        if (metaData.getColumns().length > metaData.getPrimaryKeys().length)
        {
            return new UpdateRowOperation(connection, metaData);
        }

        // otherwise, operation only verify if row exist
        return new RowExistOperation(connection,  metaData);
    }

    /**
     * This class represents a operation executable on a single table row.
     */
    class RowOperation
    {
        protected IPreparedBatchStatement _statement;
        protected OperationData _operationData;
        protected BigInteger _ignoreMapping;

        /**
         * Execute this operation on the sepcfied table row.
         * @return <code>true</code> if operation have been executed on the row.
         */
        public boolean execute(ITable table, int row)
                throws DataSetException, SQLException
        {
            Column[] columns = _operationData.getColumns();
            for (int i = 0; i < columns.length; i++)
            {
                // Bind value only if not in ignore mapping
                if (_ignoreMapping == null || !_ignoreMapping.testBit(i))
                {
                    Object value = table.getValue(row, columns[i].getColumnName());
                    _statement.addValue(value, columns[i].getDataType());
                }
            }
            _statement.addBatch();
            int result = _statement.executeBatch();
            _statement.clearBatch();

            return result == 1;
        }

        /**
         * Cleanup this operation state.
         */
        public void close() throws SQLException
        {
            if (_statement != null)
            {
                _statement.close();
            }
        }
    }

    /**
     * Insert row operation.
     */
    private class InsertRowOperation extends RowOperation
    {
        private IDatabaseConnection _connection;
        private ITableMetaData _metaData;

        public InsertRowOperation(IDatabaseConnection connection,
                ITableMetaData metaData)
                throws DataSetException, SQLException
        {
            _connection = connection;
            _metaData = metaData;
        }

        public boolean execute(ITable table, int row)
                throws DataSetException, SQLException
        {
            // If current row have a diffrent ignore value mapping than
            // previous one, we generate a new statement
            if (_ignoreMapping == null ||
                    !_insertOperation.equalsIgnoreMapping(_ignoreMapping, table, row))
            {
                // Execute and close previous statement
                if (_statement != null)
                {
                    _statement.close();
                }

                _ignoreMapping = _insertOperation.getIngnoreMapping(table, row);
                _operationData = _insertOperation.getOperationData(_metaData,
                        _ignoreMapping, _connection);
                _statement = new SimplePreparedStatement(_operationData.getSql(),
                        _connection.getConnection());
            }

            return super.execute(table, row);
        }

    }

    /**
     * Update row operation.
     */
    private class UpdateRowOperation extends RowOperation
    {
        PreparedStatement _countStatement;

        public UpdateRowOperation(IDatabaseConnection connection,
                ITableMetaData metaData)
                throws DataSetException, SQLException
        {
            // setup update statement
            _operationData = _updateOperation.getOperationData(
                    metaData, null, connection);
            _statement = new SimplePreparedStatement(_operationData.getSql(),
                    connection.getConnection());
        }
    }

    /**
     * This operation verify if a row exists in the database.
     */
    private class RowExistOperation extends RowOperation
    {
        PreparedStatement _countStatement;

        public RowExistOperation(IDatabaseConnection connection,
                ITableMetaData metaData)
                throws DataSetException, SQLException
        {
            // setup select count statement
            _operationData = getSelectCountData(metaData, connection);
            _countStatement = connection.getConnection().prepareStatement(
                    _operationData.getSql());
        }

        private OperationData getSelectCountData(
                ITableMetaData metaData, IDatabaseConnection connection) throws DataSetException
        {
            Column[] primaryKeys = metaData.getPrimaryKeys();

            // cannot construct where clause if no primary key
            if (primaryKeys.length == 0)
            {
                throw new NoPrimaryKeyException(metaData.getTableName());
            }

            // select count
            StringBuffer sqlBuffer = new StringBuffer(128);
            sqlBuffer.append("select COUNT(*) from ");
            sqlBuffer.append(getQualifiedName(connection.getSchema(),
                    metaData.getTableName(), connection));

            // where
            sqlBuffer.append(" where ");
            for (int i = 0; i < primaryKeys.length; i++)
            {
                Column column = primaryKeys[i];

                if (i > 0)
                {
                    sqlBuffer.append(" and ");
                }
                sqlBuffer.append(column.getColumnName());
                sqlBuffer.append(" = ?");
            }

            return new OperationData(sqlBuffer.toString(), primaryKeys);
        }

        ////////////////////////////////////////////////////////////////////////
        // RowOperation class

        /**
         * Verify if the specified table row exists in the database.
         * @return <code>true</code> if row exists.
         */
        public boolean execute(ITable table, int row)
                throws DataSetException, SQLException
        {
            Column[] columns = _operationData.getColumns();
            for (int i = 0; i < columns.length; i++)
            {
                Object value = table.getValue(row, columns[i].getColumnName());
                DataType dataType = columns[i].getDataType();
                dataType.setSqlValue(value, i + 1, _countStatement);
            }

            ResultSet resultSet = _countStatement.executeQuery();
            try
            {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
            finally
            {
                resultSet.close();
            }
        }

        public void close() throws SQLException
        {
            _countStatement.close();
        }
    }

}













