/*
 * Db2DataTypeFactory.java
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

package org.dbunit.ext.db2;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.datatype.StringDataType;

import java.sql.Types;

public class Db2DataTypeFactory extends DefaultDataTypeFactory
{
    static final DataType DB2XML_XMLVARCHAR = new StringDataType(
            "DB2XML.XMLVARCHAR", Types.DISTINCT);
    static final DataType DB2XML_XMLCLOB = new StringDataType(
            "DB2XML.XMLCLOB", Types.DISTINCT);
    static final DataType DB2XML_XMLFILE = new StringDataType(
            "DB2XML.XMLFILE", Types.DISTINCT);

    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException
    {
        if (sqlType == Types.DISTINCT)
        {
            if (sqlTypeName.equals(DB2XML_XMLVARCHAR.toString()))
            {
                return DB2XML_XMLVARCHAR;
            }

            if (sqlTypeName.equals(DB2XML_XMLCLOB.toString()))
            {
                return DB2XML_XMLCLOB;
            }

            if (sqlTypeName.equals(DB2XML_XMLFILE.toString()))
            {
                return DB2XML_XMLFILE;
            }
        }

        return super.createDataType(sqlType, sqlTypeName);
    }
}
