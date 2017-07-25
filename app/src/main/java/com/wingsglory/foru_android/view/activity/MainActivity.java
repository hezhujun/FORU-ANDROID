package com.wingsglory.foru_android.view.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.view.fragment.AddFragment;
import com.wingsglory.foru_android.view.fragment.CommunityFragment;
import com.wingsglory.foru_android.view.fragment.MeFragment;
import com.wingsglory.foru_android.view.fragment.TaskDetailFragment;
import com.wingsglory.foru_android.view.fragment.TaskFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    private App app;

    private FragmentManager fragmentManager;
    private TaskFragment taskFragment;
    private TaskDetailFragment taskDetailFragment;
    private AddFragment addFragment;
    private CommunityFragment communityFragment;
    private MeFragment meFragment;
    private ActionBar mActionBar;

    private User user;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            showFragment(item.getItemId());
            return true;
        }

    };

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        app = (App) getApplication();
        user  = app.getUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentManager = getFragmentManager();

        showFragment(R.id.task_fragment);
    }

    private void showFragment(int id) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragments(fragmentTransaction);
        switch (id) {
            case R.id.task_fragment:
                if (taskFragment == null) {
                    taskFragment = TaskFragment.newInstance();
                    fragmentTransaction.add(R.id.content, taskFragment);
                } else {
                    fragmentTransaction.show(taskFragment);
                    taskFragment.show();
                }
                mActionBar.setTitle("周边任务");
                break;
            case R.id.task_detail_fragment:
                if (taskDetailFragment == null) {
                    taskDetailFragment = TaskDetailFragment.newInstance();
                    fragmentTransaction.add(R.id.content, taskDetailFragment);
                } else {
                    fragmentTransaction.show(taskDetailFragment);
                    taskDetailFragment.show();
                }
                mActionBar.setTitle("我的任务");
                break;
            case R.id.help_fragment:
                if (addFragment == null) {
                    addFragment = AddFragment.newInstance();
                    fragmentTransaction.add(R.id.content, addFragment);
                } else {
                    fragmentTransaction.show(addFragment);
                }
                mActionBar.setTitle("发布任务");
                break;
            case R.id.community_fragment:
                if (communityFragment == null) {
                    communityFragment = CommunityFragment.newInstance();
                    fragmentTransaction.add(R.id.content, communityFragment);
                } else {
                    fragmentTransaction.show(communityFragment);
                }
                mActionBar.setTitle("交流列表");
                break;
            case R.id.me_fragment:
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance();
                    fragmentTransaction.add(R.id.content, meFragment);
                } else {
                    fragmentTransaction.show(meFragment);
                }
                mActionBar.setTitle("个人主页");
                break;
        }
        fragmentTransaction.commit();
    }

    private void hideFragments(FragmentTransaction transaction){
        if(taskFragment != null){
            transaction.hide(taskFragment);
            taskFragment.hide();
        }
        if(taskDetailFragment != null){
            transaction.hide(taskDetailFragment);
            taskDetailFragment.hide();
        }
        if(addFragment != null){
            transaction.hide(addFragment);
        }
        if(communityFragment != null){
            transaction.hide(communityFragment);
        }
        if(meFragment != null){
            transaction.hide(meFragment);
        }
    }
}
