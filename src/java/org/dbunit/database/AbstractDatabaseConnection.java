/*
 * AbstractDatabaseConnection.java   Mar 6, 2002
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

package org.dbunit.database;

import java.sql.*;

import org.dbunit.dataset.*;

/**
 * @author Manuel Laflamme
 * @version 1.0
 */
public abstract class AbstractDatabaseConnection implements IDatabaseConnection
{
    private IDataSet _dataSet = null;

    public IDataSet createDataSet() throws SQLException
    {
        if (_dataSet == null)
        {
            _dataSet = new DatabaseDataSet(this);
        }

        return _dataSet;
    }

    public IDataSet createDataSet(String[] tableNames) throws SQLException
    {
        return new FilteredDataSet(tableNames, createDataSet());
    }

    public ITable createQueryTable(String resultName, String sql)
            throws DataSetException, SQLException
    {
        Statement statement = getConnection().createStatement();
        try
        {
            ResultSet resultSet = statement.executeQuery(sql);

            try
            {
                ITableMetaData metaData = ResultSetTable.createTableMetaData(
                        resultName, resultSet);
                return new CachedResultSetTable(metaData, resultSet);
            }
            finally
            {
                resultSet.close();
            }
        }
        finally
        {
            statement.close();
        }
    }

    public BatchStatement createBatchStatement() throws SQLException
    {
        if (getConnection().getMetaData().supportsBatchUpdates())
        {
            return new BatchStatement(getConnection());
        }
        else
        {
            return new SimpleStatement(getConnection());
        }
    }


}
