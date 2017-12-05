package com.tch.conference;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tch.conference.R;

public class PincodeActivity extends AppCompatActivity {

    private EditText etPinCode;
    private String pinCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pincode);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        (new FetchRtmpURL()).execute();

        etPinCode = (EditText) findViewById(R.id.etPinCode);
        etPinCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_GO) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    if (!pinCode.equals(""))
                    {
                        if (pinCode.equals(etPinCode.getText().toString()))
                        {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Wrong pin code!", Toast.LENGTH_LONG);
                            toast.show();
                            etPinCode.setText("");
                        }
                    }else{
                        (new FetchPinCode()).execute();
                    }

                    handled = true;
                }
                return handled;

            }
        });
    }

    private class FetchPinCode extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        ProgressDialog progressDialog;
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(PincodeActivity.this, null,
                    "Loading...	", true, true);
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://streaming.thecanonhouse.com/idpin");
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.hide();
            pinCode = result;
            String code = etPinCode.getText().toString();
            if (code.equals(pinCode))
            {
                Intent intent = new Intent(PincodeActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Wrong pin code!", Toast.LENGTH_LONG);
                toast.show();
                etPinCode.setText("");
            }

        }
    }

    class FetchRtmpURL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {

            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://streaming.thecanonhouse.com/streamname");
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            HomeActivity.rtmp_url = result;

        }
    }
}
