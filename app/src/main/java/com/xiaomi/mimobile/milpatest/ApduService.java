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

import android.util.Log;

import com.xiaomi.mimobile.milpatest.exceptions.InitApduSessionException;
import com.xiaomi.mimobile.milpatest.exceptions.InvalidResponseApduException;
import com.xiaomi.mimobile.milpatest.exceptions.OpenChannelException;
import com.xiaomi.mimobile.milpatest.exceptions.SendApduException;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;


public abstract class ApduService {

    protected final byte[] aid;

    private boolean running = false;

    protected ApduService(final byte[] aid) {
        Log.i("MiLpaTest","ApduService(): " + "aid = [" + TextUtil.bytesToHexString(aid) + "]");
        this.aid = aid;
    }

    public synchronized void start() throws TimeoutException, InitApduSessionException, OpenChannelException,
            SecurityException, NoSuchElementException {
        Log.i("MiLpaTest","start: ");
        Log.i("MiLpaTest","start: isRunning: " + running);

        Log.i("MiLpaTest","start: initSession");
        initSession();

        if (running) {
            Log.i("MiLpaTest","start: APDU Service is already running");
        } else {
            Log.i("MiLpaTest","start: Open Channel and selecting AID: " + TextUtil.bytesToHexString(aid));
            openChannel();
        }
        running = true;
    }

    public synchronized void stop() {
        Log.i("MiLpaTest","stop: ");
        if (running) {
            new Thread() {
                @Override
                public void run() {
                    synchronized (ApduService.this) {
                        super.run();
                        closeChannel();
                        running = false;
                    }
                }
            }.start();

        }
    }

     public abstract ResponseApdu sendApdu(final byte[] apdu) throws SendApduException, InvalidResponseApduException;

    protected abstract void initSession() throws InitApduSessionException, TimeoutException;

    protected abstract void openChannel() throws OpenChannelException, SecurityException, NoSuchElementException;

    protected abstract void closeChannel();

    protected abstract void closeSession();

}
