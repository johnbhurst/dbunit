/*
 * DatabaseOperation.java   Feb 18, 2002
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
import org.dbunit.dataset.IDataSet;

import java.sql.SQLException;

/**
 * Defines the interface contract for operations performed on the database.
 *
 * @author Manuel Laflamme
 * @version $Revision$
 */
public abstract class DatabaseOperation
{
    public static final DatabaseOperation NONE = new DummyOperation();
    public static final DatabaseOperation UPDATE = new UpdateOperation();
    public static final DatabaseOperation INSERT = new InsertOperation();
    public static final DatabaseOperation REFRESH = new RefreshOperation();
    public static final DatabaseOperation DELETE = new DeleteOperation();
    public static final DatabaseOperation DELETE_ALL = new DeleteAllOperation();
    public static final DatabaseOperation TRUNCATE_TABLE = new TruncateTableOperation();
    public static final DatabaseOperation CLEAN_INSERT = new CompositeOperation(
            DELETE_ALL, INSERT);

    /**
     * Executes this operation on the specified database using the specified
     * dataset contents.
     *
     * @param connection the database connection.
     * @param dataSet the dataset to be used by this operation.
     */
    public abstract void execute(IDatabaseConnection connection,
            IDataSet dataSet) throws DatabaseUnitException, SQLException;

    private static class DummyOperation extends DatabaseOperation
    {
        public void execute(IDatabaseConnection connection, IDataSet dataSet)
        {
        }
    }
}






