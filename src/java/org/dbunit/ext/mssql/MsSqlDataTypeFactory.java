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
package org.dbunit.ext.mssql;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;

/**
 * Specialized factory that recognizes MS SQL Server data types.
 *
 * @author Manuel Laflamme
 * @since May 19, 2003
 * @version $Revision$
 */
public class MsSqlDataTypeFactory extends DefaultDataTypeFactory
{
    public static final int NCHAR = -8;
    public static final int NVARCHAR = -9;
    public static final int NTEXT = -10;

    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException
    {
        // TODO : Process MS SQL Server custom datatype here
        switch(sqlType) {
            case NCHAR: return DataType.CHAR; // nchar
            case NVARCHAR: return DataType.VARCHAR; // nvarchar
            case NTEXT: return DataType.LONGVARCHAR; // ntext
            default: return super.createDataType(sqlType, sqlTypeName);
        }
    }
}
