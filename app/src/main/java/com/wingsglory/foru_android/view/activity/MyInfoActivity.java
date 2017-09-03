package com.wingsglory.foru_android.view.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.User;

public class MyInfoActivity extends BaseActivity implements View.OnClickListener {

    private User user;

    private ImageView userImageView;
    private TextView usernameView;
    private TextView userEmailView;
    private TextView userPhoneView;
    private TextView userRealNameView;
    private TextView userIdCardView;

    private View setEmailView;
    private View setUserRealNameView;
    private View setUserIdCardView;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MyInfoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        app = (App) getApplication();
        user = app.getUser();

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("我的信息");
        actionBar.setDisplayHomeAsUpEnabled(true);

        userImageView = (ImageView) findViewById(R.id.user_image);
        usernameView = (TextView) findViewById(R.id.user_name);
        userEmailView = (TextView) findViewById(R.id.user_email);
        userPhoneView = (TextView) findViewById(R.id.user_phone);
        userRealNameView = (TextView) findViewById(R.id.user_real_name);
        userIdCardView = (TextView) findViewById(R.id.user_id_card);

        setEmailView = findViewById(R.id.set_email_view);
        setUserRealNameView = findViewById(R.id.set_real_name_view);
        setUserIdCardView = findViewById(R.id.set_id_card_view);
    }

    private void initData() {
        Glide.with(this).load(user.getProtraitUrl()).into(userImageView);
        usernameView.setText(user.getUsername());
        if (user.getEmail() != null && !"".equals(user.getEmail())) {
            userEmailView.setVisibility(View.VISIBLE);
            userEmailView.setText(user.getEmail());
            setEmailView.setVisibility(View.GONE);
        } else {
            userEmailView.setVisibility(View.GONE);
            setEmailView.setVisibility(View.VISIBLE);
            setEmailView.setOnClickListener(this);
        }
        userPhoneView.setText(user.getPhone());
        if (user.getRealName() != null && !"".equals(user.getRealName())) {
            userRealNameView.setVisibility(View.VISIBLE);
            userRealNameView.setText(user.getRealName());
            setUserRealNameView.setVisibility(View.GONE);
        } else {
            userRealNameView.setVisibility(View.GONE);
            setUserRealNameView.setVisibility(View.VISIBLE);
            setUserRealNameView.setOnClickListener(this);
        }
        if (user.getIdCardNo() != null && !"".equals(user.getIdCardNo())) {
            userIdCardView.setVisibility(View.VISIBLE);
            userIdCardView.setText(user.getIdCardNo());
            setUserIdCardView.setVisibility(View.GONE);
        } else {
            userIdCardView.setVisibility(View.GONE);
            setUserIdCardView.setVisibility(View.VISIBLE);
            setUserIdCardView.setOnClickListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_email_view:
                UpdateEmailActivity.actionStart(this);
                break;
            case R.id.set_real_name_view:
                // 连同身份证一起设置
            case R.id.set_id_card_view:
                UpdateRealInfoActivity.actionStart(this);
                break;
            default:
                break;
        }
    }
}
