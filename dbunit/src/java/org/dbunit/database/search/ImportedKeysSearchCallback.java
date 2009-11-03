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
 */package org.dbunit.database.search;

import java.util.SortedSet;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.util.search.SearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ISearchCallback implementation that get the nodes using direct foreign key 
 * dependency, i.e, if table A has a FK for a table B, then getNodes(A) will 
 * return B.
 * 
 * @author Felipe Leme (dbunit@felipeal.net)
 * @version $Revision$
 * @since Aug 25, 2005
 */
public class ImportedKeysSearchCallback extends
    AbstractMetaDataBasedSearchCallback {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ImportedKeysSearchCallback.class);

    public ImportedKeysSearchCallback(IDatabaseConnection connection) {
        super(connection);
    }

    public SortedSet getEdges(Object node) throws SearchException {
        logger.debug("getEdges(node={}) - start", node);

        return getNodesFromImportedKeys( node );
    }

}
