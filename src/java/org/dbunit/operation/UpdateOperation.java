/*
 * UpdateOperation.java   Feb 19, 2002
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

import org.dbunit.dataset.*;
import org.dbunit.database.IDatabaseConnection;

import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

/**
 * Updates the database from the dataset contents. This operation assumes that
 * table data already exists in the database and fails if this is not the case.

 * @author Manuel Laflamme
 * @version $Revision$
 */
public class UpdateOperation extends AbstractBatchOperation
{
    UpdateOperation()
    {
    }

    ////////////////////////////////////////////////////////////////////////////
    // AbstractBatchOperation class

    public OperationData getOperationData(ITableMetaData metaData, BigInteger ignoreMapping, IDatabaseConnection connection) throws DataSetException
    {
        Column[] columns = metaData.getColumns();
        Column[] primaryKeys = metaData.getPrimaryKeys();

        // cannot construct where clause if no primary key
        if (primaryKeys.length == 0)
        {
            throw new NoPrimaryKeyException(metaData.getTableName());
        }

        // update table
        StringBuffer sqlBuffer = new StringBuffer(128);
        sqlBuffer.append("update ");
        sqlBuffer.append(getQualifiedName(connection.getSchema(),
                metaData.getTableName(), connection));

        // set
        boolean firstSet = true;
        List columnList = new ArrayList(columns.length);
        sqlBuffer.append(" set ");
        for (int i = 0; i < columns.length; i++)
        {
            Column column = columns[i];

            // set if not primary key
            if (DataSetUtils.getColumn(column.getColumnName(), primaryKeys) == null)
            {
                if (!firstSet)
                {
                    sqlBuffer.append(", ");
                }
                firstSet = false;

                // escape column name
                String columnName = getQualifiedName(null,
                        column.getColumnName(), connection);
                sqlBuffer.append(columnName);
                sqlBuffer.append(" = ?");
                columnList.add(column);
            }
        }

        // where
        sqlBuffer.append(" where ");
        for (int i = 0; i < primaryKeys.length; i++)
        {
            Column column = primaryKeys[i];

            if (i > 0)
            {
                sqlBuffer.append(" and ");
            }

            // escape column name
            String columnName = getQualifiedName(null,
                    column.getColumnName(), connection);
            sqlBuffer.append(columnName);
            sqlBuffer.append(" = ?");
            columnList.add(column);
        }

        return new OperationData(sqlBuffer.toString(),
                (Column[])columnList.toArray(new Column[0]));
    }

}






