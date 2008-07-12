/*
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2005, DbUnit.org
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
package org.dbunit.database.search;

import java.io.File;
import java.sql.Connection;
import java.util.HashSet;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

import org.dbunit.HypersonicEnvironment;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.util.search.SearchException;

/**  
 * @author Felipe Leme <dbunit@felipeal.net>
 * @version $Revision$
 * @since Aug 28, 2005
 */

public class TablesDependencyHelperTest extends TestCase {

  
    private Connection jdbcConnection;
  
    private IDatabaseConnection connection;
  
    protected void setUp( String sqlFile ) throws Exception {
    	this.setUp(new String[]{sqlFile});
    }

    protected void setUp( String[] sqlFileList ) throws Exception {
        this.jdbcConnection = HypersonicEnvironment.createJdbcConnection("mem:tempdb");
        for (int i = 0; i < sqlFileList.length; i++) {
        	File sql = new File("src/sql/" + sqlFileList[i]);
            HypersonicEnvironment.executeDdlFile(sql, this.jdbcConnection);
		}
        this.connection = new DatabaseConnection(jdbcConnection);
    }

    protected void tearDown() throws Exception {
        HypersonicEnvironment.shutdown(this.jdbcConnection);
        this.jdbcConnection.close();
//      HypersonicEnvironment.deleteFiles( "tempdb" );
    }   


    public void testGetDependentTablesFromOneTable() throws Exception {    
        setUp( ImportNodesFilterSearchCallbackTest.SQL_FILE );    
        String[][] allInput = ImportNodesFilterSearchCallbackTest.SINGLE_INPUT;
        String[][] allExpectedOutput = ImportNodesFilterSearchCallbackTest.SINGLE_OUTPUT;
        for (int i = 0; i < allInput.length; i++) {
            String[] input = allInput[i];
            String[] expectedOutput = allExpectedOutput[i];
            String[] actualOutput = TablesDependencyHelper.getDependentTables( this.connection, input[0]);
            ArrayAssert.assertEquals( "output didn't match for i=" + i, expectedOutput, actualOutput );
        }           
    }

    public void testGetDependentTablesFromOneTable_RootTableDoesNotExist() throws Exception {    
        setUp( ImportNodesFilterSearchCallbackTest.SQL_FILE );    

        try
        {
            TablesDependencyHelper.getDependentTables( this.connection, "XXXXXX_TABLE_NON_EXISTING");
            fail("Should not be able to get the dependent tables for a non existing input table");
        }
        catch(SearchException expected)
        {
            Throwable cause = expected.getCause();
            assertTrue(cause instanceof NoSuchTableException);
            String expectedMessage = "The table 'XXXXXX_TABLE_NON_EXISTING' does not exist in schema 'null'";
            assertEquals(expectedMessage, cause.getMessage());
        }
    }

    public void testGetDependentTablesFromManyTables() throws Exception {    
        setUp( ImportNodesFilterSearchCallbackTest.SQL_FILE );    
        String[][] allInput = ImportNodesFilterSearchCallbackTest.COMPOUND_INPUT;
        String[][] allExpectedOutput = ImportNodesFilterSearchCallbackTest.COMPOUND_OUTPUT;
        for (int i = 0; i < allInput.length; i++) {
            String[] input = allInput[i];
            String[] expectedOutput = allExpectedOutput[i];
            String[] actualOutput = TablesDependencyHelper.getDependentTables( this.connection, input);
            ArrayAssert.assertEquals( "output didn't match for i=" + i, expectedOutput, actualOutput );
        }           
    }

    public void testGetAllDependentTablesFromOneTable() throws Exception {    
        setUp( ImportAndExportKeysSearchCallbackOwnFileTest.SQL_FILE );    
        String[][] allInput = ImportAndExportKeysSearchCallbackOwnFileTest.SINGLE_INPUT;
        String[][] allExpectedOutput = ImportAndExportKeysSearchCallbackOwnFileTest.SINGLE_OUTPUT;
        for (int i = 0; i < allInput.length; i++) {
            String[] input = allInput[i];
            String[] expectedOutput = allExpectedOutput[i];
            String[] actualOutput = TablesDependencyHelper.getAllDependentTables( this.connection, input[0]);
            ArrayAssert.assertEquals( "output didn't match for i=" + i, expectedOutput, actualOutput );
        }           
    }

