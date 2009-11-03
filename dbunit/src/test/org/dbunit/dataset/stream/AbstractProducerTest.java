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
package org.dbunit.dataset.stream;

import junit.framework.TestCase;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;

/**
 * @author Manuel Laflamme
 * @since Apr 29, 2003
 * @version $Revision$
 */
public abstract class AbstractProducerTest extends TestCase
{
    private static final String[] TABLE_NAMES = {
        "DUPLICATE_TABLE",
        "SECOND_TABLE",
        "TEST_TABLE",
        "NOT_NULL_TABLE",
        "EMPTY_TABLE",
    };

    public AbstractProducerTest(String s)
    {
        super(s);
    }

    protected String[] getExpectedNames() throws Exception
    {
        return (String[])TABLE_NAMES.clone();
    }

    protected int[] getExpectedRowCount() throws Exception
    {
        return new int[] {1, 2, 3, 1, 0};
    }

    protected String getNotNullTableName() throws Exception
    {
        return "NOT_NULL_TABLE";
    }

    protected Column[] createExpectedColumns(Column.Nullable nullable) throws Exception
    {
        Column[] columns = new Column[4];
        for (int i = 0; i < columns.length; i++)
        {
            columns[i] = new Column("COLUMN" + i, DataType.UNKNOWN, nullable);
        }
        return columns;
    }

    protected Object[] createExpectedRow(int row) throws Exception
    {
        Object[] values = new Object[4];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = "row " + row + " col " + i;
        }
        return values;
    }

    protected abstract IDataSetProducer createProducer() throws Exception;

    public void testProduce() throws Exception
    {
        // Setup consumer
        MockDataSetConsumer consumer = new MockDataSetConsumer();
        consumer.addExpectedStartDataSet();
        String[] expectedNames = getExpectedNames();
        int[] rowCounts = getExpectedRowCount();
        for (int i = 0; i < expectedNames.length; i++)
        {
            String expectedName = expectedNames[i];
            Column.Nullable nullable = expectedName.equals(getNotNullTableName()) ?
                    Column.NO_NULLS : Column.NULLABLE;
            Column[] expectedColumns = createExpectedColumns(nullable);

            consumer.addExpectedStartTable(expectedName, expectedColumns);
            for (int j = 0; j < rowCounts[i]; j++)
            {
                consumer.addExpectedRow(expectedName, createExpectedRow(j));
            }
            consumer.addExpectedEndTable(expectedName);
        }
        consumer.addExpectedEndDataSet();

        // Setup producer
        IDataSetProducer producer = createProducer();
        producer.setConsumer(consumer);

        // Produce and verify consumer
        producer.produce();
        consumer.verify();
    }

    public void testProduceWithoutConsumer() throws Exception
    {
        IDataSetProducer producer = createProducer();
        producer.produce();
    }

}
