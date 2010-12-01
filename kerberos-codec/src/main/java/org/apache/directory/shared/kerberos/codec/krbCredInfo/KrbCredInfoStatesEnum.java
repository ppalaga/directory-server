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
package org.apache.directory.shared.kerberos.codec.krbCredInfo;


import org.apache.directory.shared.asn1.ber.grammar.Grammar;
import org.apache.directory.shared.asn1.ber.grammar.States;


/**
 * This class store the KrbCredInfo grammar's constants. It is also used for debugging
 * purpose
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum KrbCredInfoStatesEnum implements States
{
    // Start
    START_STATE,                           // 0
    
    // ----- KRB-ERROR component --------------------------------------
    KRB_CRED_INFO_SEQ_TAG_STATE,           // 1
    
    KRB_CRED_INFO_KEY_TAG_STATE,           // 2
    
    KRB_CRED_INFO_PREALM_TAG_STATE,        // 3
    KRB_CRED_INFO_PREALM_STATE,            // 4

    KRB_CRED_INFO_PNAME_TAG_STATE,         // 5

    KRB_CRED_INFO_FLAGS_TAG_STATE,         // 6
    KRB_CRED_INFO_FLAGS_STATE,             // 7

    KRB_CRED_INFO_AUTHTIME_TAG_STATE,      // 8
    KRB_CRED_INFO_AUTHTIME_STATE,          // 9

    KRB_CRED_INFO_STARTTIME_TAG_STATE,     // 10
    KRB_CRED_INFO_STARTTIME_STATE,         // 11

    KRB_CRED_INFO_ENDTIME_TAG_STATE,       // 12
    KRB_CRED_INFO_ENDTIME_STATE,           // 13

    KRB_CRED_INFO_RENEWTILL_TAG_STATE,     // 14
    KRB_CRED_INFO_RENEWTILL_STATE,         // 15

    KRB_CRED_INFO_SREALM_TAG_STATE,        // 16
    KRB_CRED_INFO_SREALM_STATE,
    
    KRB_CRED_INFO_SNAME_TAG_STATE,         // 17

    KRB_CRED_INFO_CADDR_TAG_STATE,         // 18

    // End
    LAST_KRB_CRED_INFO_STATE;              // 19

    
    /**
     * Get the grammar name
     * 
     * @param grammar The grammar code
     * @return The grammar name
     */
    public String getGrammarName( int grammar )
    {
        return "KRB_CRED_INFO_GRAMMAR";
    }


    /**
     * Get the grammar name
     * 
     * @param grammar The grammar class
     * @return The grammar name
     */
    public String getGrammarName( Grammar grammar )
    {
        if ( grammar instanceof KrbCredInfoGrammar )
        {
            return "KRB_CRED_INFO_GRAMMAR";
        }
        else
        {
            return "UNKNOWN GRAMMAR";
        }
    }


    /**
     * Get the string representing the state
     * 
     * @param state The state number
     * @return The String representing the state
     */
    public String getState( int state )
    {
        return ( ( state == LAST_KRB_CRED_INFO_STATE.ordinal() ) ? "LAST_KRB_CRED_INFO_STATE" : name() );
    }

    
    /**
     * {@inheritDoc}
     */
    public boolean isEndState()
    {
        return this == LAST_KRB_CRED_INFO_STATE;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public KrbCredInfoStatesEnum getStartState()
    {
        return START_STATE;
    }
}