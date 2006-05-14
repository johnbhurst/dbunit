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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created By:   fede
 * Date:         10-mar-2004
 * Time:         10.52.00
 *
 * Last Checkin: $Author$
 * Date:         $Date$
 * Revision:     $Revision$
 */
public class AllTests extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(CsvParserTest.class));
        suite.addTest(new TestSuite(CsvProducerTest.class));
        suite.addTest(new TestSuite(CsvDataSetWriterTest.class));
        suite.addTest(new TestSuite(CsvDataSetTest.class));
        suite.addTest(new TestSuite(CsvURLDataSetTest.class));
        suite.addTest(new TestSuite(CsvURLProducerTest.class));
        
        return suite;
    }
}
