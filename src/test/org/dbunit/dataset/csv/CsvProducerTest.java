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

package org.dbunit.dataset.csv;

import junit.framework.TestCase;
import org.dbunit.DatabaseUnitException;
import org.dbunit.ant.Export;
import org.dbunit.ant.Operation;
import org.dbunit.ant.Query;
import org.dbunit.ant.AbstractStep;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;

import java.io.File;
import java.io.FileInputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class CSVProducerTest extends TestCase {
    private String driverClass;
    private String url;
    private String user;
    private String password;
    private IDatabaseConnection connection;
    private static final int ORDERS_ROWS_NUMBER = 4;
    private static final int ORDERS_ROW_ROWS_NUMBER = 3;
    private static final String THE_DIRECTORY = "src/csv/orders";

    public void testProduceFromFolder() throws DataSetException {
        CSVProducer producer = new CSVProducer(THE_DIRECTORY);
        CachedDataSet consumer = new CachedDataSet();
        // producer.setConsumer(new CSVDataSetWriter("src/csv/orders-out"));

        producer.setConsumer(consumer);
        producer.produce();
        final ITable[] tables = consumer.getTables();
        assertEquals("expected 2 tables", 2, tables.length);

        final ITable orders = consumer.getTable("orders");
        assertNotNull("orders table not found", orders);
        assertEquals("wrong number of rows", ORDERS_ROWS_NUMBER, orders.getRowCount());
        assertEquals("wrong number of columns", 2, orders.getTableMetaData().getColumns().length);

        final ITable ordersRow = consumer.getTable("orders_row");
        assertNotNull("orders_row table not found", ordersRow);
        assertEquals("wrong number of rows", ORDERS_ROW_ROWS_NUMBER, ordersRow.getRowCount());
        assertEquals("wrong number of columns", ORDERS_ROW_ROWS_NUMBER, ordersRow.getTableMetaData().getColumns().length);
    }

    public void testProduceAndInsertFromFolder() throws DatabaseUnitException, ClassNotFoundException, SQLException {
        produceAndInsertToDatabase();
        Statement statement = connection.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("select count(*) from orders");
        resultSet.next();
        int count = resultSet.getInt(1);
        assertEquals(ORDERS_ROWS_NUMBER, count);
        resultSet.close();
        statement.close();
    }

    private void produceAndInsertToDatabase() throws DatabaseUnitException, SQLException {
        CSVProducer producer = new CSVProducer(THE_DIRECTORY);
        CachedDataSet consumer = new CachedDataSet();
        producer.setConsumer(consumer);
        producer.produce();
        DatabaseOperation operation = DatabaseOperation.INSERT;
        operation.execute(connection, consumer);
    }

    public void testInsertOperationWithCsvFormat() throws SQLException, DatabaseUnitException {
        Operation operation = new Operation();
        operation.setFormat(AbstractStep.FORMAT_CSV);
        operation.setSrc(new File(THE_DIRECTORY));
        operation.setType("INSERT");
        operation.execute(connection);
        Statement statement = connection.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("select count(*) from orders");
        resultSet.next();
        final int count = resultSet.getInt(1);
        assertEquals("wrong number of row in orders table", ORDERS_ROWS_NUMBER, count);
        resultSet.close();
        statement.close();
    }

    public void testExportTaskWithCsvFormat() throws DatabaseUnitException, SQLException {
        produceAndInsertToDatabase();

        final String fromAnt = "target/csv/from-ant";
        final File dir = new File(fromAnt);
        deleteDirectory(dir);

        Export export = new Export();
        export.setFormat(AbstractStep.FORMAT_CSV);
        export.setDest(dir);

        Query query = new Query();
        query.setName("orders");
        query.setSql("select * from orders");
        export.addQuery(query);

        Query query2 = new Query();
        query2.setName("orders_row");
        query2.setSql("select * from orders_row");
        export.addQuery(query2);

        export.execute(getConnection());

        final File ordersFile = new File(fromAnt + "/orders.csv");
        assertTrue("file '" + ordersFile.getAbsolutePath() + "' does not exists", ordersFile.exists());
        final File ordersRowFile = new File(fromAnt + "/orders_row.csv");
        assertTrue("file " + ordersRowFile + " does not exists", ordersRowFile.exists());
    }

    private void deleteDirectory(final File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            file.delete();
        }
        dir.delete();
    }

    private IDatabaseConnection getConnection() throws SQLException {
        return new DatabaseConnection(DriverManager.getConnection(url, user, password));
    }

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        final FileInputStream inStream = new FileInputStream("src/csv/cvs-tests.properties");
        properties.load(inStream);
        inStream.close();
        driverClass = properties.getProperty("cvs-tests.driver.class");
        url = properties.getProperty("cvs-tests.url");
        user = properties.getProperty("cvs-tests.user");
        password = properties.getProperty("cvs-tests.password");
        assertFalse("".equals(driverClass));
        assertFalse("".equals(url));
        assertFalse("".equals(user));
        Class.forName(driverClass);
        connection = getConnection();
        Statement statement = connection.getConnection().createStatement();
        try {
            statement.execute("DROP TABLE ORDERS");
            statement.execute("DROP TABLE ORDERS_ROW");
        } catch (Exception ignored) {}
        statement.execute("CREATE TABLE ORDERS (ID INTEGER, DESCRIPTION VARCHAR)");
        statement.execute("CREATE TABLE ORDERS_ROW (ID INTEGER, DESCRIPTION VARCHAR, QUANTITY INTEGER)");
        //statement.execute("delete from orders");
        //statement.execute("delete from orders_row");
        statement.close();
    }

    protected void tearDown() throws Exception {
        connection.close();
    }
}
