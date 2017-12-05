package com.tch.conference.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tch.conference.MainActivity;
import com.tch.conference.R;



public class BookoutFragment extends Fragment implements Session.SessionListener, Publisher.PublisherListener, Subscriber.SubscriberListener, Subscriber.VideoListener {

    private String leftTime = "";
    private long timeStampObj = 0;

    private Handler fetchLeftTimeHandler;
    private Handler showLeftTimeHandler;
    private Runnable fetchLeftTimeRunnable;
    private Runnable showLeftTimeRunnable;
    private HttpURLConnection urlConnection;

    @InjectView(R.id.publisherViewContainer)
    RelativeLayout publisherViewContainer;
    @InjectView(R.id.subscriberViewContainer)
    RelativeLayout subscriberViewContainer;
    @InjectView(R.id.otherViewsContainer)
    GridLayout otherViewsContainer;
    @InjectView(R.id.webView)
    WebView webView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    private LinearLayout.LayoutParams publisherParams;
    private LinearLayout.LayoutParams subscriberParams;

    public static String session_url = "";
    public static String web_url = "";
    private static String LOGTAG = "test";

    MainActivity mainActivity;

    private ProgressDialog mProgressDialog;
    ArrayList<Stream> mStreams;
    ArrayList<Subscriber> mSubscribers;
    Session mSession;
    Publisher mPublisher;

    public static BookoutFragment newInstance() {
        BookoutFragment bookoutFragment = new BookoutFragment();
        return bookoutFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_breakout, container, false);
        ButterKnife.inject(this, view);

        mainActivity = (MainActivity)getActivity();
        mStreams = new ArrayList<Stream>();
        mSubscribers = new ArrayList<Subscriber>();

        subscriberParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        publisherParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (!session_url.equals(""))
            (new GetSessionData(session_url)).execute();

        // show title

        fetchLeftTimeHandler = new Handler();
        fetchLeftTimeRunnable = new Runnable() {
            public void run() {
                (new FetchLeftTime()).execute();
                fetchLeftTimeHandler.postDelayed(this, 5000);
            }
        };
        fetchLeftTimeRunnable.run();

        showLeftTimeHandler = new Handler();
        showLeftTimeRunnable = new Runnable() {
            public void run() {
                showTitle();
                showLeftTimeHandler.postDelayed(this, 1000);
            }
        };
        showLeftTimeRunnable.run();


        webView.getSettings().setJavaScriptEnabled(true);
        String user_agent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/538.44 (KHTML, like Gecko) Version/8.0 Safari/538.44";
        webView.getSettings().setUserAgentString(user_agent);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.loadUrl(web_url);

        mainActivity.tvTitle.setText("0:00");
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    static class ViewHolder {
        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private void showTitle()
    {
        long currentTimeStamp = System.currentTimeMillis();
        int leftTimeValue = 0;
        try {
            leftTimeValue = Integer.parseInt(leftTime);
        } catch (Exception e) {
            e.printStackTrace();
            leftTimeValue = 0;
        }
        if (leftTimeValue < 0)
            leftTimeValue = 0;
        int time_left = leftTimeValue - (int)currentTimeStamp/1000 + (int)timeStampObj/1000;
        if (time_left < 0) time_left = 0;
        if (timeStampObj == 0) time_left = 0;

        int min = time_left / 60;
        int sec = time_left % 60;

        String strTitle = String.format("%d : %02d", min, sec);
        mainActivity.tvTitle.setText(strTitle);
    }



    public class SessionData {
        private String apiKey;
        private String sessionId;
        private String token;

        public String getAPIKey() {
            return apiKey;
        }
        public void setAPIKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getSessionId() {
            return sessionId;
        }
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
    }


    @Override
    public void onDestroyView(){
        if (fetchLeftTimeHandler != null)
            fetchLeftTimeHandler.removeCallbacks(fetchLeftTimeRunnable);
        if (showLeftTimeRunnable != null)
            showLeftTimeHandler.removeCallbacks(showLeftTimeRunnable);

        if (mSession != null)
        {
            mStreams.clear();
            mSubscribers.clear();
            mSession.disconnect();
        }

        super.onDestroyView();
    }


    /**
     * opentok SDK integration.
     */

    private void sessionConnect(){
        if (mSession == null) {
            mSession = new Session(getActivity().getApplicationContext(),
                    OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID);
            mSession.setSessionListener(this);
            mSession.connect(OpenTokConfig.TOKEN);
        }
    }

    private void subscribeToStream(Stream stream)
    {
        Subscriber subscriber = new Subscriber(getActivity().getApplicationContext(), stream);
        subscriber.setVideoListener(this);
        mSession.subscribe(subscriber);
        mSubscribers.add(subscriber);

        //attachSubscriberView(subscriber);
        refreshViews();
    }



    @Override
    public void onConnected(Session session) {
        Log.i("test", "call to onConnected of the SessionListener");
        mPublisher = new Publisher(getActivity().getApplicationContext(), "publisher");
        mPublisher.setPublisherListener(this);
        publisherViewContainer.addView(mPublisher.getView(), publisherParams);
        mSession.publish(mPublisher);
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
            mStreams.add(stream);
            subscribeToStream(stream);
        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOGTAG, "Disconnected from the session.");
        if (mPublisher != null) {
            publisherViewContainer.removeView(mPublisher.getView());
        }

        if (mSubscribers.size() != 0) {
            subscriberViewContainer.removeView(mSubscribers.get(0).getView());
            if (mSubscribers.size() > 1){
                otherViewsContainer.removeAllViewsInLayout();
            }
        }

        mPublisher = null;
        //mSubscriber = null;
        mSubscribers.clear();
        mStreams.clear();
        mSession = null;
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i("test", "call to onStreamDropped of the SessionListener");
        if (mSubscribers.size() != 0) {
            unsubscribeFromStream(stream);
        }
    }

