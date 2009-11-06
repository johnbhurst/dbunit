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
package org.dbunit.dataset.sqlloader;

import java.io.File;

import junit.framework.TestCase;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;

/**
 * @author Stephan Strittmatter (stritti AT users.sourceforge.net)
 * @author Last changed by: $Author$
 * @version $Revision$ $Date$
 * @since 2.4.0
 */
public class SqlLoaderCsvDataSetTest extends TestCase {

    /**
     * Gets the data set.
     * 
     * @return the data set
     * 
     * @throws DataSetException the data set exception
     */
    protected IDataSet getDataSet() throws DataSetException {

        SqlLoaderControlDataSet loadedDataSet =
            new SqlLoaderControlDataSet(new File("src/sqlloader"), new File("src/sqlloader/tables.lst"));

        return loadedDataSet;
    }

    /**
     * Test null columns.
     * 
     * @throws DataSetException the data set exception
     */
    public void testCountryTable() throws DataSetException {

        ITable table = getDataSet().getTable("COUNTRY");

        assertEquals(249, table.getRowCount());
        
        // One sample test value
        Object val = table.getValue(3, "NAME");
        assertEquals("AMERICAN_SAMOA", val);
    }

}
