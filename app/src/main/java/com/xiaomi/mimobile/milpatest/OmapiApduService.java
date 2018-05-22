/**********************************************************************************************************************
 * Copyright © 2016 GEMALTO                                                                                           *
 *                                                                                                                    *
 * This computer program includes confidential and proprietary information of Gemalto and is a trade secret           *
 * of Gemalto. All use, disclosure, and/or reproduction is prohibited unless authorized in writing by Gemalto.        *
 * All Rights Reserved.                                                                                               *
 *                                                                                                                    *
 * The computer program is provided "AS IS" without warranty of any kind. Gemalto makes no warranties to              *
 * any person or entity with respect to the computer program and disclaims all other warranties, expressed            *
 * or implied. Gemalto expressly disclaims any implied warranty of merchantability, fitness for particular            *
 * purpose and any warranty which may arise from course of performance, course of dealing, or usage of                *
 * trade. Further Gemalto does not warrant that the computer program will meet requirements or that                   *
 * operation of the computer program will be uninterrupted or error-free.                                             *
 *                                                                                                                    *
 **********************************************************************************************************************/

package com.xiaomi.mimobile.milpatest;

import android.content.Context;
import android.util.Log;

import com.xiaomi.mimobile.milpatest.exceptions.InitApduSessionException;
import com.xiaomi.mimobile.milpatest.exceptions.InvalidResponseApduException;
import com.xiaomi.mimobile.milpatest.exceptions.OpenChannelException;
import com.xiaomi.mimobile.milpatest.exceptions.SendApduException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class OmapiApduService extends ApduService {

    private static final int CONNECTION_TIMEOUT = 5;

    // use only one instance of service and session
    private static SEService seService;

    private final Context context;

    private Session session;
    private Channel channel;

    public OmapiApduService(final Context context, final byte[] aid) {
        super(aid);
        this.context = context;
    }

    @Override
    protected synchronized void initSession() throws TimeoutException, InitApduSessionException {
        Log.i("MiLpaTest","initSession()");

        if (seService == null || !seService.isConnected()) {
            Log.i("MiLpaTest","initSession: instantiating seService");
            final CountDownLatch latch = new CountDownLatch(1);
            seService = new SEService(context, new SEService.CallBack() {
                @Override
                public void serviceConnected(final SEService seService) {
                    Log.i("MiLpaTest","initSession: serviceConnected");
                    latch.countDown();
                }
            });

            final boolean success;

            try {
                // set maximum waiting time for service binding to 5 seconds
                success = latch.await(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            } catch (final InterruptedException exception) {
                Log.e("MiLpaTest","initSession: thread interrupted");
                throw new TimeoutException("Thread was interrupted");
            }

            if (!success) {
                Log.e("MiLpaTest","initSession: connection time out");
                throw new TimeoutException("connection time out");
            }

            Log.i("MiLpaTest","SEService is connected : " + seService.isConnected());
        }

        try {
            Log.i("MiLpaTest","Number of attached readers : " + seService.getReaders().length);
            for (final Reader reader : seService.getReaders()) {
                Log.i("MiLpaTest","reader : " + reader.getName());
                if (reader.getName().toLowerCase().contains("sim")) {
                    // only target the SIM reader
                    session = reader.openSession();
                }
            }
        } catch (final IllegalStateException exception) {
            Log.e("MiLpaTest","initSession: OMAPI service unavailable");
            throw new InitApduSessionException("OMAPI service unavailable");
        } catch (final IOException exception) {
            Log.e("MiLpaTest","initSession: failed to communicate through the reader");
            throw new InitApduSessionException("failed to communicate through the reader");
        }

    }

    @Override
    protected void openChannel() throws OpenChannelException {
        Log.i("MiLpaTest","openChannel()");
        try {
            Log.i("MiLpaTest","openChannel: selecting: " + TextUtil.bytesToHexString(aid));
            channel = session.openLogicalChannel(aid);
            Log.i("MiLpaTest","openChannel: " + TextUtil.bytesToHexString(aid) + " selected");

            Log.i("MiLpaTest",
                    "openChannel: Channel Select Response:" + TextUtil.bytesToHexString(channel.getSelectResponse()));
            Log.i("MiLpaTest","openChannel: Channel isBasicChannel: " + channel.isBasicChannel());
        } catch (final IOException e) {
            Log.e("MiLpaTest","IOException while open channel ", e);
            final String message = "Failed to communicate through reader";
            Log.e("MiLpaTest","openChannel: " + message);
            throw new OpenChannelException(message);
        }
    }

    @Override
    public ResponseApdu sendApdu(final byte[] apdu) throws SendApduException, InvalidResponseApduException {
        Log.i("MiLpaTest","sendApdu(): apdu: " + TextUtil.bytesToHexString(apdu));
        if (channel.isClosed()) {
            throw new IllegalStateException("Channel is closed");
        }
        try {
            return new ResponseApdu(channel.transmit(apdu));
        } catch (final IOException e) {
            final String message = "Failed to send APDU to eUICC";
            Log.e("MiLpaTest","sendApdu: " + message, e);
            throw new SendApduException(message);
        }
    }

    @Override
    protected void closeChannel() {
        Log.i("MiLpaTest","closeChannel()");
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                Log.w("MiLpaTest","closeChannel: Failed to close channel {}", e);
            }
        }
    }

    @Override
    protected void closeSession() {
        Log.i("MiLpaTest","closeSession()");
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                Log.w("MiLpaTest","closeSession: Failed to close session {}", e);
            }
        }
    }
}
