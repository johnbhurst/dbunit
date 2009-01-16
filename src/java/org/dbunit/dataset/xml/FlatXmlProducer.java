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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;
import org.dbunit.dataset.OrderedTableNameMap;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.BufferedConsumer;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Manuel Laflamme
 * @author Last changed by: $Author$
 * @version $Revision$ $Date$
 * @since 1.5 (Apr 18, 2003)
 */
public class FlatXmlProducer extends DefaultHandler implements IDataSetProducer, ContentHandler
{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(FlatXmlProducer.class);

    private static final IDataSetConsumer EMPTY_CONSUMER = new DefaultConsumer();
    private static final String DATASET = "dataset";

    private final InputSource _inputSource;
    private final EntityResolver _resolver;
    private boolean _validating = false;
    /**
     * The dataset used to retrieve the metadata for the tables via {@link IDataSet#getTableMetaData(String)}. 
     * Can be null
     */
    private IDataSet _metaDataSet;
    /**
     * The DTD handler which is used to parse a DTD if available. The result of the parsing is stored in
     * {@link #_metaDataSet}.
     */
    private FlatDtdHandler _dtdHandler;
    
    /**
     * The current line number in the current table
     */
    private int _lineNumber = 0;
    /**
     * The current line number
     */
    private int _lineNumberGlobal = 0;
    /**
     * Whether the column sensing feature should be used to dynamically recognize new columns
     * during the parse process.
     */
    private boolean _columnSensing = false;
    private boolean _caseSensitiveTableNames;

    /**
     * The consumer which is responsible for creating the datasets and tables
     */
    private IDataSetConsumer _consumer = EMPTY_CONSUMER;
    /**
     * The ordered table name map which also holds the currently active {@link ITableMetaData}
     */
    private OrderedTableNameMap _orderedTableNameMap;

    
    public FlatXmlProducer(InputSource xmlSource)
    {
        this(xmlSource, true);
    }

    public FlatXmlProducer(InputSource xmlSource, boolean dtdMetadata)
    {
        this(xmlSource, dtdMetadata, false);
    }

    public FlatXmlProducer(InputSource xmlSource, IDataSet metaDataSet)
    {
        _inputSource = xmlSource;
        _metaDataSet = metaDataSet;
        _resolver = this;
        _caseSensitiveTableNames = metaDataSet.isCaseSensitiveTableNames();
        initialize(false);
    }

    public FlatXmlProducer(InputSource xmlSource, EntityResolver resolver)
    {
        _inputSource = xmlSource;
        _resolver = resolver;
        initialize(true);
    }
    
    /**
     * @param xmlSource The input datasource
     * @param dtdMetadata Whether or not DTD metadata is available to parse via a DTD handler
     * @param columnSensing Whether or not the column sensing feature should be used (see FAQ)
     */
    public FlatXmlProducer(InputSource xmlSource, boolean dtdMetadata, boolean columnSensing)
    {
        this(xmlSource, dtdMetadata, columnSensing, false);
    }
    
    /**
     * @param xmlSource The input datasource
     * @param dtdMetadata Whether or not DTD metadata is available to parse via a DTD handler
     * @param columnSensing Whether or not the column sensing feature should be used (see FAQ)
     * @param caseSensitiveTableNames Whether or not this dataset should use case sensitive table names
     * @since 2.4.2
     */
    public FlatXmlProducer(InputSource xmlSource, boolean dtdMetadata, boolean columnSensing, boolean caseSensitiveTableNames)
    {
        _inputSource = xmlSource;
        _columnSensing = columnSensing;
        _caseSensitiveTableNames = caseSensitiveTableNames;
        _resolver = this;
        initialize(dtdMetadata);
    }


    
    private void initialize(boolean dtdMetadata)
    {
        if (dtdMetadata)
        {
            this._dtdHandler = new FlatDtdHandler(this);
        }
    }
    
    private ITableMetaData createTableMetaData(String tableName, Attributes attributes) throws DataSetException
    {
    	if (logger.isDebugEnabled())
    		logger.debug("createTableMetaData(tableName={}, attributes={}) - start", tableName, attributes);

    	// First try to find it in the DTD's dataset
        if (_metaDataSet != null)
        {
            return _metaDataSet.getTableMetaData(tableName);
        }

        // Create metadata from attributes
        Column[] columns = new Column[attributes.getLength()];
        for (int i = 0; i < attributes.getLength(); i++)
        {
            columns[i] = new Column(attributes.getQName(i), DataType.UNKNOWN);
        }

        return new DefaultTableMetaData(tableName, columns);
    }
    
    
    /**
     * merges the existing columns with the potentially new ones.
     * @param columnsToMerge List of extra columns found, which need to be merge back into the metadata.
     * @return ITableMetaData The merged metadata object containing the new columns
     * @throws DataSetException
     */
    private ITableMetaData mergeTableMetaData(List columnsToMerge, ITableMetaData originalMetaData) throws DataSetException
    {
        Column[] columns = new Column[originalMetaData.getColumns().length + columnsToMerge.size()];
        System.arraycopy(originalMetaData.getColumns(), 0, columns, 0, originalMetaData.getColumns().length);
        
        for (int i = 0; i < columnsToMerge.size(); i++)
        {
        	Column column = (Column)columnsToMerge.get(i);
        	columns[columns.length - columnsToMerge.size() + i] = column;
        }
    	
    	return new DefaultTableMetaData(originalMetaData.getTableName(), columns);
    }
    
