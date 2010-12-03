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
package org.apache.directory.server.kerberos.shared.messages;


import java.util.Set;

import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.directory.server.kerberos.shared.messages.value.KdcOptions;
import org.apache.directory.shared.kerberos.components.KdcReqBody;
import org.apache.directory.shared.kerberos.KerberosMessageType;
import org.apache.directory.shared.kerberos.KerberosTime;
import org.apache.directory.shared.kerberos.codec.types.EncryptionType;
import org.apache.directory.shared.kerberos.components.EncryptedData;
import org.apache.directory.shared.kerberos.components.HostAddresses;
import org.apache.directory.shared.kerberos.components.PaData;
import org.apache.directory.shared.kerberos.messages.Ticket;


/**
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class KdcRequest extends KerberosMessage
{
    private PaData[] preAuthData; //optional
    private KdcReqBody requestBody;
    private byte[] bodyBytes;


    /**
     * Creates a new instance of KdcRequest.
     *
     * @param pvno
     * @param messageType
     * @param preAuthData
     * @param requestBody
     */
    public KdcRequest( int pvno, KerberosMessageType messageType, PaData[] preAuthData, KdcReqBody requestBody )
    {
        super( pvno, messageType );
        this.preAuthData = preAuthData;
        this.requestBody = requestBody;
    }


    /**
     * Creates a new instance of KdcRequest.
     *
     * @param pvno
     * @param messageType
     * @param preAuthData
     * @param requestBody
     * @param bodyBytes
     */
    public KdcRequest( int pvno, KerberosMessageType messageType, PaData[] preAuthData, KdcReqBody requestBody,
        byte[] bodyBytes )
    {
        this( pvno, messageType, preAuthData, requestBody );
        this.bodyBytes = bodyBytes;
    }


    /**
     * Returns an array of {@link PaData}s.
     *
     * @return The array of {@link PaData}s.
     */
    public PaData[] getPreAuthData()
    {
        return preAuthData;
    }


    /**
     * Returns the request body.
     * 
     * @return The request body.
     */
    public KdcReqBody getRequestBody()
    {
        return requestBody;
    }


    /**
     * Returns the bytes of the body.  This is used for verifying checksums in
     * the Ticket-Granting Service (TGS).
     *
     * @return The bytes of the body.
     */
    public byte[] getBodyBytes()
    {
        return bodyBytes;
    }


    // KdcReqBody delegate methods

    /**
     * Returns additional {@link Ticket}s.
     *
     * @return The {@link Ticket}s.
     */
    public Ticket[] getAdditionalTickets()
    {
        return requestBody.getAdditionalTickets();
    }


    /**
     * Returns the {@link HostAddresses}.
     *
     * @return The {@link HostAddresses}.
     */
    public HostAddresses getAddresses()
    {
        return requestBody.getAddresses();
    }


    /**
     * Returns the client {@link KerberosPrincipal}.
     *
     * @return The client {@link KerberosPrincipal}.
     */
    public KerberosPrincipal getClientPrincipal()
    {
        return requestBody.getClientPrincipal();
    }


    /**
     * Returns the realm of the server principal.
     *
     * @return The realm.
     */
    public String getRealm()
    {
        return requestBody.getServerPrincipal().getRealm();
    }


    /**
     * Returns the {@link EncryptedData}.
     *
     * @return The {@link EncryptedData}.
     */
    public EncryptedData getEncAuthorizationData()
    {
        return requestBody.getEncAuthorizationData();
    }


    /**
     * Returns an array of requested {@link EncryptionType}s.
     *
     * @return The array of {@link EncryptionType}s.
     */
    public Set<EncryptionType> getEType()
    {
        return requestBody.getEType();
    }


    /**
     * Returns the from {@link KerberosTime}.
     *
     * @return The from {@link KerberosTime}.
     */
    public KerberosTime getFrom()
    {
        return requestBody.getFrom();
    }


    /**
     * Returns the {@link KdcOptions}.
     *
     * @return The {@link KdcOptions}.
     */
    public KdcOptions getKdcOptions()
    {
        return requestBody.getKdcOptions();
    }


    /**
     * Returns the nonce.
     *
     * @return The nonce.
     */
    public int getNonce()
    {
        return requestBody.getNonce();
    }


    /**
     * Returns the "R" {@link KerberosTime}.
     *
     * @return The "R" {@link KerberosTime}.
     */
    public KerberosTime getRtime()
    {
        return requestBody.getRtime();
    }


    /**
     * Returns the server {@link KerberosPrincipal}.
     *
     * @return The server {@link KerberosPrincipal}.
     */
    public KerberosPrincipal getServerPrincipal()
    {
        return requestBody.getServerPrincipal();
    }


    /**
     * Returns the till {@link KerberosTime}.
     *
     * @return The till {@link KerberosTime}.
     */
    public KerberosTime getTill()
    {
        return requestBody.getTill();
    }


    // KdcReqBody KdcOptions delegate accesors

    /**
     * Returns the option at the specified index.
     *
     * @param option
     * @return The option.
     */
    public boolean getOption( int option )
    {
        return requestBody.getKdcOptions().get( option );
    }


    /**
     * Sets the option at the specified index.
     *
     * @param option
     */
    public void setOption( int option )
    {
        requestBody.getKdcOptions().set( option );
    }


    /**
     * Clears the option at the specified index.
     *
     * @param option
     */
    public void clearOption( int option )
    {
        requestBody.getKdcOptions().clear( option );
    }
}
