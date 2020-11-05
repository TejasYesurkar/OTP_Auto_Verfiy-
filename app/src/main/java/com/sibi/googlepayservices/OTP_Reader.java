package com.sibi.googlepayservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

public class OTP_Reader extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static EditText editText;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setEditText(EditText editText)
    {
        OTP_Reader.editText = editText;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for(SmsMessage sms:messages){
            String message = sms.getMessageBody();
            String otp = message.split(": ")[1];
            Log.d(">>","OTP:"+otp);
            editText.setText(otp);
        }
    }
}
