/*
 * AbstractBatchStatement.java   Mar 15, 2002
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

package org.dbunit.database.statement;

import java.sql.*;

/**
 * @author Manuel Laflamme
 * @version 1.0
 */
public abstract class AbstractBatchStatement implements IBatchStatement
{
    protected final Statement _statement;

    AbstractBatchStatement(Connection connection) throws SQLException
    {
        _statement = connection.createStatement();
    }

    public void close() throws SQLException
    {
        _statement.close();
    }


}

