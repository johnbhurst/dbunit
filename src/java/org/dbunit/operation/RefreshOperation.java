/*
 * RefreshOperation.java   Feb 19, 2002
 *
 * The dbUnit database testing framework.
 * Copyright (C) 2002   Manuel Laflamme
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

import java.sql.SQLException;
import java.sql.Statement;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IPreparedBatchStatement;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.*;

/**
 * This operation literally refreshes dataset contents into the database. This
 * means that data of existing rows is updated and non-existing row get
 * inserted. Any rows which exist in the database but not in dataset stay
 * unaffected.
 *
 * @author Manuel Laflamme
 * @version 1.0
 */
public class RefreshOperation extends DatabaseOperation
{
    private static final InsertOperation INSERT =
            (InsertOperation)DatabaseOperation.INSERT;
    private static final UpdateOperation UPDATE =
            (UpdateOperation)DatabaseOperation.UPDATE;

    RefreshOperation()
    {
    }

    protected int executeRowOperation(IPreparedBatchStatement statement,
            Column[] columns, ITable table, int row)
            throws DatabaseUnitException, SQLException
    {
        for (int i = 0; i < columns.length; i++)
        {
            Object value = table.getValue(row, columns[i].getColumnName());
            statement.addValue(value, columns[i].getDataType());
        }
        statement.addBatch();
        int result = statement.executeBatch();
        statement.clearBatch();

        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // DatabaseOperation class

    public void execute(IDatabaseConnection connection, IDataSet dataSet)
            throws DatabaseUnitException, SQLException
    {
        IStatementFactory factory = connection.getStatementFactory();
        String schema = connection.getSchema();

        // this dataset is used to get metadata from database
        IDataSet databaseDataSet = connection.createDataSet();

        // for each table
        ITable[] tables = DataSetUtils.getTables(dataSet);
        for (int i = 0; i < tables.length; i++)
        {
            // use database metadata to get columns datatype
            String tableName = tables[i].getTableMetaData().getTableName();
            ITableMetaData metaData = databaseDataSet.getTableMetaData(tableName);

            // setup insert statement    <
            OperationData insertData = INSERT.getOperationData(schema, metaData);
            IPreparedBatchStatement insertStatement =
                    factory.createPreparedStatement(insertData.getSql(), connection);

            // setup update statement
            OperationData updateData = UPDATE.getOperationData(schema, metaData);
            IPreparedBatchStatement updateStatement =
                    factory.createPreparedStatement(updateData.getSql(), connection);

            // refresh each table's row
            ITable table = dataSet.getTable(tableName);
            for (int j = 0; j < table.getRowCount(); j++)
            {
                // try to update row
                int result = executeRowOperation(updateStatement,
                        updateData.getColumns(), table, j);
                if (result == 0)
                {
                    // update not sucessful, try insert instead
                    executeRowOperation(insertStatement,
                            insertData.getColumns(), table, j);
                }
            }
        }

    }
}


