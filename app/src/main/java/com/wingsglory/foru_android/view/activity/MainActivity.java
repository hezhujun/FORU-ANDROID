package com.wingsglory.foru_android.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.TaskDTO;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.view.fragment.AddFragment;
import com.wingsglory.foru_android.view.fragment.CommunityFragment;
import com.wingsglory.foru_android.view.fragment.MeFragment;
import com.wingsglory.foru_android.view.fragment.TaskDetailFragment;
import com.wingsglory.foru_android.view.fragment.TaskFragment;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private TaskFragment taskFragment;
    private TaskDetailFragment taskDetailFragment;
    private AddFragment addFragment;
    private CommunityFragment communityFragment;
    private MeFragment meFragment;

    private User user;
    private Set<TaskDTO> taskPublished = new HashSet<>();
    private Set<TaskDTO> taskMyPublished = new HashSet<>();
    private Set<TaskDTO> taskMyAccepted = new HashSet<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            showFragment(item.getItemId());
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent  intent = new Intent();
        user  = (User) intent.getSerializableExtra("user");
        user = Globle.user;

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
                    taskFragment = TaskFragment.newInstance(user, taskPublished);
                    fragmentTransaction.add(R.id.content, taskFragment);
                } else {
                    fragmentTransaction.show(taskFragment);
                    taskFragment.show();
                }
                break;
            case R.id.task_detail_fragment:
                if (taskDetailFragment == null) {
                    taskDetailFragment = TaskDetailFragment.newInstance(taskMyPublished, taskMyAccepted, user);
                    fragmentTransaction.add(R.id.content, taskDetailFragment);
                } else {
                    fragmentTransaction.show(taskDetailFragment);
                }
                break;
            case R.id.help_fragment:
                if (addFragment == null) {
                    addFragment = AddFragment.newInstance(user, taskPublished, this);
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
