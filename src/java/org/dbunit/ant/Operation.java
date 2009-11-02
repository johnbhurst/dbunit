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

package org.dbunit.ant;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.TransactionOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;

/**
 * The <code>Operation</code> class is the step that defines which
 * operation will be performed in the execution of the <code>DbUnitTask</code>
 * task.
 *
 * @author Timothy Ruppert
 * @author Ben Cox
 * @version $Revision$
 * @since Jun 10, 2002
 * @see org.dbunit.ant.DbUnitTaskStep
 */
public class Operation extends AbstractStep
{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(Operation.class);

    private static final String DEFAULT_FORMAT = FORMAT_FLAT;

    protected String _type = "CLEAN_INSERT";
    private String _format;
    private File _src;
    private boolean _transaction = false;
    private DatabaseOperation _operation;
    private boolean _forwardOperation = true;
    private String _nullToken;

    public File getSrc()
    {
        return _src;
    }

    public void setSrc(File src)
    {
        _src = src;
    }

    public String getFormat()
    {
        return _format != null ? _format : DEFAULT_FORMAT;
    }

    public void setFormat(String format)
    {
        logger.debug("setFormat(format={}) - start", format);

        // Check if the given format is accepted
        checkDataFormat(format);
        // If we get here the given format is a valid data format
        _format = format;
    }

    public boolean isTransaction()
    {
        return _transaction;
    }

    public void setTransaction(boolean transaction)
    {
        _transaction = transaction;
    }

    public String getNullToken() 
    {
        return _nullToken;
    }

    public void setNullToken(final String nullToken) 
    {
        this._nullToken = nullToken;
    }

    public DatabaseOperation getDbOperation()
    {
        return _operation;
    }

    public String getType()
    {
        return _type;
    }

    public void setType(String type) 
    {
        logger.debug("setType(type={}) - start", type);

        if ("UPDATE".equals(type)) {
            _operation = DatabaseOperation.UPDATE;
            _forwardOperation = true;
        } else if ("INSERT".equals(type)) {
            _operation = DatabaseOperation.INSERT;
            _forwardOperation = true;
        } else if ("REFRESH".equals(type)) {
            _operation = DatabaseOperation.REFRESH;
            _forwardOperation = true;
        } else if ("DELETE".equals(type)) {
            _operation = DatabaseOperation.DELETE;
            _forwardOperation = false;
        } else if ("DELETE_ALL".equals(type)) {
            _operation = DatabaseOperation.DELETE_ALL;
            _forwardOperation = false;
        } else if ("CLEAN_INSERT".equals(type)) {
            _operation = DatabaseOperation.CLEAN_INSERT;
            _forwardOperation = false;
        } else if ("NONE".equals(type)) {
            _operation = DatabaseOperation.NONE;
            _forwardOperation = true;
        } else if ("MSSQL_CLEAN_INSERT".equals(type)) {
            _operation = InsertIdentityOperation.CLEAN_INSERT;
            _forwardOperation = false;
        } else if ("MSSQL_INSERT".equals(type)) {
            _operation = InsertIdentityOperation.INSERT;
            _forwardOperation = true;
        } else if ("MSSQL_REFRESH".equals(type)) {
            _operation = InsertIdentityOperation.REFRESH;
            _forwardOperation = true;
        } else {
            throw new IllegalArgumentException("Type must be one of: UPDATE, INSERT,"
                    + " REFRESH, DELETE, DELETE_ALL, CLEAN_INSERT, MSSQL_INSERT, "
                    + " or MSSQL_REFRESH but was: " + type);
        }
        _type = type;
    }

    public void execute(IDatabaseConnection connection) throws DatabaseUnitException
    {
        logger.debug("execute(connection={}) - start", connection);
        if (_operation == null)
        {
            throw new DatabaseUnitException("Operation.execute(): setType(String) must be called before execute()!");
        }

        if (_operation == DatabaseOperation.NONE)
        {
            return;
        }

        try {
            DatabaseOperation operation = (_transaction ? new TransactionOperation(_operation) : _operation);
            // TODO This is not very nice and the design should be reviewed but it works for now (gommma)
            boolean useForwardOnly = _forwardOperation && ! isOrdered();
            IDataSet dataset = getSrcDataSet(getSrc(), getFormat(), useForwardOnly);
            if (_nullToken != null) {
                dataset = new ReplacementDataSet(dataset);
                ((ReplacementDataSet)dataset).addReplacementObject(_nullToken, null);
            }
            if(isOrdered()) 
            {
                DatabaseSequenceFilter databaseSequenceFilter = new DatabaseSequenceFilter(connection);
                dataset = new FilteredDataSet(databaseSequenceFilter, dataset);
            }
            operation.execute(connection, dataset);
        }
        catch (SQLException e)
        {
            throw new DatabaseUnitException(e);
        }
    }

    public String getLogMessage()
    {
        return "Executing operation: " + _type
                + "\n          on   file: " + ((_src == null) ? null : _src.getAbsolutePath())
                + "\n          with format: " + _format;
    }


    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append("Operation: ");
        result.append(" type=").append(_type);
        result.append(", format=").append(_format);
        result.append(", src=").append(_src == null ? "null" : _src.getAbsolutePath());
        result.append(", operation=").append(_operation);
        result.append(", nullToken=").append(_nullToken);
        result.append(", ordered=").append(super.isOrdered());
        return result.toString();
    }
}
