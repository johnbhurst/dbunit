/*
 * Column.java   Feb 17, 2002
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

package org.dbunit.dataset;

import java.sql.DatabaseMetaData;

import org.dbunit.dataset.datatype.DataType;

/**
 * Represents a table column.
 *
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class Column
{
    /**
     * Indicates that the column might not allow <code>NULL</code> values.
     */
    public static final Nullable NO_NULLS = new Nullable("noNulls");
    /**
     * Indicates that the column definitely allows <code>NULL</code> values.
     */
    public static final Nullable NULLABLE = new Nullable("nullable");
    /**
     * Indicates that the nullability of columns is unknown.
     */
    public static final Nullable NULLABLE_UNKNOWN = new Nullable("nullableUnknown");

    private final String _columnName;
    private final DataType _dataType;
    private final String _sqlTypeName;
    private final Nullable _nullable;

    /**
     * Creates a Column object. This contructor set nullable to true.
     *
     * @param columnName the column name
     * @param dataType the data type
     */
    public Column(String columnName, DataType dataType)
    {
        _columnName = columnName;
        _dataType = dataType;
        _nullable = NULLABLE_UNKNOWN;
        _sqlTypeName = dataType.toString();
    }

    /**
     * Creates a Column object.
     */
    public Column(String columnName, DataType dataType, Nullable nullable)
    {
        _columnName = columnName;
        _dataType = dataType;
        _sqlTypeName = dataType.toString();
        _nullable = nullable;
    }

    /**
     * Creates a Column object.
     */
    public Column(String columnName, DataType dataType, String sqlTypeName,
            Nullable nullable)
    {
        _columnName = columnName;
        _dataType = dataType;
        _sqlTypeName = sqlTypeName;
        _nullable = nullable;
    }

    /**
     * Returns this column name.
     */
    public String getColumnName()
    {
        return _columnName;
    }

    /**
     * Returns this column data type.
     */
    public DataType getDataType()
    {
        return _dataType;
    }

    /**
     * Returns this column sql data type name.
     */
    public String getSqlTypeName()
    {
        return _sqlTypeName;
    }

    /**
     * Returns <code>true</code> if this column is nullable.
     */
    public Nullable getNullable()
    {
        return _nullable;
    }

    /**
     * Returns the appropriate Nullable constant according specified JDBC
     * DatabaseMetaData constant.
     *
     * @param nullable one of the following constants
     * {@link java.sql.DatabaseMetaData#columnNoNulls},
     * {@link java.sql.DatabaseMetaData#columnNullable},
     * {@link java.sql.DatabaseMetaData#columnNullableUnknown}
     */
    public static Nullable nullableValue(int nullable)
    {
        switch (nullable)
        {
            case DatabaseMetaData.columnNoNulls:
                return NO_NULLS;

            case DatabaseMetaData.columnNullable:
                return NULLABLE;

            case DatabaseMetaData.columnNullableUnknown:
                return NULLABLE_UNKNOWN;

            default:
                throw new IllegalArgumentException("Unknown constant value "
                        + nullable);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Object class

    public String toString()
    {
        return "(" + _columnName + ", " + _dataType + ", " + _nullable + ")";
//        return _columnName;
    }

    public static class Nullable
    {

        private final String _name;

        private Nullable(String name)
        {
            _name = name;
        }

        ////////////////////////////////////////////////////////////////////////////
        // Object class

        public String toString()
        {
            return _name;
        }
    }

}







