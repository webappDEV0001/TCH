package com.tch.conference;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tch.conference.R;
import com.tch.conference.fragment.BookoutFragment;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity {

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.list_slidermenu)
    ListView mDrawerList;
    @InjectView(R.id.tvTitle)
    public TextView tvTitle;

    FragmentTransaction fragmentTransaction;
    Fragment fragment;

    ArrayList<HashMap<String, String>> listMenu = new ArrayList<HashMap<String, String>>();

    private ArrayList<String> weburl_list = new ArrayList<String>() {{
        add("https://docs.google.com/document/d/1LTvZ7oIhEZdGN1u-9V4CcbFv4ZxmbtzwkL2WxuViyww/edit");
        add("https://docs.google.com/document/d/1nw9A32EPueZQvutGeJtP2_ewxdPIZj4RtVaO4228Czo/edit");
        add("https://docs.google.com/document/d/18Z-tXQDE0Y1P2zO-2HpnIhx2tHrVWKpn_X5CgOEf1Q0/edit");
        add("https://docs.google.com/document/d/1vMhIthjr8T1WHhzfh9EQP_x7ohBO2n8bVXecUEDqF-4/edit");
        add("https://docs.google.com/document/d/1cJ9Khl4UQf352nTttZlv1jQoMPxsVCoEArpNdxYq_uc/edit");
        add("https://docs.google.com/document/d/1hyk1AALVZ3ZKO35b4LZ8iwspHt5mAT6L7ZJ5RtZZXHM/edit");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        if(b != null)
            HomeActivity.selectedPosition = b.getInt("position");

        int width = getResources().getDisplayMetrics().widthPixels/4;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams)mDrawerList.getLayoutParams();
        params.width = width;
        mDrawerList.setLayoutParams(params);

        setMenuList();
        displayView(HomeActivity.selectedPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setMenuList() {
        listMenu.clear();

        HashMap<String, String> hashMap1 = new HashMap<String, String>();
        hashMap1.put("catid", "");
        hashMap1.put("category", "Home");

        HashMap<String, String> hashMap2 = new HashMap<String, String>();
        hashMap2.put("catid", "");
        hashMap2.put("category", "Breakout1");

        HashMap<String, String> hashMap3 = new HashMap<String, String>();
        hashMap3.put("catid", "");
        hashMap3.put("category", "Breakout2");

        HashMap<String, String> hashMap4 = new HashMap<String, String>();
        hashMap4.put("catid", "");
        hashMap4.put("category", "Breakout3");

        HashMap<String, String> hashMap5 = new HashMap<String, String>();
        hashMap5.put("catid", "");
        hashMap5.put("category", "Breakout4");

        HashMap<String, String> hashMap6 = new HashMap<String, String>();
        hashMap6.put("catid", "");
        hashMap6.put("category", "Breakout5");

        HashMap<String, String> hashMap7 = new HashMap<String, String>();
        hashMap7.put("catid", "");
        hashMap7.put("category", "Breakout6");

        listMenu.add(hashMap1);
        listMenu.add(hashMap2);
        listMenu.add(hashMap3);
        listMenu.add(hashMap4);
        listMenu.add(hashMap5);
        listMenu.add(hashMap6);
        listMenu.add(hashMap7);
    }

    @OnClick(R.id.rlMenu)
    @SuppressWarnings("unused")
    public void Menu(View view) {
        hideKeyboard();
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            mDrawerLayout.openDrawer(mDrawerList);
        }
    }

    private void displayView(int position) {
        // update the main content by replacing fragments

        HomeActivity.selectedPosition = position;
        switch (position) {
            case 0:
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                BookoutFragment.web_url = weburl_list.get(position-1);
                BookoutFragment.session_url = "http://streaming.thecanonhouse.com/session" + String.valueOf(position);
                fragment = BookoutFragment.newInstance();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
            frameLayout.setVisibility(View.VISIBLE);

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);
            mDrawerList.setAdapter(new MenuListAdapter(getApplicationContext(), listMenu, position));
        }
    }

    public class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;
        ArrayList<HashMap<String, String>> locallist;

        int selectedPosition;

        public MenuListAdapter(Context mContext, ArrayList<HashMap<String, String>> locallist, int selectedPosition) {
            this.mContext = mContext;
            this.locallist = locallist;
            this.selectedPosition = selectedPosition;
            inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return locallist.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {

            ViewHolder holder;
            view = inflater.inflate(R.layout.menulist_row, null);
            holder = new ViewHolder(view);
            view.setTag(holder);

            if (position == selectedPosition) {
                holder.rlMain.setBackgroundColor(getResources().getColor(R.color.gray_selected));
            } else {
                holder.rlMain.setBackgroundColor(getResources().getColor(R.color.transparent));
            }

            holder.tvTitle.setText(locallist.get(position).get("category"));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = position;
                    displayView(selectedPosition);
                }
            });

            return view;
        }
    }

    static class ViewHolder {
        @InjectView(R.id.rlMain)
        RelativeLayout rlMain;
        @InjectView(R.id.tv_title)
        TextView tvTitle;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    protected void startActivity(Class klass) {
        startActivity(new Intent(this, klass));
    }

    protected void hideKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            super.onBackPressed();
            finish();
        }
    }
}


