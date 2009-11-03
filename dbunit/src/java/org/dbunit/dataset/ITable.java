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

package org.dbunit.dataset;


/**
 * A table represents a collection of tabular data.
 *
 * @author Manuel Laflamme
 * @version $Revision$
 * @since Feb 17, 2002
 */
public interface ITable
{
    public static final Object NO_VALUE = new Object();

    /**
     * Returns this table metadata.
     */
    public ITableMetaData getTableMetaData();

    /**
     * Returns this table row count.
     */
    public int getRowCount();

    /**
     * Returns this table value for the specified row and column.
     * @param row The row index, starting with 0
     * @param column The name of the column
     * @return The value
     *
     * @throws NoSuchColumnException if specified column name do not exist in
     * this table
     * @throws RowOutOfBoundsException if specified row is less than zero or
     * equals or greater than <code>getRowCount</code>
     */
    public Object getValue(int row, String column) throws DataSetException;
}






