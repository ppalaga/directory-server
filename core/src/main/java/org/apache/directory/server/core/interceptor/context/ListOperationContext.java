/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.core.interceptor.context;

import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.shared.ldap.message.DerefAliasesEnum;

/**
 * A ListContext context used for Interceptors. It contains all the informations
 * needed for the List operation, and used by all the interceptors
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class ListOperationContext  extends AbstractOperationContext
{
    private DerefAliasesEnum aliasDerefMode = DerefAliasesEnum.DEREF_ALWAYS;


    /**
     * Creates a new instance of ListOperationContext.
     */
    public ListOperationContext()
    {
        super();
    }


    /**
     * Creates a new instance of ListOperationContext.
     *
     * @param dn The DN to get the suffix from
     */
    public ListOperationContext( LdapDN dn )
    {
        super( dn );
    }


    /**
     * Creates a new instance of ListOperationContext.
     *
     * @param dn The DN to get the suffix from
     * @param aliasDerefMode the alias dereferencing mode to use
     */
    public ListOperationContext( LdapDN dn, DerefAliasesEnum aliasDerefMode )
    {
        super( dn );
        this.aliasDerefMode = aliasDerefMode;
    }

    
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return "ListOperationContext with DN '" + getDn().getUpName() + "'";
    }


    public DerefAliasesEnum getAliasDerefMode()
    {
        return aliasDerefMode;
    }
}
