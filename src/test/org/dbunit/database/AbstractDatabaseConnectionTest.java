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

import java.sql.SQLException;

import org.dbunit.AbstractDatabaseTest;
import org.dbunit.DatabaseProfile;
import org.dbunit.IDatabaseTester;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 * @since Mar 26, 2002
 */
public abstract class AbstractDatabaseConnectionTest extends AbstractDatabaseTest
{
	
    public AbstractDatabaseConnectionTest(String s)
    {
        super(s);
    }

    
	public final void testGetRowCount() throws Exception
    {
    	System.out.println("1: " + _connection);
        assertEquals("EMPTY_TABLE", 0, _connection.getRowCount("empty_table", null));
        assertEquals("EMPTY_TABLE", 0, _connection.getRowCount("empty_table"));

        assertEquals("TEST_TABLE", 6, _connection.getRowCount("test_table", null));
        assertEquals("TEST_TABLE", 6, _connection.getRowCount("test_table"));

        assertEquals("PK_TABLE", 1, _connection.getRowCount("pk_table", "where PK0 = 0"));
    }

    public final void testGetRowCount_NonexistingSchema() throws Exception
    {
    	DatabaseProfile profile = super.getEnvironment().getProfile();
    	String nonexistingSchema = profile.getSchema() + "_444_XYZ_TEST";

    	IDatabaseTester dbTester = this.newDatabaseTester(nonexistingSchema);;
    	try {
			IDatabaseConnection dbConnection = dbTester.getConnection();
			
			assertEquals(nonexistingSchema, dbConnection.getSchema());
			try {
				dbConnection.getRowCount("test_table");
				fail("Should not be able to retrieve row count for non-existing schema " + nonexistingSchema);
			}
			catch(SQLException expected)
			{
				// All right
			}
    	}
    	finally {
    		// Reset the testers schema for subsequent tests (environment.dbTester is a singleton)
    		dbTester.setSchema(profile.getSchema());    		
    	}
    }

    public final void testGetRowCount_NoSchemaSpecified() throws Exception
    {
    	DatabaseProfile profile = super.getEnvironment().getProfile();
    	IDatabaseTester dbTester = this.newDatabaseTester(null);
    	try {
			IDatabaseConnection dbConnection = dbTester.getConnection();
			
			assertEquals(null, dbConnection.getSchema());
	        assertEquals("TEST_TABLE", 6, _connection.getRowCount("test_table", null));
    	}
    	finally {
    		// Reset the testers schema for subsequent tests (environment.dbTester is a singleton)
    		dbTester.setSchema(profile.getSchema());    		
    	}
    }

    
    private IDatabaseTester newDatabaseTester(String schema) throws Exception {
    	IDatabaseTester tester = super.newDatabaseTester();
    	tester.setSchema(schema);
    	return tester;
	}

}