    private void unsubscribeFromStream(Stream stream) {

        mStreams.remove(stream);

        for (int i = 0; i < mSubscribers.size(); i++)
        {
            if (mSubscribers.get(i).getStream().getStreamId().equals(stream.getStreamId()))
            {
                if (i == 0)
                    subscriberViewContainer.removeView(mSubscribers.get(i).getView());
                else
                    otherViewsContainer.removeView(mSubscribers.get(i).getView());
                mSession.unsubscribe(mSubscribers.get(i));
                mSubscribers.remove(i);

            }
        }

        refreshViews();
    }

    @Override
    public void onError(Session session, OpentokError error) {
        Log.i("test", "SessionListener error: " + error.getMessage());
    }

    @Override
    public void onStreamCreated(PublisherKit publisher, Stream stream) {
        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
            mStreams.add(stream);
            subscribeToStream(stream);
        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisher, Stream stream) {
        Log.i("test", "call to onStreamDestroyed of the PublisherListener");
    }

    @Override
    public void onError(PublisherKit publisher, OpentokError error) {
        Log.i("test", "PublisherListener error: " + error.getMessage());
    }

    @Override
    public void onConnected(SubscriberKit subscriber) {

        Log.i("test", "call to onConnected of the SubscriberListener");
    }

    @Override
    public void onDisconnected(SubscriberKit subscriber) {

        Log.i("test", "call to onDisconnected of the SubscriberListener");
    }

    @Override
    public void onError(SubscriberKit subscriber, OpentokError error) {
        Log.i("test", "SubscriberListener error: " + error.getMessage());
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriber) {
        Log.i(LOGTAG, "First frame received");
    }

    private void refreshViews()
    {
        subscriberViewContainer.removeAllViewsInLayout();
        otherViewsContainer.removeAllViewsInLayout();

        if (mSubscribers.size() > 0) {

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            subscriberViewContainer.addView(mSubscribers.get(0).getView(), layoutParams);
            mSubscribers.get(0).setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL);
            for (int i = 1; i < mSubscribers.size(); i++)
            {
                GridLayout.LayoutParams layoutParam = new GridLayout.LayoutParams();
                layoutParam.width = otherViewsContainer.getWidth() / 3;
                layoutParam.height = layoutParam.width * 3 / 4;
                final View subscriberView = mSubscribers.get(i).getView();
                subscriberView.setOnClickListener(new SubscriberClickListner(subscriberView, i){
                    @Override
                    public void onClick(View view){
                        subscriberView.setOnClickListener(null);
                        replaceToFirst(index);
                    }
                });
                otherViewsContainer.addView(subscriberView, layoutParam);
                mSubscribers.get(i).setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                        BaseVideoRenderer.STYLE_VIDEO_FILL);
            }
        }
    }

    private void replaceToFirst(int index)
    {
        Subscriber firstSubscriber = mSubscribers.get(0);
        Subscriber tmpSubscriber = mSubscribers.get(index);
        mSubscribers.set(0, tmpSubscriber);
        mSubscribers.set(index, firstSubscriber);
        refreshViews();
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriber, java.lang.String reason) {
        Log.i("test", "call to onVideoDisabled of the VideoListener");
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriber, java.lang.String reason) {
        Log.i("test", "call to onVideoEnabled of the VideoListener");
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriber) {
        Log.i("test", "call to onVideoDisableWarning of the VideoListener");
    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
        Log.i("test", "call to onVideoDisableWarning of the VideoListener");
    }

    private class GetSessionData extends AsyncTask<Void, Void, Void> {

        String url;
        String resultdata;

        public GetSessionData(String url){
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection c = null;
            try {
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        resultdata = sb.toString();
                }

            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {

            if (mProgressDialog != null)
                mProgressDialog.dismiss();
            SessionData sessionData = new Gson().fromJson(resultdata, SessionData.class);
            OpenTokConfig.API_KEY = sessionData.getAPIKey();
            OpenTokConfig.SESSION_ID = sessionData.getSessionId();
            OpenTokConfig.TOKEN = sessionData.getToken();

            System.out.println(sessionData);

            mSession = new Session(getActivity().getApplicationContext(), OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID);
            mSession.setSessionListener(BookoutFragment.this);
            mSession.connect(OpenTokConfig.TOKEN);

        }
    }


    public static class OpenTokConfig
    {
        public static String API_KEY;
        public static String SESSION_ID;
        public static String TOKEN;
        public static boolean SUBSCRIBE_TO_SELF = false;
    }

    class SubscriberClickListner implements View.OnClickListener {
        public int index = 0;

        public SubscriberClickListner(View view, int index)
        {
            this.index = index;
        }

        @Override
        public void onClick(View view)
        {

        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(100);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            super.onPageStarted(view, url, favicon);
        }
    }

    private class FetchLeftTime extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL("http://streaming.thecanonhouse.com/get_timer");
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
            leftTime = result;
            timeStampObj = System.currentTimeMillis();

        }
    }
}
