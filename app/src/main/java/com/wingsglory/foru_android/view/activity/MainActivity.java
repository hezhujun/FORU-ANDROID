package com.wingsglory.foru_android.view.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.TaskDTO;
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

    private User user;
    private Map<Integer, TaskDTO> taskPublishedBuffer = new HashMap<>();
    private Map<Integer, TaskDTO> taskMyPublishedBuffer = new HashMap<>();
    private Map<Integer, TaskDTO> taskMyAcceptedBuffer = new HashMap<>();

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
                    taskFragment = TaskFragment.newInstance(user, taskPublishedBuffer);
                    fragmentTransaction.add(R.id.content, taskFragment);
                } else {
                    fragmentTransaction.show(taskFragment);
                    taskFragment.show();
                }
                break;
            case R.id.task_detail_fragment:
                if (taskDetailFragment == null) {
                    taskDetailFragment = TaskDetailFragment.newInstance(taskMyPublishedBuffer, taskMyAcceptedBuffer, user);
                    fragmentTransaction.add(R.id.content, taskDetailFragment);
                } else {
                    fragmentTransaction.show(taskDetailFragment);
                }
                break;
            case R.id.help_fragment:
                if (addFragment == null) {
                    addFragment = AddFragment.newInstance(user, taskPublishedBuffer, this);
                    fragmentTransaction.add(R.id.content, addFragment);
                } else {
                    fragmentTransaction.show(addFragment);
                }
                break;
            case R.id.community_fragment:
                if (communityFragment == null) {
                    communityFragment = CommunityFragment.newInstance();
                    fragmentTransaction.add(R.id.content, communityFragment);
                } else {
                    fragmentTransaction.show(communityFragment);
                }
                break;
            case R.id.me_fragment:
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance();
                    fragmentTransaction.add(R.id.content, meFragment);
                } else {
                    fragmentTransaction.show(meFragment);
                }
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

    public void updateTaskPublished() {
        taskFragment.updateMapTask();
    }

    public void updateTaskMyPublished() {

    }
}
