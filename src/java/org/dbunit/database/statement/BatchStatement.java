/*
 * BatchStatement.java   Feb 20, 2002
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

package org.dbunit.database.statement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 */
public class BatchStatement extends AbstractBatchStatement
{
    BatchStatement(Connection connection) throws SQLException
    {
        super(connection);
    }

    public void addBatch(String sql) throws SQLException
    {
        _statement.addBatch(sql);
    }

    public int executeBatch() throws SQLException
    {
        int[] results = _statement.executeBatch();
        int result = 0;
        for (int i = 0; i < results.length; i++)
        {
            result += results[i];
        }
        return result;
    }

    public void clearBatch() throws SQLException
    {
        _statement.clearBatch();
    }

}






