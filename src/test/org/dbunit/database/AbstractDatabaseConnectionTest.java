/*
 * AbstractDatabaseConnectionTest.java   Mar 26, 2002
 *
 * DbUnit Database Testing Framework
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

import org.dbunit.AbstractDatabaseTest;

/**
 * @author Manuel Laflamme
 * @version 1.0
 */
public abstract class AbstractDatabaseConnectionTest extends AbstractDatabaseTest
{
    public AbstractDatabaseConnectionTest(String s)
    {
        super(s);
    }

    public final void testGetRowCount() throws Exception
    {
        assertEquals("EMPTY_TABLE", 0, _connection.getRowCount("EMPTY_TABLE", null));
        assertEquals("EMPTY_TABLE", 0, _connection.getRowCount("EMPTY_TABLE"));

        assertEquals("TEST_TABLE", 6, _connection.getRowCount("TEST_TABLE", null));
        assertEquals("TEST_TABLE", 6, _connection.getRowCount("TEST_TABLE"));

        assertEquals("PK_TABLE", 1, _connection.getRowCount("PK_TABLE", "where PK0 = 0"));
    }
}
