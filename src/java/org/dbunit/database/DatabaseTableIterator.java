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
package org.dbunit.database;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;

/**
 * @author Manuel Laflamme
 * @since Apr 12, 2003
 * @version $Revision$
 */
public class DatabaseTableIterator implements ITableIterator
{
    private final String[] _tableNames;
    private final IDataSet _dataSet;
    private IResultSetTable _currentTable;
    private int _index = -1;

    public DatabaseTableIterator(String[] tableNames, IDataSet dataSet)
    {
        _tableNames = tableNames;
        _dataSet = dataSet;
        _currentTable = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // ITableIterator interface

    public boolean next() throws DataSetException
    {
        _index++;

        // Ensure previous table is closed
        if (_currentTable != null)
        {
            _currentTable.close();
            _currentTable = null;
        }

        return _index < _tableNames.length;
    }

    public ITableMetaData getTableMetaData() throws DataSetException
    {
        return _dataSet.getTableMetaData(_tableNames[_index]);
    }

    public ITable getTable() throws DataSetException
    {
        if (_currentTable == null)
        {
            _currentTable = (IResultSetTable)_dataSet.getTable(_tableNames[_index]);
        }
        return _currentTable;
    }
}