    /**
     * @return The currently active table metadata or <code>null</code> if no active
     * metadata exists.
     */
    private ITableMetaData getActiveMetaData()
    {
        if(_orderedTableNameMap != null)
        {
            String lastTableName = _orderedTableNameMap.getLastTableName();
            if(lastTableName != null)
            {
                return (ITableMetaData) _orderedTableNameMap.get(lastTableName);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
        
    }
    
    /**
     * @param tableName
     * @return <code>true</code> if the given tableName is a new one
     * which means that it differs from the last active table name.
     */
    private boolean isNewTable(String tableName) 
    {
        return !_orderedTableNameMap.isLastTable(tableName);
    }


    /**
     * parses the attributes in the current row, and checks whether a new column 
     * is found.
     * 
     * <p>Depending on the value of the <code>columnSensing</code> flag, the appropriate
     * action is taken:</p>
     * 
     * <ul>
     *   <li>If it is true, the new column is merged back into the metadata;</li>
     *   <li>If not, a warning message is displayed.</li>
     * </ul>
     * 
     * @param attributes  Attributed for the current row.
     * @throws DataSetException
     */
	protected void handleMissingColumns(Attributes attributes)
			throws DataSetException
	{
		List columnsToMerge = new ArrayList();
		
		ITableMetaData activeMetaData = getActiveMetaData();
		// Search all columns that do not yet exist and collect them
		int attributeLength = attributes.getLength();
		for (int i = 0 ; i < attributeLength; i++)
		{
			try {
			    activeMetaData.getColumnIndex(attributes.getQName(i));
			} 
			catch (NoSuchColumnException e) {
				columnsToMerge.add(new Column(attributes.getQName(i), DataType.UNKNOWN));
			}
		}
		
		if (!columnsToMerge.isEmpty())
		{
			if (_columnSensing)
	    	{
	    		logger.debug("Column sensing enabled. Will create a new metaData with potentially new columns if needed");
	    		activeMetaData = mergeTableMetaData(columnsToMerge, activeMetaData);
	    		_orderedTableNameMap.update(activeMetaData.getTableName(), activeMetaData);
	    		// We also need to recreate the table, copying the data already collected from the old one to the new one
	    		_consumer.startTable(activeMetaData);
	    	} 
	    	else
	    	{
	    		StringBuffer extraColumnNames = new StringBuffer();
	    		for (Iterator i = columnsToMerge.iterator(); i.hasNext();) {
					Column col = (Column) i.next();
					extraColumnNames.append(extraColumnNames.length() > 0 ? "," : "").append(col.getColumnName());
				}
	    	    String msg = "Extra columns (" + extraColumnNames.toString() + ") on line " + (_lineNumber + 1)
						+ " for table " + activeMetaData.getTableName() + " (global line number is "
						+ _lineNumberGlobal + "). Those columns will be ignored.";
	    	    msg += "\n\tPlease add the extra columns to line 1,"
                    + " or use a DTD to make sure the value of those columns are populated" 
                    + " or specify 'columnSensing=true' for your FlatXmlProducer.";
	    	    msg += "\n\tSee FAQ for more details.";
	    		logger.warn(msg);
	    	}
		}
	}
	
	public void setColumnSensing(boolean columnSensing)
	{
		_columnSensing = columnSensing;
	}

    public void setValidating(boolean validating)
    {
        _validating = validating;
    }

    ////////////////////////////////////////////////////////////////////////////
    // IDataSetProducer interface

    public void setConsumer(IDataSetConsumer consumer) throws DataSetException
    {
        logger.debug("setConsumer(consumer) - start");

        if(this._columnSensing) {
            _consumer = new BufferedConsumer(consumer);
        }
        else {
            _consumer = consumer;
        }
    }

    public void produce() throws DataSetException
    {
        logger.debug("produce() - start");

        try
        {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setValidating(_validating);
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();

            if(_dtdHandler != null)
            {
                FlatDtdHandler.setLexicalHandler(xmlReader, _dtdHandler);
                FlatDtdHandler.setDeclHandler(xmlReader, _dtdHandler);
            }

            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(this);
            xmlReader.setEntityResolver(_resolver);
            xmlReader.parse(_inputSource);
        }
        catch (ParserConfigurationException e)
        {
            throw new DataSetException(e);
        }
        catch (SAXException e)
        {
            DataSetException exceptionToRethrow = XmlProducer.buildException(e);
            throw exceptionToRethrow;
        }
        catch (IOException e)
        {
            throw new DataSetException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // EntityResolver interface

    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
    {
        logger.debug("resolveEntity(publicId={}, systemId={}) - start", publicId, systemId);

        // No DTD metadata wanted/available
        if (_dtdHandler == null || !_dtdHandler.isDtdPresent())
        {
            return new InputSource(new StringReader(""));
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // ErrorHandler interface

    public void error(SAXParseException e) throws SAXException
    {
        throw e;

    }

    ////////////////////////////////////////////////////////////////////////
    // ContentHandler interface

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
    {
    	if (logger.isDebugEnabled())
    		logger.debug("startElement(uri={}, localName={}, qName={}, attributes={}) - start",
    				new Object[] { uri, localName, qName, attributes });

        try
        {
            ITableMetaData activeMetaData = getActiveMetaData();
            // Start of dataset
            if (activeMetaData == null && qName.equals(DATASET))
            {
                _consumer.startDataSet();
                _orderedTableNameMap = new OrderedTableNameMap(_caseSensitiveTableNames);
                return;
            }

            // New table
            if (isNewTable(qName))
            {
                // If not first table, notify end of previous table to consumer
                if (activeMetaData != null)
                {
                    _consumer.endTable();
                }

                // In FlatXML the table might have appeared before already, so check for this
                if(_orderedTableNameMap.containsTable(qName))
                {
                    activeMetaData = (ITableMetaData)_orderedTableNameMap.get(qName);
                    _orderedTableNameMap.setLastTable(qName);
                }
                else
                {
                    activeMetaData = createTableMetaData(qName, attributes);
                    _orderedTableNameMap.add(activeMetaData.getTableName(), activeMetaData);
                }
                
                // Notify start of new table to consumer
                _consumer.startTable(activeMetaData);
                _lineNumber = 0;
            }

            // Row notification
            if (attributes.getLength() > 0)
            {
                // If we do not have a DTD
            	if (_dtdHandler == null || !_dtdHandler.isDtdPresent())
            	{
	            	handleMissingColumns(attributes);
	            	// Since a new MetaData object was created assign it to the local variable
	            	activeMetaData = getActiveMetaData();
            	}
            	
            	_lineNumber++;
            	_lineNumberGlobal++;
                Column[] columns = activeMetaData.getColumns();
                Object[] rowValues = new Object[columns.length];
                for (int i = 0; i < columns.length; i++)
                {
                    Column column = columns[i];
                    rowValues[i] = attributes.getValue(column.getColumnName());
                }
                _consumer.row(rowValues);
            }
        }
        catch (DataSetException e)
        {
            throw new SAXException(e);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
    	if (logger.isDebugEnabled())
    		logger.debug("endElement(uri={}, localName={}, qName={}) - start",
    				new Object[]{ uri, localName, qName });

        // End of dataset
        if (qName.equals(DATASET))
        {
            try
            {
                // Notify end of active table to consumer
                if (getActiveMetaData() != null)
                {
                    _consumer.endTable();
                }

                // Notify end of dataset to consumer
                _consumer.endDataSet();
            }
            catch (DataSetException e)
            {
                throw new SAXException(e);
            }
        }
    }

    private static class FlatDtdHandler extends FlatDtdProducer
    {
        /**
         * Logger for this class
         */
        private final Logger logger = LoggerFactory.getLogger(FlatDtdHandler.class);

        private boolean _dtdPresent = false;
        private FlatXmlProducer xmlProducer;

        public FlatDtdHandler(FlatXmlProducer xmlProducer)
        {
            this.xmlProducer = xmlProducer;
        }

        public boolean isDtdPresent() 
        {
            return _dtdPresent;
        }
        
        ////////////////////////////////////////////////////////////////////////////
        // LexicalHandler interface

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException
        {
        	if (logger.isDebugEnabled())
        		logger.debug("startDTD(name={}, publicId={}, systemId={}) - start",
        				new Object[] { name, publicId, systemId });

            _dtdPresent = true;
            try
            {
                // Cache the DTD content to use it as metadata
                FlatDtdDataSet metaDataSet = new FlatDtdDataSet();
                this.setConsumer(metaDataSet);
                // Set the metaData on the xmlProducer
                xmlProducer._metaDataSet = metaDataSet;

                super.startDTD(name, publicId, systemId);
            }
            catch (DataSetException e)
            {
                throw new SAXException(e);
            }
        }
    }
}
