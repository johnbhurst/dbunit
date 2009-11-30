/*
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002-2008, DbUnit.org
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

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.IDataTypeFactory;

import junit.framework.TestCase;

/**
 * @author gommma (gommma AT users.sourceforge.net)
 * @author Last changed by: $Author$
 * @version $Revision$ $Date$
 * @since 2.4.0
 */
public class DatabaseConfigTest extends TestCase
{
    public void testSetProperty_InvalidType_Array() throws Exception
    {
        DatabaseConfig config = new DatabaseConfig();
        String simpleString = "TABLE";
        try {
            config.setProperty(DatabaseConfig.PROPERTY_TABLE_TYPE, simpleString);
            fail("The property 'table type' should be a string array");
        }
        catch(IllegalArgumentException expected){
            String expectedMsg = "Cannot cast object of type 'class java.lang.String' to allowed type 'class [Ljava.lang.String;'.";
            assertEquals(expectedMsg, expected.getMessage());
        }
    }
    
    public void testSetProperty_CorrectType_Array() throws Exception
    {
        DatabaseConfig config = new DatabaseConfig();
        String[] stringArray = new String[] {"TABLE"};
        config.setProperty(DatabaseConfig.PROPERTY_TABLE_TYPE, stringArray);
        assertEquals(stringArray, config.getProperty(DatabaseConfig.PROPERTY_TABLE_TYPE));
    }

    public void testSetProperty_Interface() throws Exception
    {
        DatabaseConfig config = new DatabaseConfig();
        IDataTypeFactory myFactory = new IDataTypeFactory() {
        
            public DataType createDataType(int sqlType, String sqlTypeName,
                    String tableName, String columnName) throws DataTypeException {
                return null;
            }
        
            public DataType createDataType(int sqlType, String sqlTypeName)
                    throws DataTypeException {
                return null;
            }
        };
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, myFactory);
        assertEquals(myFactory, config.getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY));
    }
    
    public void testSetPropertyToNullWhereNotAllowed() throws Exception
    {
        DatabaseConfig config = new DatabaseConfig();
        try {
            config.setProperty(DatabaseConfig.PROPERTY_BATCH_SIZE, null);
            assertEquals(null, config.getProperty(DatabaseConfig.PROPERTY_BATCH_SIZE));
            fail("Should not be able to set a not-nullable property to null");
        }
        catch(IllegalArgumentException expected){
            String expectedMsg = "The property 'http://www.dbunit.org/properties/batchSize' is not nullable.";
            assertEquals(expectedMsg, expected.getMessage());
        }
    }

    public void testSetPropertyToNullWhereAllowed() throws Exception
    {
        DatabaseConfig config = new DatabaseConfig();
        config.setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, null);
        assertEquals(null, config.getProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER));
    }

    public void testSetFeatureViaSetPropertyMethod() throws Exception
    {
        DatabaseConfig config = new DatabaseConfig();
        config.setProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, "true");
        assertEquals(Boolean.TRUE, config.getProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS));
        assertEquals(true, config.getFeature(DatabaseConfig.FEATURE_BATCHED_STATEMENTS));
    }

    public void testSetFeatureViaSetFeatureMethod() throws Exception
    {
        DatabaseConfig config = new DatabaseConfig();
        config.setFeature(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, true);
        assertEquals(Boolean.TRUE, config.getProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS));
        assertEquals(true, config.getFeature(DatabaseConfig.FEATURE_BATCHED_STATEMENTS));
    }

}
