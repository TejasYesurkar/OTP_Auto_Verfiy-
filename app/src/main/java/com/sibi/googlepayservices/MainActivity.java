package com.sibi.googlepayservices;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import static com.google.android.gms.auth.api.phone.SmsRetriever.*;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private PaymentsClient paymentsClient;
    private GoogleApiClient apiClient;
    int MY_PERMISSIONS_REQUEST_SEND_SMS=0;
    EditText etMobile;
    String message;
    EditText et_otpNumber;
    final int SMS_CONSENT_REQUEST = 2;  // Set to an unused request code
    MySMSBroadcastReceiver mySMSBroadcastReceiver;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etMobile=findViewById(R.id.etMobile);
          requestSMSPermission();
        et_otpNumber = findViewById(R.id.et_otpNumber);
        new OTP_Reader().setEditText(et_otpNumber);
        message="[#] Your ExampleApp code is: 21444\n" ;

        getCreadenticalApiClient();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        requestHint();

        IntentFilter intentFilter = new IntentFilter(SMS_RETRIEVED_ACTION);
        registerReceiver(mySMSBroadcastReceiver, intentFilter);




    }
    private void requestSMSPermission()
    {
        String permission = Manifest.permission.RECEIVE_SMS;

        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED)
        {
            String[] permission_list = new String[1];
            permission_list[0] = permission;

            ActivityCompat.requestPermissions(this, permission_list,1);
        }
    }
    private void sendSMS(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
                Log.d(">>","Granted");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
                Log.d(">>","Not Granted");
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
      if(requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS){
          if (grantResults.length > 0
                  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
              SmsManager smsManager = SmsManager.getDefault();
              smsManager.sendTextMessage("7776935133", null, message, null, null);
              Toast.makeText(getApplicationContext(), "SMS sent.",
                      Toast.LENGTH_LONG).show();
          } else {
              Toast.makeText(getApplicationContext(),
                      "SMS faild, please try again.", Toast.LENGTH_LONG).show();
              return;
          }
      }



    }

    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                apiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),20, null, 0, 0, 0);

        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void SmSRetriever(){
        String senderPhoneNumber = "7776935133";
        SmsRetrieverClient client = getClient(this);
        Task<Void> task = getClient(this).startSmsUserConsent(senderPhoneNumber /* or null */);


        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(">>","onSuccess");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(">>","onFailure");
            }
        });
    }

    // Obtain the phone number from the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string
                etMobile.setText(credential.getId().substring(3));

                sendSMS();
            }
        }

        if (resultCode == SMS_CONSENT_REQUEST) {
            // Get SMS message content
            String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
            // Extract one-time code from the message and complete verification
            // `sms` contains the entire text of the SMS message, so you will need
            // to parse the string.
            String oneTimeCode = (message); // define this function
            Log.d(">>",oneTimeCode);
            // send one time code to the server
        } else {
            // Consent canceled, handle the error ...
        }

    }
    private void getCreadenticalApiClient() {
        apiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .enableAutoManage(MainActivity.this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(">>","onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(">>","onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(">>","onConnectionFailed");
    }

    public void sendMSG(View view) {
        sendSMS();
    }
}