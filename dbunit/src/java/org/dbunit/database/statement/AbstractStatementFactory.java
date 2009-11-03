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

package org.dbunit.database.statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;

import java.sql.SQLException;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 * @since Apr 10, 2002
 */
public abstract class AbstractStatementFactory implements IStatementFactory
{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(AbstractStatementFactory.class);

    /**
     * Returns <code>true</code> if target database supports batch statement.
     */
    protected boolean supportBatchStatement(IDatabaseConnection connection)
            throws SQLException
    {
        logger.debug("supportBatchStatement(connection={}) - start", connection);

        if (connection.getConfig().getFeature(DatabaseConfig.FEATURE_BATCHED_STATEMENTS))
        {
            return connection.getConnection().getMetaData().supportsBatchUpdates();
        }

        return false;
    }
}

