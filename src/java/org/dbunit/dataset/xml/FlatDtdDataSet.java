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

package org.dbunit.dataset.xml;

import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;

import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 * @since Apr 4, 2002
 */
public class FlatDtdDataSet extends AbstractDataSet implements IDataSetConsumer
{
    private static final List EMPTY_LIST = Arrays.asList(new Object[0]);

    private final List _tableNames = new ArrayList();
    private final Map _tableMap = new HashMap();
    private boolean _ready = false;

    public FlatDtdDataSet()
    {
    }

    public FlatDtdDataSet(InputStream in) throws DataSetException, IOException
    {
        this(new FlatDtdProducer(new InputSource(in)));
    }

    public FlatDtdDataSet(Reader reader) throws DataSetException, IOException
    {
        this(new FlatDtdProducer(new InputSource(reader)));
    }

    public FlatDtdDataSet(IDataSetProducer producer) throws DataSetException
    {
        producer.setConsumer(this);
        producer.produce();
    }

    /**
     * Write the specified dataset to the specified output stream as DTD.
     */
    public static void write(IDataSet dataSet, OutputStream out)
            throws IOException, DataSetException
    {
        write(dataSet, new OutputStreamWriter(out));
    }

    /**
     * Write the specified dataset to the specified writer as DTD.
     */
    public static void write(IDataSet dataSet, Writer out)
            throws IOException, DataSetException
    {
        FlatDtdWriter datasetWriter = new FlatDtdWriter(out);
        datasetWriter.write(dataSet);
    }

    ////////////////////////////////////////////////////////////////////////////
    // AbstractDataSet class

    protected ITableIterator createIterator(boolean reversed)
            throws DataSetException
    {
        // Verify producer notifications completed
        if (!_ready)
        {
            throw new IllegalStateException("Not ready!");
        }

        String[] names = (String[])_tableNames.toArray(new String[0]);
        ITable[] tables = new ITable[names.length];
        for (int i = 0; i < names.length; i++)
        {
            String tableName = names[i];
            ITable table = (ITable)_tableMap.get(tableName);
            if (table == null)
            {
                throw new NoSuchTableException(tableName);
            }

            tables[i] = table;
        }

        return new DefaultTableIterator(tables, reversed);
    }

    ////////////////////////////////////////////////////////////////////////////
    // IDataSet interface

    public String[] getTableNames() throws DataSetException
    {
        // Verify producer notifications completed
        if (!_ready)
        {
            throw new IllegalStateException("Not ready!");
        }

        return (String[])_tableNames.toArray(new String[0]);
    }

    public ITableMetaData getTableMetaData(String tableName) throws DataSetException
    {
        // Verify producer notifications completed
        if (!_ready)
        {
            throw new IllegalStateException("Not ready!");
        }

        String upperTableName = tableName.toUpperCase();
        if (_tableMap.containsKey(upperTableName))
        {
            ITable table = (ITable)_tableMap.get(upperTableName);
            return table.getTableMetaData();
        }

        throw new NoSuchTableException(tableName);
    }

    public ITable getTable(String tableName) throws DataSetException
    {
        // Verify producer notifications completed
        if (!_ready)
        {
            throw new IllegalStateException("Not ready!");
        }

        String upperTableName = tableName.toUpperCase();
        if (_tableMap.containsKey(upperTableName))
        {
            return (ITable)_tableMap.get(upperTableName);
        }

        throw new NoSuchTableException(tableName);
    }

    ////////////////////////////////////////////////////////////////////////
    // IDataSetConsumer interface

    public void startDataSet() throws DataSetException
    {
        _ready = false;
    }

    public void endDataSet() throws DataSetException
    {
        _ready = true;
    }

    public void startTable(ITableMetaData metaData) throws DataSetException
    {
        String tableName = metaData.getTableName();
        _tableNames.add(tableName);
        _tableMap.put(tableName.toUpperCase(), new DefaultTable(metaData, EMPTY_LIST));
    }

    public void endTable() throws DataSetException
    {
        // no op
    }

    public void row(Object[] values) throws DataSetException
    {
        // no op
    }
}