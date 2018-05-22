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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.xiaomi.mimobile.milpatest.exceptions.InvalidResponseApduException;
import com.xiaomi.mimobile.milpatest.exceptions.OpenChannelException;


import java.util.NoSuchElementException;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TelephonyApduService extends ApduService {

    
    private final Context context;

    private TelephonyManager telephonyManager;
    private IccOpenLogicalChannelResponse channel;

    public TelephonyApduService(final Context context, final byte[] aid) {
        super(aid);
        this.context = context;
    }

    @Override
    protected void initSession() {
        Log.i("MiLpaTest","initSession()");
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    protected void openChannel() throws OpenChannelException, NoSuchElementException {
        Log.i("MiLpaTest","openChannel()");
        boolean hasCarrierPrivileges = telephonyManager.hasCarrierPrivileges();
        Toast.makeText(context, hasCarrierPrivileges ? "有权限":"没有权限", Toast.LENGTH_LONG).show();
        channel = telephonyManager.iccOpenLogicalChannel(TextUtil.bytesToHexString(aid));
        if (channel == null) {
            Log.w("MiLpaTest","openChannel: Invalid Channel");
            throw new OpenChannelException("Channel is null");
        }

        if (channel.getChannel() == IccOpenLogicalChannelResponse.INVALID_CHANNEL) {
            Log.w("MiLpaTest","openChannel: Invalid Channel");
            throw new OpenChannelException("Invalid Channel");
        }

        if (channel.getStatus() != IccOpenLogicalChannelResponse.STATUS_NO_ERROR) {
            switch (channel.getStatus()) {
                case IccOpenLogicalChannelResponse.STATUS_NO_SUCH_ELEMENT:
                    Log.w("MiLpaTest","openChannel: Unknown AID " + TextUtil.bytesToHexString(aid));
                    throw new NoSuchElementException(
                            "Attempt to select unknown AID: " + TextUtil.bytesToHexString(aid));
                case IccOpenLogicalChannelResponse.STATUS_MISSING_RESOURCE:
                    Log.w("MiLpaTest","openChannel: No logical channel available");
                    throw new OpenChannelException("No logical channel available");
                default:
                    Log.w("MiLpaTest","openChannel: Unknown open channel error");
                    throw new OpenChannelException("Unknown open channel error");
            }
        }

        if (channel.getSelectResponse() != null) {
            Log.i("MiLpaTest","Select: " + TextUtil.bytesToHexString(aid) + ", response: " +
                    TextUtil.bytesToHexString(channel.getSelectResponse()));
        }

    }

    @Override
    public ResponseApdu sendApdu(final byte[] cApdu) throws InvalidResponseApduException {
        Log.i("MiLpaTest","sendApdu(): " + "cApdu = [" + TextUtil.bytesToHexString(cApdu) + "]");
        final String data = getDataFromCApdu(cApdu);

        final int lengthIn = (cApdu.length > 4) ? cApdu[4] & 0xFF : 0x00;

        // @formatter:off
        String[] dataSw = sendApdu(
                                    cApdu[0] & 0xFF,    // CLA
                                    cApdu[1] & 0xFF,    // INS
                                    cApdu[2] & 0xFF,    // P1
                                    cApdu[3] & 0xFF,    // P2
                                    lengthIn,           // Lc
                                    data);
        // @formatter:on

        Log.i("MiLpaTest","sendApdu() original command returns: {" + dataSw[0] + "}_{" + dataSw[1] + "}");

        // create response buffer
        final StringBuilder dataBuilder = new StringBuilder(dataSw[0]);

        while (dataSw[1].startsWith("61")) {

            // issue get response command
            Log.i("MiLpaTest","sendApdu() issuing get response command");

            //@formatter:off
            dataSw = sendApdu(
                            cApdu[0] & 0xFF,    // CLA
                            0xC0,               // INS
                            0x00,               // P1
                            0x00,               // P2
                            0x00,               // Le
                            null);
            //@formatter:on

            Log.i("MiLpaTest","sendApdu: GET RESPONSE RAPDU: {" + dataSw[0] + "}_{" + dataSw[1] + "}");

            dataBuilder.append(dataSw[0]);
        }

        // append last sw
        dataBuilder.append(dataSw[1]);

        return new ResponseApdu(TextUtil.hexStringToBytes(dataBuilder.toString()));
    }

    @Override
    protected void closeChannel() {
        Log.i("MiLpaTest","closeChannel()");
        if (channel != null) {
            telephonyManager.iccCloseLogicalChannel(channel.getChannel());
        }
    }

    @Override
    protected void closeSession() {
        Log.i("MiLpaTest","closeSession()");
    }

    private String getDataFromCApdu(final byte[] cApdu) {
        if (cApdu.length < 5) {
            return "";
        } else {
            return TextUtil.bytesToHexString(cApdu).substring(10);
        }
    }

    private String[] sendApdu(final int cla, final int ins, final int p1, final int p2, final int p3,
                              final String data) throws InvalidResponseApduException {
        //@formatter:off
        final String rapdu = telephonyManager.iccTransmitApduLogicalChannel(channel.getChannel(),
                                cla,        // CLA
                                ins,        // INS
                                p1,         // P1
                                p2,         // P2
                                p3,         // Lc
                                data);
        //@formatter:on

        if (rapdu == null) {
            Log.w("MiLpaTest","sendApdu: null response");
            throw new InvalidResponseApduException(null);
        }

        final String[] retval = new String[2];
        final int rapduLen = rapdu.length();

        retval[0] = rapdu.substring(0, rapduLen - 4);
        retval[1] = rapdu.substring(rapduLen - 4);

        return retval;
    }

}
