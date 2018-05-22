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

import com.xiaomi.mimobile.milpatest.exceptions.InvalidResponseApduException;

import java.util.Arrays;


public class ResponseApdu {

    private final byte[] rApduBytes;

    public ResponseApdu(final byte[] rApduBytes) throws InvalidResponseApduException {
        if (rApduBytes == null || rApduBytes.length < 2) {
            throw new InvalidResponseApduException(rApduBytes);
        }

        this.rApduBytes = rApduBytes.clone();
    }

    public final int dataLen() {
        return rApduBytes.length - 2;
    }

    public final int statusWord() {
        return ((rApduBytes[dataLen()] & 0xFF) << 8) + (rApduBytes[dataLen() + 1] & 0xFF);
    }

    public final byte[] statusWordBytes() {
        return Arrays.copyOfRange(rApduBytes, dataLen(), rApduBytes.length);
    }

    public final byte[] data() {
        return Arrays.copyOfRange(rApduBytes, 0, dataLen());
    }

    public final byte[] rapdu() {
        return rApduBytes.clone();
    }

    @Override
    public String toString() {
        return TextUtil.bytesToHexString(data()) + "_" + TextUtil.bytesToHexString(statusWordBytes());
    }
}
