/*
 * AbstractDatabaseConnectionTest.java   Mar 26, 2002
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

import org.dbunit.AbstractDatabaseTest;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 */
public abstract class AbstractDatabaseConnectionTest extends AbstractDatabaseTest
{
    public AbstractDatabaseConnectionTest(String s)
    {
        super(s);
    }

    public final void testGetRowCount() throws Exception
    {
        assertEquals("EMPTY_TABLE", 0, _connection.getRowCount("empty_table", null));
        assertEquals("EMPTY_TABLE", 0, _connection.getRowCount("empty_table"));

        assertEquals("TEST_TABLE", 6, _connection.getRowCount("test_table", null));
        assertEquals("TEST_TABLE", 6, _connection.getRowCount("test_table"));

        assertEquals("PK_TABLE", 1, _connection.getRowCount("pk_table", "where PK0 = 0"));
    }
}


