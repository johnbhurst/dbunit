/*
 * CachedResultSetTable.java   Feb 20, 2002
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

import org.dbunit.dataset.CachedTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.IDataTypeFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class CachedResultSetTable extends CachedTable implements IResultSetTable
{
    public CachedResultSetTable(ITableMetaData metaData, ResultSet resultSet
            ) throws SQLException, DataSetException
    {
        this(new ForwardOnlyResultSetTable(metaData, resultSet));
    }

    public CachedResultSetTable(ITableMetaData metaData,
            IDatabaseConnection connection) throws SQLException, DataSetException
    {
        this(new ForwardOnlyResultSetTable(metaData, connection));
    }

    public CachedResultSetTable(IResultSetTable table) throws DataSetException, SQLException
    {
        super(table.getTableMetaData());
        try
        {
            _rowList.addAll(createRowList(table));
        }
        finally
        {
            table.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // IResultSetTable interface

    public void close() throws DataSetException
    {
        // nothing to do, already closed
    }
}





