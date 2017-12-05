package com.tch.conference.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.tch.conference.HomeActivity;
import com.tch.conference.R;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class HomeFragment extends Fragment {

    ArrayList<HashMap<String, String>> listPost = new ArrayList<HashMap<String, String>>();

    @InjectView(R.id.leftView)
    View leftView;
    @InjectView(R.id.rightView)
    View rightView;
    @InjectView(R.id.webView)
    WebView mWebView;

    private VideoView mVideoView;
    private boolean bFullScreen = false;
    private String path;

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();
        return homeFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);

        String url = "http://service1.rumbletalk.net/h:F:ldkn/";
        mWebView.getSettings().setJavaScriptEnabled(true);
        String user_agent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/538.44 (KHTML, like Gecko) Version/8.0 Safari/538.44";
        mWebView.getSettings().setUserAgentString(user_agent);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.loadUrl(url);


        if (!LibsChecker.checkVitamioLibs(getActivity()))
            return null;

        mVideoView = (VideoView)view.findViewById(R.id.vitamio_videoView);
        path = "http://wms.shared.streamshow.it/canale8/canale8/playlist.m3u8";

        mVideoView.setVideoPath(HomeActivity.rtmp_url);
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.requestFocus();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });

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

    @OnClick(R.id.fullScreenButton)
    public void onFullScreenButton(View view){
        if (!bFullScreen){
            rightView.setVisibility(View.GONE);
            bFullScreen = true;

        } else {
            rightView.setVisibility(View.VISIBLE);
            bFullScreen = false;
        }

    }

}
