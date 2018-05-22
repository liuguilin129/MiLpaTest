package com.xiaomi.mimobile.milpatest;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaomi.mimobile.milpatest.exceptions.InitApduSessionException;
import com.xiaomi.mimobile.milpatest.exceptions.InvalidResponseApduException;
import com.xiaomi.mimobile.milpatest.exceptions.OpenChannelException;
import com.xiaomi.mimobile.milpatest.exceptions.SendApduException;

import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {
//    private byte[] aid = {
//            (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x59, (byte) 0x10, (byte) 0x10, (byte) 0xFF,
//            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x89, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00};

    private byte[] aid = null;
    ApduService mOmapiApduService;
    ApduService mTelephonyApduService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!RuntimePermissionsUtil.isPermissionGranted(this, RuntimePermissionsUtil.RUNTIME_PERMISSION_SMARTCARD)) {
            RuntimePermissionsUtil.requestRuntimePermissions(this, RuntimePermissionsUtil.RUNTIME_PERMISSION_LIST_SMARTCARD);
        }
        EditText aidText = (EditText) findViewById(R.id.aid);
        String aidStr = PreferenceManager.getDefaultSharedPreferences(this).getString("aid", "");
        aidText.setText(aidStr);

        EditText inputText = (EditText) findViewById(R.id.input);
        String inputStr = PreferenceManager.getDefaultSharedPreferences(this).getString("input", "");
        inputText.setText(inputStr);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void testIccTransmitApduBasicChannel() {
        Log.i("MiLpaTest", "iccTransmitApduBasicChannel:3F00");
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
////        telephonyManager.createForSubscriptionId();
////        telephonyManager.getSubscriberId();
//        Log.i("MiLpaTest", "telephonyManager subid = " + telephonyManager.getSubscriberId());
//        SubscriptionInfo info1 = SubscriptionManager.from(this).getActiveSubscriptionInfoForSimSlotIndex(0);
        SubscriptionInfo info2 = SubscriptionManager.from(this).getActiveSubscriptionInfoForSimSlotIndex(1);

//
//        Log.i("MiLpaTest", "slot0 subid = " + info1.getSubscriptionId());
//        Log.i("MiLpaTest", "slot1 subid = " + info2.getSubscriptionId());
//
//        TelephonyManager telephonyManager1 = telephonyManager.createForSubscriptionId(info1.getSubscriptionId());
//        TelephonyManager telephonyManager2 = telephonyManager.createForSubscriptionId(info2.getSubscriptionId());


//        String result = telephonyManager.iccTransmitApduBasicChannel(0x00, 0xA4, 0x00, 0x04, 0x02, "3F00");
//        Log.i("MiLpaTest", "result: " + result);
//
//        byte[] resultByte = TextUtil.hexStringToBytes(result);
//        result = telephonyManager.iccTransmitApduBasicChannel(0x00, 0xC0, 0x00, 0x00, resultByte[1], "");
//        Log.i("MiLpaTest", "result: " + result);

        String result = telephonyManager.iccTransmitApduBasicChannel(0xA0, 0xF4, 0x00, 0x04, 0x12, "084906109078563412084906109078563412");
        Log.i("MiLpaTest", "result: " + result);

//        byte[] resultByte = TextUtil.hexStringToBytes(result);
//        result = telephonyManager.iccTransmitApduBasicChannel(0x00, 0xC0, 0x00, 0x00, resultByte[1], "");
//        Log.i("MiLpaTest", "result: " + result);
    }

    private void testIccTransmitApduBasicChannel2() {
        Log.i("MiLpaTest", "iccTransmitApduBasicChannel:2FE2");
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String result = telephonyManager.iccTransmitApduBasicChannel(0x00, 0xA4, 0x00, 0x04, 0x02, "2FE2");
        Log.i("MiLpaTest", "result: " + result);
    }

    public void sendByOma(View view) {
//        testIccTransmitApduBasicChannel();
        aid = getAid();
        if (aid == null) {
            return;
        }
        if (mOmapiApduService == null) {
            mOmapiApduService = new OmapiApduService(this, aid);
        }
        byte[] resp = sendApdu(mOmapiApduService);
        setOutput(TextUtil.bytesToHexString(resp));
    }

    public void sendByTelephony(View view) {
//        testIccTransmitApduBasicChannel2();
        aid = getAid();
        if (aid == null) {
            return;
        }
        if (mTelephonyApduService == null) {
            mTelephonyApduService = new TelephonyApduService(this, aid);
        }
        byte[] resp = sendApdu(mTelephonyApduService);
        setOutput(TextUtil.bytesToHexString(resp));
    }

    private byte[] getAid() {
        EditText editText = (EditText) findViewById(R.id.aid);
        String aid = editText.getText().toString();
        Log.i("MiLpaTest", "aid: " + aid);
        if (TextUtils.isEmpty(aid) || aid.length() % 2 != 0) {
            Toast.makeText(this, "aid位数不对", Toast.LENGTH_LONG).show();
            return null;
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("aid", aid).commit();
        return TextUtil.hexStringToBytes(aid);
    }

    private String getInput() {
        EditText editText = (EditText)findViewById(R.id.input);
        String input = editText.getText().toString();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("input", input).commit();
        return input;
    }

    private void setOutput(String str) {
        EditText editText = (EditText)findViewById(R.id.output);
        editText.setText(str);
        Log.i("MiLpaTest", "output: " + str);
    }

    private byte[] sendApdu(ApduService apduService) {
        String str = getInput();
        if (TextUtils.isEmpty(str) || str.length() % 2 != 0) {
            Toast.makeText(this, "输入参数位数不对", Toast.LENGTH_LONG).show();
            return new byte[]{0x6f, 0x00};
        }
        byte[] apdu = TextUtil.hexStringToBytes(str);
        Log.i("MiLpaTest", "input: " + str);
        try {
            apduService.start();

            ResponseApdu responseApdu = apduService.sendApdu(apdu);

            return responseApdu.data();

        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InitApduSessionException e) {
            e.printStackTrace();
        } catch (OpenChannelException e) {
            e.printStackTrace();
        }  catch (SendApduException e) {
            e.printStackTrace();
        } catch (InvalidResponseApduException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[]{0x6f, 0x00};
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTelephonyApduService != null) {
            mTelephonyApduService.stop();
        }
        if (mOmapiApduService != null) {
            mOmapiApduService.stop();
        }
    }
}
