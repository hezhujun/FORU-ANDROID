package com.wingsglory.foru_android.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.PreferenceUtil;
import com.wingsglory.foru_android.view.activity.AddressActivity;
import com.wingsglory.foru_android.view.activity.LoginActivity;
import com.wingsglory.foru_android.view.activity.MyInfoActivity;

import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class MeFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ImageView userImageView;
    private TextView usernameView;
    private View myInfoView;
    private View myAddresseeView;
    private View productIdeaView;
    private View productUpdateView;
    private View productAboutView;
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
        view = inflater.inflate(R.layout.fragment_me, container, false);
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
        myInfoView = view.findViewById(R.id.my_info);
        myAddresseeView = view.findViewById(R.id.my_address);
        productIdeaView = view.findViewById(R.id.product_idea);
        productUpdateView = view.findViewById(R.id.product_update);
        productAboutView = view.findViewById(R.id.product_about);
        logoutButton = view.findViewById(R.id.logout);
        myInfoView.setOnClickListener(this);
        myAddresseeView.setOnClickListener(this);
        productIdeaView.setOnClickListener(this);
        productUpdateView.setOnClickListener(this);
        productAboutView.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
    }

    private void initData() {
        user = app.getUser();
        if (user.getProtraitUrl() != null && !"".equals(user.getProtraitUrl())) {
            Glide.with(this).load(user.getProtraitUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userImageView);
        }
        usernameView.setText(user.getUsername());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_info:
                MyInfoActivity.actionStart(getActivity());
                break;
            case R.id.my_address:
                AddressActivity.actionStart(getActivity());
                break;
            case R.id.product_idea:
            case R.id.product_update:
            case R.id.product_about:
                noImplement();
                break;
            case R.id.logout:
                LoginActivity.actionStart(getActivity());
                // 清空信息
                PreferenceUtil.save(app, -1, "", "");
                PreferenceUtil.setAutoLogin(app, false);
                JMessageClient.logout();
                getActivity().finish();
                break;
            default:
                break;
        }
    }

    private void noImplement() {
        Toast.makeText(getActivity(), "暂未实现", Toast.LENGTH_SHORT).show();
    }
}
