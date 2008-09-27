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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.dbunit.Assertion;
import org.dbunit.dataset.AbstractDataSetTest;
import org.dbunit.dataset.DataSetUtils;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;

/**
 * @author Manuel Laflamme
 * @author Last changed by: $Author$
 * @version $Revision$ $Date$
 * @since Feb 17, 2002
 */
public class XmlDataSetTest extends AbstractDataSetTest
{
    public XmlDataSetTest(String s)
    {
        super(s);
    }

    protected IDataSet createDataSet() throws Exception
    {
        Reader in = new FileReader(
                new File("src/xml/dataSetTest.xml"));
        return new XmlDataSet(in);
    }

    protected IDataSet createDuplicateDataSet() throws Exception
    {
        InputStream in = new FileInputStream(
                new File("src/xml/xmlDataSetDuplicateTest.xml"));
        return new XmlDataSet(in);
    }

    protected IDataSet createMultipleCaseDuplicateDataSet() throws Exception 
    {
        InputStream in = new FileInputStream(
                new File("src/xml/xmlDataSetDuplicateMultipleCaseTest.xml"));
        return new XmlDataSet(in);
    }

    public void testWrite() throws Exception
    {
        IDataSet expectedDataSet = (XmlDataSet)createDataSet();
        File tempFile = File.createTempFile("dataSetTest", ".xml");
        try
        {
            OutputStream out = new FileOutputStream(tempFile);

            try
            {
                // write dataset in temp file
                XmlDataSet.write(expectedDataSet, out);

                // load new dataset from temp file
                IDataSet actualDataSet = new XmlDataSet(new FileReader(tempFile));

                // verify table count
                assertEquals("table count", expectedDataSet.getTableNames().length,
                        actualDataSet.getTableNames().length);

                // verify each table
                ITable[] expected = DataSetUtils.getTables(expectedDataSet);
                ITable[] actual = DataSetUtils.getTables(actualDataSet);
                assertEquals("table count", expected.length, actual.length);
                for (int i = 0; i < expected.length; i++)
                {
                    String expectedName = expected[i].getTableMetaData().getTableName();
                    String actualName = actual[i].getTableMetaData().getTableName();
                    assertEquals("table name", expectedName, actualName);

                    assertTrue("not same instance", expected[i] != actual[i]);
                    Assertion.assertEquals(expected[i], actual[i]);
                }
            }
            finally
            {
                out.close();
            }
        }
        finally
        {
            tempFile.delete();
        }
    }

}




