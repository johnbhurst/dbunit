/*
 * DatabaseDataSet.java   Feb 17, 2002
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

package org.dbunit.database;

import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DataSetUtils;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchTableException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class DatabaseDataSet extends AbstractDataSet
{
    private final IDatabaseConnection _connection;
    private final Map _tableMap = new HashMap();
    private List _nameList = null;

    DatabaseDataSet(IDatabaseConnection connection) throws SQLException
    {
        _connection = connection;
    }

    static String getSelectStatement(String schema, ITableMetaData metaData, String escapePattern)
            throws DataSetException
    {
        Column[] columns = metaData.getColumns();
        Column[] primaryKeys = metaData.getPrimaryKeys();

        // select
        StringBuffer sqlBuffer = new StringBuffer(128);
        sqlBuffer.append("select ");
        for (int i = 0; i < columns.length; i++)
        {
            if (i > 0)
            {
                sqlBuffer.append(", ");
            }
            String columnName = DataSetUtils.getQualifiedName(null,
                    columns[i].getColumnName(), escapePattern);
            sqlBuffer.append(columnName);
        }

        // from
        sqlBuffer.append(" from ");
        sqlBuffer.append(DataSetUtils.getQualifiedName(schema,
                metaData.getTableName(), escapePattern));

        // order by
        for (int i = 0; i < primaryKeys.length; i++)
        {
            if (i == 0)
            {
                sqlBuffer.append(" order by ");
            }
            else
            {
                sqlBuffer.append(", ");
            }
            sqlBuffer.append(primaryKeys[i].getColumnName());

        }

        return sqlBuffer.toString();
    }

    private String getQualifiedName(String prefix, String name)
    {
        DatabaseConfig config = _connection.getConfig();
        boolean feature = config.getFeature(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES);
        if (feature)
        {
            return DataSetUtils.getQualifiedName(prefix, name);
        }
        return name;
    }

    /**
     * Get all the table names form the database that are not system tables.
     */
    private void initialize() throws DataSetException
    {
        if (_nameList != null)
        {
            return;
        }

        try
        {
            Connection jdbcConnection = _connection.getConnection();
            String schema = _connection.getSchema();
            String[] tableType = (String[])_connection.getConfig().getProperty(
                    DatabaseConfig.PROPERTY_TABLE_TYPE);

            DatabaseMetaData databaseMetaData = jdbcConnection.getMetaData();
            ResultSet resultSet = databaseMetaData.getTables(
                    null, schema, "%", tableType);

            try
            {
                List nameList = new ArrayList();
                while (resultSet.next())
                {
                    String schemaName = resultSet.getString(2);
                    String tableName = resultSet.getString(3);
//                    String tableType = resultSet.getString(4);
//                    System.out.println("schema=" + schemaName + ", table=" + tableName + ", type=" + tableType + "");
                    tableName = getQualifiedName(schemaName, tableName);

                    // prevent table name conflict
                    if (_tableMap.containsKey(tableName.toUpperCase()))
                    {
                        throw new AmbiguousTableNameException(tableName);
                    }
                    nameList.add(tableName);
                    _tableMap.put(tableName.toUpperCase(), null);
                }

                _nameList = nameList;
            }
            finally
            {
                resultSet.close();
            }
        }
        catch (SQLException e)
        {
            throw new DataSetException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // AbstractDataSet class

    protected ITableIterator createIterator(boolean reversed)
            throws DataSetException
    {
        String[] names = getTableNames();
        if (reversed)
        {
            names = DataSetUtils.reverseStringArray(names);
        }

        return new DatabaseTableIterator(names, this);
    }

    ////////////////////////////////////////////////////////////////////////////
    // IDataSet interface

    public String[] getTableNames() throws DataSetException
    {
        initialize();

        return (String[])_nameList.toArray(new String[0]);
    }

    public ITableMetaData getTableMetaData(String tableName) throws DataSetException
    {
        initialize();

        // Verify if table exist in the database
        String upperTableName = tableName.toUpperCase();
        if (!_tableMap.containsKey(upperTableName))
        {
            throw new NoSuchTableException(tableName);
        }

        // Try to find cached metadata
        ITableMetaData metaData = (ITableMetaData)_tableMap.get(upperTableName);
        if (metaData != null)
        {
            return metaData;
        }

        // Search for original database table name
        for (Iterator it = _nameList.iterator(); it.hasNext();)
        {
            String databaseTableName = (String)it.next();
            if (databaseTableName.equalsIgnoreCase(tableName))
            {
                // Create metadata and cache it
                metaData = new DatabaseTableMetaData(
                        databaseTableName, _connection);
                _tableMap.put(upperTableName, metaData);
                break;
            }
        }

        return metaData;
    }

    public ITable getTable(String tableName) throws DataSetException
    {
        initialize();

        try
        {
            ITableMetaData metaData = getTableMetaData(tableName);

            DatabaseConfig config = _connection.getConfig();
            IResultSetTableFactory factory = (IResultSetTableFactory)config.getProperty(
                    DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY);
            return factory.createTable(metaData, _connection);
        }
        catch (SQLException e)
        {
            throw new DataSetException(e);
        }
    }
}











