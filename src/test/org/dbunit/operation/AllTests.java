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

package org.dbunit.operation;

import org.dbunit.DatabaseEnvironment;
import org.dbunit.TestFeature;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Manuel Laflamme
 * @version $Revision$
 * @since Feb 19, 2002
 */
public class AllTests
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new TestSuite(AbstractBatchOperationTest.class));
        suite.addTest(new TestSuite(CloseConnectionOperationTest.class));
        suite.addTest(new TestSuite(CompositeOperationTest.class));
        suite.addTest(new TestSuite(DeleteAllOperationTest.class));
        suite.addTest(new TestSuite(DeleteOperationTest.class));
        suite.addTest(new TestSuite(InsertOperationTest.class));
        suite.addTest(new TestSuite(RefreshOperationTest.class));

        DatabaseEnvironment environment = DatabaseEnvironment.getInstance();
        if (environment.support(TestFeature.TRANSACTION))
        {
            suite.addTest(new TestSuite(TransactionOperationTest.class));
        }

        if (environment.support(TestFeature.TRUNCATE_TABLE))
        {
            suite.addTest(new TestSuite(TruncateTableOperationTest.class));
        }

        suite.addTest(new TestSuite(UpdateOperationTest.class));

        return suite;
    }
}





