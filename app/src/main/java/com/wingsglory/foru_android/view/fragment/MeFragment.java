package com.wingsglory.foru_android.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.view.activity.AddressActivity;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class MeFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ImageView userImageView;
    private TextView usernameView;
    private View myAddresseeView;
    private View logoutButton;

    private User user;
    private App app;

    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_me, container, false);
        initView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {
        userImageView = (ImageView) view.findViewById(R.id.user_image);
        usernameView = (TextView) view.findViewById(R.id.user_name);
        myAddresseeView = view.findViewById(R.id.my_address);
        myAddresseeView.setOnClickListener(this);
        logoutButton = view.findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);
    }

    private void initData() {
        user = app.getUser();
        if (user.getProtraitUrl() != null && !"".equals(user.getProtraitUrl())) {
            Glide.with(this).load(user.getProtraitUrl()).into(userImageView);
        }
        usernameView.setText(user.getUsername());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_address:
                AddressActivity.actionStart(getActivity());
                break;
            case R.id.logout:
                break;
            default:
                break;
        }
    }
}
