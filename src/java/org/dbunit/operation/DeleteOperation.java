/*
 * DeleteOperation.java   Feb 19, 2002
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

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.*;

/**
 * Deletes only the dataset contents from the database. This operation does not
 * delete the entire table contents but only data that are present in the
 * dataset.
 *
 * @author Manuel Laflamme
 * @version 1.0
 */
public class DeleteOperation extends AbstractBatchOperation
{
    DeleteOperation()
    {
    }

    ////////////////////////////////////////////////////////////////////////////
    // AbstractBatchOperation class

    protected String[] getTableNames(IDataSet dataSet) throws DatabaseUnitException
    {
        return DataSetUtils.getReverseTableNames(dataSet);
    }

    protected String getOperationStatement(String schemaName, ITable table,
            int row) throws DatabaseUnitException
    {
        ITableMetaData metaData = table.getTableMetaData();
        Column[] primaryKeys = metaData.getPrimaryKeys();

        // cannot construct where clause if no primary key
        if (primaryKeys.length == 0)
        {
            throw new NoPrimaryKeyException(metaData.getTableName());
        }

        // delete from
        String sql = "delete from " + DataSetUtils.getAbsoluteName(schemaName,
                metaData.getTableName());

        // where
        sql += " where ";
        for (int i = 0; i < primaryKeys.length; i++)
        {
            Column key = primaryKeys[i];
            Object value = table.getValue(row, key.getColumnName());
            sql += key.getColumnName() + " = " +
                    DataSetUtils.getSqlValueString(value, key.getDataType());
            if (i + 1 < primaryKeys.length)
            {
                sql += " and ";
            }
        }

        return sql;
    }

}

