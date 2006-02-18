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

import java.sql.ResultSet;
import java.util.Map;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.PrimaryKeyFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.util.search.IEdge;
import org.dbunit.util.search.SearchException;

/**
 * Extension of the ImportedKeysSearchCallback, where each new edge is added to a 
 * PrimaryKeyFilter.
 * @author Felipe Leme <dbunit@felipeal.net>
 * @version $Revision$
 * @since Sep 9, 2005
 */
public class ImportedKeysSearchCallbackFilteredByPKs extends ImportedKeysSearchCallback {

  // primary key filter associated with the call back
  private final PrimaryKeyFilter pksFilter;
     
  /**
   * Default constructor.
   * @param connection database connection
   * @param allowedPKs map of allowed rows, based on the primary keys (key is the name
   * of a table; value is a Set with allowed primary keys for that table)
   */
  public ImportedKeysSearchCallbackFilteredByPKs(IDatabaseConnection connection, Map allowedPKs) {
    super(connection);
    this.pksFilter = new PrimaryKeyFilter(connection, allowedPKs, false);
  }
  
  /**
   * Get the primary key filter associated with the call back
   * @return primary key filter associated with the call back
   */
  public ITableFilter getFilter() {
    return this.pksFilter;
  }
  
  
  public void nodeAdded(Object node) throws SearchException {
    this.pksFilter.nodeAdded( node );
  }
  
  protected IEdge newEdge(ResultSet rs, int type, String from, String to, String fkColumn, String pkColumn) throws SearchException {
    ForeignKeyRelationshipEdge edge = createFKEdge( rs, type, from, to, fkColumn, pkColumn );
    this.pksFilter.edgeAdded( edge );
    return edge;
  }


}
