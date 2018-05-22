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

package com.xiaomi.mimobile.milpatest.exceptions;

/**
 * @author mbaharsyah
 */
public class InitApduSessionException extends Exception {
    public InitApduSessionException(final String message) {
        super(message);
    }
}
