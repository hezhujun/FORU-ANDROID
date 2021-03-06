package com.wingsglory.foru_android.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.view.fragment.AddFragment;
import com.wingsglory.foru_android.view.fragment.MeFragment;
import com.wingsglory.foru_android.view.fragment.TaskDetailFragment;
import com.wingsglory.foru_android.view.fragment.TaskFragment;

import jiguang.chat.activity.fragment.ConversationListFragment;

public class MainActivity extends BaseActivity {

    private FragmentManager fragmentManager;
    private TaskFragment taskFragment;
    private TaskDetailFragment taskDetailFragment;
    private AddFragment addFragment;
    private ConversationListFragment conversationListFragment;
    private MeFragment meFragment;
    private ActionBar mActionBar;
    private Fragment currentFragment;

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

        user = app.getUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentManager = getSupportFragmentManager();

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
                currentFragment = taskFragment;
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
                currentFragment = taskDetailFragment;
                break;
            case R.id.help_fragment:
                if (addFragment == null) {
                    addFragment = AddFragment.newInstance();
                    fragmentTransaction.add(R.id.content, addFragment);
                } else {
                    fragmentTransaction.show(addFragment);
                }
                mActionBar.setTitle("发布任务");
                currentFragment = addFragment;
                break;
            case R.id.community_fragment:
                if (conversationListFragment == null) {
                    conversationListFragment = new ConversationListFragment();
                    fragmentTransaction.add(R.id.content, conversationListFragment);
                } else {
                    fragmentTransaction.show(conversationListFragment);
                }
                mActionBar.setTitle("交流列表");
                currentFragment = conversationListFragment;
                break;
            case R.id.me_fragment:
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance();
                    fragmentTransaction.add(R.id.content, meFragment);
                } else {
                    fragmentTransaction.show(meFragment);
                }
                mActionBar.setTitle("个人主页");
                currentFragment = meFragment;
                break;
        }
        fragmentTransaction.commit();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (taskFragment != null) {
            transaction.hide(taskFragment);
            taskFragment.hide();
        }
        if (taskDetailFragment != null) {
            transaction.hide(taskDetailFragment);
            taskDetailFragment.hide();
        }
        if (addFragment != null) {
            transaction.hide(addFragment);
        }
        if (conversationListFragment != null) {
            transaction.hide(conversationListFragment);
        }
        if (meFragment != null) {
            transaction.hide(meFragment);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        if (currentFragment != null) {
            if (currentFragment instanceof TaskFragment
                    && taskFragment != null) {
                taskFragment.show();
            } else if (currentFragment instanceof TaskDetailFragment
                    && taskDetailFragment != null) {
                taskDetailFragment.show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        if (taskFragment != null) {
            taskFragment.hide();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        if (taskFragment != null && taskFragment.getmMapView() != null) {
            taskFragment.getmMapView().onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        if (taskFragment != null && taskFragment.getmMapView() != null) {
            taskFragment.getmMapView().onDestroy();
        }
    }

}
