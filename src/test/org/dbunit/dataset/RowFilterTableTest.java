package org.dbunit.dataset;

import java.io.FileReader;

import junit.framework.TestCase;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.IRowValueProvider;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.RowFilterTable;
import org.dbunit.dataset.filter.IRowFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 * @author gommma
 * @since 2.3.0
 */
public class RowFilterTableTest extends TestCase
{
	
    private IDataSet getDataSet() throws Exception
    {
        return new FlatXmlDataSet(new FileReader(
                "src/xml/rowFilterTableTest.xml"));
    }


    public void testRowFilter_HappyPath() throws Exception
    {
    	ITable testTable = getDataSet().getTable("TEST_TABLE");
    	IRowFilter rowFilter = new IRowFilter() {
			public boolean accept(IRowValueProvider rowValueProvider) {
				try {
					String value = (String)rowValueProvider.getColumnValue("COLUMN0");
					// filter out first row
					if(value.equals("row 0 col 0")) {
						return false;
					}
					return true;
				} catch (DataSetException e) {
					throw new RuntimeException("Should not happen in this unit test",e);
				}
			}
		
		};
    	ITable rowFilterTable = new RowFilterTable(testTable, rowFilter);
    	// The first row should be filtered
    	assertEquals(3, rowFilterTable.getRowCount());
    	assertEquals("row 1 col 0", rowFilterTable.getValue(0, "COLUMN0"));
    	assertEquals("row 2 col 0", rowFilterTable.getValue(1, "COLUMN0"));
    	assertEquals("row 3 col 0", rowFilterTable.getValue(2, "COLUMN0"));
    }
}
