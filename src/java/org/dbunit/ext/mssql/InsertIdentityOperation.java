/*
 * InsertIdentityOperation.java
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
 */

package org.dbunit.ext.mssql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.ExclusiveTransactionException;
import org.dbunit.operation.AbstractOperation;

/**
 * This class disable the MS SQL Server automatic identifier generation for
 * the execution of inserts.
 * <p>
 * If you are using the Microsoft driver (i.e.
 * <code>com.microsoft.jdbc.sqlserver.SQLServerDriver</code>), you'll need to
 * use the <code>SelectMethod=cursor</code> parameter in the JDBC connection
 * string. Your databaseUrl would look something like the following:
 * <p>
 * <code>jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=mydb;SelectMethod=cursor</code>
 * <p>
 * Thanks to Jeremy Stein how have submited multiple patches.
 *
 * @author Manuel Laflamme
 * @author Eric Pugh
 * @version $Revision$
 * @since Apr 9, 2002
 */
public class InsertIdentityOperation extends AbstractOperation
{
    public static final DatabaseOperation INSERT =
            new InsertIdentityOperation(DatabaseOperation.INSERT);

    public static final DatabaseOperation CLEAN_INSERT =
            new CompositeOperation(DatabaseOperation.DELETE_ALL,
                    new InsertIdentityOperation(DatabaseOperation.INSERT));

    public static final DatabaseOperation REFRESH =
            new InsertIdentityOperation(DatabaseOperation.REFRESH);

    private final DatabaseOperation _operation;

    /**
     * Creates a new InsertIdentityOperation object that decorates the
     * specified operation.
     */
    public InsertIdentityOperation(DatabaseOperation operation)
    {
        _operation = operation;
    }

    protected boolean hasIdentityColumn(ITableMetaData metaData)
            throws DataSetException
    {
        // check all columns to see if they are an identity column
        Column[] columns = metaData.getColumns();

        for (int i = 0; i < columns.length; i++)
        {
            if (columns[i].getSqlTypeName().endsWith("identity"))
            {
                return true;
            }
        }

        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // DatabaseOperation class

    public void execute(IDatabaseConnection connection, IDataSet dataSet)
            throws DatabaseUnitException, SQLException
    {
        Connection jdbcConnection = connection.getConnection();
        Statement statement = jdbcConnection.createStatement();

        try
        {
            IDataSet databaseDataSet = connection.createDataSet();
            

            // INSERT_IDENTITY need to be enabled/disabled inside the
            // same transaction
            if (jdbcConnection.getAutoCommit() == false)
            {
                throw new ExclusiveTransactionException();
            }
            jdbcConnection.setAutoCommit(false);

            // Execute decorated operation one table at a time
            ITableIterator iterator = dataSet.iterator();
            while(iterator.next())
            {
                ITable table = iterator.getTable();
                String tableName = table.getTableMetaData().getTableName();

                ITableMetaData metaData =
                        databaseDataSet.getTableMetaData(tableName);

                // enable identity insert
                boolean hasIdentityColumn = hasIdentityColumn(metaData);

                if (hasIdentityColumn)
                {
                    StringBuffer sqlBuffer = new StringBuffer(128);
                    sqlBuffer.append("SET IDENTITY_INSERT ");
                    sqlBuffer.append(getQualifiedName(connection.getSchema(),
                            metaData.getTableName(), connection));
                    sqlBuffer.append(" ON");
                    statement.execute(sqlBuffer.toString());
                }

                try
                {
                    _operation.execute(connection, new DefaultDataSet(table));
                }
                finally
                {
                    // disable identity insert
                    if (hasIdentityColumn)
                    {
                        StringBuffer sqlBuffer = new StringBuffer(128);
                        sqlBuffer.append("SET IDENTITY_INSERT ");
                        sqlBuffer.append(getQualifiedName(connection.getSchema(),
                                metaData.getTableName(), connection));
                        sqlBuffer.append(" OFF");
                        statement.execute(sqlBuffer.toString());
                    }
                    jdbcConnection.commit();
                }
            }
        }
        finally
        {
            jdbcConnection.setAutoCommit(true);
            statement.close();
        }
    }
}