    public void testGetAllDependentTablesFromManyTables() throws Exception {    
        setUp( ImportAndExportKeysSearchCallbackOwnFileTest.SQL_FILE );    
        String[][] allInput = ImportAndExportKeysSearchCallbackOwnFileTest.COMPOUND_INPUT;
        String[][] allExpectedOutput = ImportAndExportKeysSearchCallbackOwnFileTest.COMPOUND_OUTPUT;
        for (int i = 0; i < allInput.length; i++) {
            String[] input = allInput[i];
            String[] expectedOutput = allExpectedOutput[i];
            String[] actualOutput = TablesDependencyHelper.getAllDependentTables( this.connection, input);
            ArrayAssert.assertEquals( "output didn't match for i=" + i, expectedOutput, actualOutput );
        }           
    }

    public void testGetAllDatasetFromOneTable() throws Exception {    
    	try {
        setUp( ImportAndExportKeysSearchCallbackOwnFileTest.SQL_FILE );    
        String[][] allInput = ImportAndExportKeysSearchCallbackOwnFileTest.SINGLE_INPUT;
        String[][] allExpectedOutput = ImportAndExportKeysSearchCallbackOwnFileTest.SINGLE_OUTPUT;
        for (int i = 0; i < allInput.length; i++) {
            String[] input = allInput[i];
            String[] expectedOutput = allExpectedOutput[i];
            IDataSet actualOutput = TablesDependencyHelper.getAllDataset( this.connection, input[0], new HashSet());
            String[] actualOutputTables = actualOutput.getTableNames();
            ArrayAssert.assertEquals( "output didn't match for i=" + i, expectedOutput, actualOutputTables );
        }           
        }catch(Exception e){
        	e.printStackTrace();
        	fail("Exception: "+e);
        }
    }

    public void testGetAllDatasetFromOneTable_SeparateSchema() throws Exception {
    	try{
        setUp( new String[] {
        		"hypersonic_switch_schema.sql", 
        		ImportAndExportKeysSearchCallbackOwnFileTest.SQL_FILE
        		} );
        
        String[][] allInput = ImportAndExportKeysSearchCallbackOwnFileTest.SINGLE_INPUT;
        String[][] allExpectedOutput = ImportAndExportKeysSearchCallbackOwnFileTest.SINGLE_OUTPUT;
        for (int i = 0; i < allInput.length; i++) {
            String[] input = allInput[i];
            // Modify the input tables so that they are fully qualified (include the schema name)
            for (int j = 0; j < input.length; j++) {
				input[j] = "TEST_SCHEMA." + input[j];
			}
            String[] expectedOutput = allExpectedOutput[i];
            IDataSet actualOutput = TablesDependencyHelper.getAllDataset( this.connection, input[0], new HashSet());
            String[] actualOutputTables = actualOutput.getTableNames();
            ArrayAssert.assertEquals( "output didn't match for i=" + i, expectedOutput, actualOutputTables );
        }
        
        }catch(Exception e){
        	e.printStackTrace();
        	fail("Exception: "+e);
        }
    }

    
    
// TODO The order gets lost on they way because of the conversion between Map and Array
//  public void testGetDatasetFromManyTables() throws Exception {    
//  setUp( ImportNodesFilterSearchCallbackTest.SQL_FILE );    
//  String[][] allInput = ImportNodesFilterSearchCallbackTest.COMPOUND_INPUT;
//  String[][] allExpectedOutput = ImportNodesFilterSearchCallbackTest.COMPOUND_OUTPUT;
//  for (int i = 0; i < allInput.length; i++) {
//      String[] input = allInput[i];
//      HashMap inputMap = new HashMap();
//      for (int j = 0; j < input.length; j++) {
//          inputMap.put(input[j], new HashSet());
//      }
//      
//      String[] expectedOutput = allExpectedOutput[i];
//      IDataSet actualOutput = TablesDependencyHelper.getDataset( this.connection, inputMap);
//      String[] actualOutputArray = actualOutput.getTableNames();
//      ArrayAssert.assertEquals( "output didn't match for i=" + i, expectedOutput, actualOutputArray );
//  }           
//}


  // ImportAndExportKeysSearchCallbackOwnFileTest
  
  
  

}
