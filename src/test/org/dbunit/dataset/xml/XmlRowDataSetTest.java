/*
 * XmlRowDataSetTest.java   Mar 13, 2002
 *
 * The dbUnit database testing framework.
 * Copyright (C) 2002   Manuel Laflamme
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.dataset.*;

/**
 * @author Manuel Laflamme
 * @version 1.0
 */
public class XmlRowDataSetTest extends AbstractDataSetTest
{
    public XmlRowDataSetTest(String s)
    {
        super(s);
    }

    protected IDataSet createDataSet() throws Exception
    {
        InputStream in = new FileInputStream(
                new File("src/xml/xmlRowDataSetTest.xml"));
        return new XmlRowDataSet(in, true);
    }

    public void testWrite() throws Exception
    {
        List tableList = new ArrayList();

        IDataSet dataSet = createDataSet();
        File tempFile = File.createTempFile("xmlRowDataSetTest", "xml");
        OutputStream out = new FileOutputStream(tempFile);
        try
        {
            // write dataset in temp file
            XmlRowDataSet.write(dataSet, out);

            // load new dataset from temp file
            XmlRowDataSet xmlDataSet2 = new XmlRowDataSet(new FileInputStream(tempFile), true);

            // verify table count
            assertEquals("table count", dataSet.getTableNames().length,
                    xmlDataSet2.getTableNames().length);

            // verify each table
            String[] tableNames = dataSet.getTableNames();
            for (int i = 0; i < tableNames.length; i++)
            {
                String name = tableNames[i];
                ITable table1 = dataSet.getTable(name);
                ITable table2 = xmlDataSet2.getTable(name);
                assertTrue("not same instance", table1 != table2);
                DataSetUtils.assertEquals(table1, table2);
            }
        }
        finally
        {
            out.close();
            tempFile.delete();
        }
    }

}
