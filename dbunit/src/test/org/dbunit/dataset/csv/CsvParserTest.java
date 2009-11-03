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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.dataset.common.handlers.IllegalInputCharacterException;
import org.dbunit.dataset.common.handlers.PipelineException;


public class CsvParserTest extends TestCase {

    CsvParser parser;

/*
    public void testNewParserHasNotNullPipeline() {
        assertNotNull(parser.getPipeline());
    }

    public void testAfterEachParsingThePipelineIsEmpty() throws PipelineException, IllegalInputCharacterException {

        class MockPipeline extends Pipeline {
            boolean setProductCalled = false;

            protected void setProducts(List products) {
                assertEquals(0, products.size());
                super.setProducts(products);
                setProductCalled = true;
            }
        }

        MockPipeline mockPipeline = new MockPipeline();
        parser.setPipeline(mockPipeline);
        parser.parse("");
        assertTrue("the set product method should be called to prepare a new list of products",
                mockPipeline.setProductCalled);
    }
*/

    
    public void testCanParseNonQuotedStrings() throws PipelineException, IllegalInputCharacterException {
        String csv = "Hello, world";
        List parsed = parser.parse(csv);
        assertEquals(2, parsed.size());
        assertEquals(parsed.get(0), "Hello");
        assertEquals(parsed.get(1), "world");
    }

    public void testAFieldCanContainANewLine () throws PipelineException, IllegalInputCharacterException {
        assertEquals("", 3, parser.parse("Hello, World\nIt's today, the day before tomorrow").size());
    }

    public void testDontAcceptIncompleteFields () throws PipelineException, IllegalInputCharacterException {
        String incompleteFields = "AAAAA,\"BB";

        try {
            parser.parse(incompleteFields);
            fail("should have thrown an exception");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    public void testAFileCanContainFieldWithNewLine () throws IOException, CsvParserException {
        final String pathname = "src/csv/with-newlines.csv";
        List list = parser.parse(new File(pathname));
        assertEquals("wrong number of lines parsed from " + pathname, 2, list.size());
        List row = (List) list.get(1);
        assertEquals("AA\nAAA", row.get(0));
        assertEquals("BB\nBBB", row.get(1));
    }

    public void testRaiseACSVParserExceptonWhenParsingAnEmptyFile () throws IOException {
        failParsing(new File("src/csv/empty-file.csv"));
    }

    public void testRaiseACSVParserExceptonWhenParsingFileWithDifferentNumberOfColumns () throws IllegalInputCharacterException, IOException, PipelineException {
        failParsing(new File("src/csv/different-column-numbers-last.csv"));
        failParsing(new File("src/csv/different-column-numbers-first.csv"));
    }

    private void failParsing(File sample) throws IOException {
        try {
            parser.parse(sample);
            fail("should have thrown a CsvParserException");
        } catch (CsvParserException e) {
            assertTrue(true);
        }
    }

    public void testSample() throws Exception {

        File sample = new File("src/csv/sample.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sample)));
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        String line;
        while ((line = lineNumberReader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().length() == 0)
                continue;
            //System.out.println("line: " + line);
            List actual = parser.parse(line);
            assertEquals("wrong tokens on line " + lineNumberReader.getLineNumber() + " " + line,
                    3, actual.size());
        }
    }

    public void testWhitespacePreservedOnQuotedStrings() throws PipelineException, IllegalInputCharacterException {
        String csv = "\" Hello, \",world";
        List parsed = parser.parse(csv);
        assertEquals(2, parsed.size());
        assertEquals(" Hello, ", parsed.get(0));
        assertEquals("world", parsed.get(1));
        csv = " Hello, world";
        parsed = parser.parse(csv);
        assertEquals(2, parsed.size());
        assertEquals("Hello", parsed.get(0));
        assertEquals("world", parsed.get(1));
        csv = "\" Hello, \",\" world \"";;
        parsed = parser.parse(csv);
        assertEquals(2, parsed.size());
        assertEquals(" Hello, ", parsed.get(0));
        assertEquals(" world ", parsed.get(1));
    }

    protected void setUp() throws Exception {
        parser = new CsvParserImpl();
    }

}

