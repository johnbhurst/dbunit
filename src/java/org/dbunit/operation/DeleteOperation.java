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

    public OperationData getOperationData(String schemaName,
            ITableMetaData metaData) throws DataSetException
    {
        // cannot construct where clause if no primary key
        Column[] primaryKeys = metaData.getPrimaryKeys();
        if (primaryKeys.length == 0)
        {
            throw new NoPrimaryKeyException(metaData.getTableName());
        }

        // delete from
        StringBuffer sqlBuffer = new StringBuffer(128);
        sqlBuffer.append("delete from ");
        sqlBuffer.append(DataSetUtils.getQualifiedName(
                schemaName, metaData.getTableName()));

        // where
        sqlBuffer.append(" where ");
        for (int i = 0; i < primaryKeys.length; i++)
        {
            sqlBuffer.append(primaryKeys[i].getColumnName());
            sqlBuffer.append(" = ?");
            if (i + 1 < primaryKeys.length)
            {
                sqlBuffer.append(" and ");
            }
        }

        return new OperationData(sqlBuffer.toString(), primaryKeys);
    }

}




