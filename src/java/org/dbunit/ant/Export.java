/*
 * Export.java    Jun 10, 2002
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002, Timothy Ruppert && Ben Cox
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
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * The <code>Export</code> class is the step that facilitates exporting
 * the contents of the database and/or it's corresponding dtd to a file.
 * The export can be performed on a full dataset or a partial one if
 * specific table names are identified.
 *
 * @author Timothy Ruppert && Ben Cox
 * @version $Revision$
 * @see DbUnitTaskStep
 */
public class Export implements DbUnitTaskStep
{

    private File _dest;
    private String _format = "flat";
    private List _tables = new ArrayList();
//    private List queries = new ArrayList();

    public Export()
    {
    }

    private String getAbsolutePath(File filename)
    {
        return filename != null ? filename.getAbsolutePath() : "null";
    }

    public File getDest()
    {
        return _dest;
    }

    public String getFormat()
    {
        return _format;
    }

    public List getTables()
    {
        return _tables;
    }

    public void setDest(File dest)
    {
        _dest = dest;
    }

    public void setFormat(String format)
    {
        if (format.equalsIgnoreCase("flat")
                || format.equalsIgnoreCase("xml")
                || format.equalsIgnoreCase("dtd"))
        {
            _format = format;
        }
        else
        {
            throw new IllegalArgumentException("Type must be one of: 'flat'(default), 'xml', or 'dtd' but was: " + format);
        }
    }

    public void addTable(Table table)
    {
        _tables.add(table);
    }

    public void addQuery(Query query)
    {
        _tables.add(query);
    }

    public void execute(IDatabaseConnection connection) throws DatabaseUnitException
    {
        try
        {
            IDataSet dataset = null;

            if (_dest == null)
            {
                throw new DatabaseUnitException("'_dest' is a required attribute of the <export> step.");
            }

            // retrieve the complete database if no tables or queries specified.
            if (_tables.size() == 0)
            {
                dataset = connection.createDataSet();
            }
            else
            {
                List tableList = new ArrayList();
                for (Iterator it = _tables.iterator(); it.hasNext();)
                {
                    Object table = it.next();
                    if (table instanceof Query)
                    {
                        tableList.add(createTable((Query)table, connection));
                    }
                    else
                    {
                        tableList.add(createTable((Table)table, connection));
                    }
                }

                ITable[] tables = (ITable[])tableList.toArray(new ITable[0]);
                dataset = new DefaultDataSet(tables);

            }
            // save the dataset
            Writer out = new FileWriter(_dest);
            try
            {
                if (_format.equalsIgnoreCase("flat"))
                {
                    FlatXmlDataSet.write(dataset, out);
                }
                else if (_format.equalsIgnoreCase("xml"))
                {
                    XmlDataSet.write(dataset, out);
                }
                else if (_format.equalsIgnoreCase("dtd"))
                {
                    FlatDtdDataSet.write(dataset, out);
                }
            }
            finally
            {
                out.close();
            }

        }
        catch (IOException e)
        {
            throw new DatabaseUnitException(e);
        }
        catch (SQLException e)
        {
            throw new DatabaseUnitException(e);
        }
    }

    private ITable createTable(Table table, IDatabaseConnection connection)
           throws DataSetException, SQLException
    {
        return connection.createDataSet().getTable(table.getName());
    }

    private ITable createTable(Query query, IDatabaseConnection connection)
            throws DataSetException, SQLException
    {
        return connection.createQueryTable(query.getName(), query.getSql());
    }

    private String[] getTableArray()
    {
        String[] result = new String[_tables.size()];
        for (int i = 0; i < _tables.size(); i++)
        {
            Table table = (Table)_tables.get(i);
            result[i] = table.getName();
        }
        return result;
    }

    public String getLogMessage()
    {
        return "Executing export: "
                + "\n      in _format: " + _format
                + " to datafile: " + getAbsolutePath(_dest);
    }


    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append("Export: ");
        result.append(" _dest=" + getAbsolutePath(_dest));
        result.append(", _format= " + _format);
        result.append(", _tables= " + _tables);

        return result.toString();
    }
}


