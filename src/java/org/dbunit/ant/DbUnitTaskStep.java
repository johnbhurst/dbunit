/*
 * DbUnitTaskStep.java    Mar 24, 2002
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002, Timothy Ruppert && Ben Cox
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

package org.dbunit.ant;

import org.dbunit.DatabaseUnitException;

import java.sql.Connection;

/**
 * The <code>DbUnitTaskStep</code> interface allows the execute method
 * to be called on all elements nested under <code>DbUnitTask</code>.
 *
 * @author Timothy Ruppert && Ben Cox
 * @version $Revision$
 */
public interface DbUnitTaskStep
{

    public void execute(Connection conn) throws DatabaseUnitException;

    public String getLogMessage();

}

