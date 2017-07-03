package com.wingsglory.foru_android.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.solver.Goal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostra13.universalimageloader.utils.L;
import com.wingsglory.foru_android.Const;
import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.TaskDTO;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;
import com.wingsglory.foru_android.view.adapter.TaskExpandableListAdapter;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class TaskDetailFragment extends Fragment {
    private static final String TAG = "TaskDetailFragment";

    private Set<TaskDTO> myTaskPublishedSet;
    private Set<TaskDTO> myTaskAcceptedSet;
    private List<TaskDTO> myTaskPublishedList;
    private List<TaskDTO> myTaskAcceptedList;
    private User user;

    private ExpandableListView taskDetailListView;
    private TaskExpandableListAdapter adapter;

    public static TaskDetailFragment newInstance(Set<TaskDTO> myTaskPublishedSet, Set<TaskDTO> myTaskAcceptedSet, User user) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.myTaskAcceptedSet = myTaskAcceptedSet;
        fragment.myTaskPublishedSet = myTaskPublishedSet;

        fragment.myTaskPublishedList = new ArrayList<>(myTaskPublishedSet);
        fragment.myTaskAcceptedList = new ArrayList<>(myTaskAcceptedSet);

        fragment.user = user;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new TaskExpandableListAdapter(getActivity(), myTaskPublishedList, myTaskAcceptedList);
        new HistoryTaskListAsyncTask(user.getId(), "/task/history/published", myTaskPublishedList, myTaskPublishedSet).execute();
        new HistoryTaskListAsyncTask(user.getId(), "/task/history/accepted", myTaskAcceptedList, myTaskAcceptedSet).execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail, container, false);
        taskDetailListView = (ExpandableListView) view.findViewById(R.id.task_detail_list_view);
        try {
            taskDetailListView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "页面初始化失败", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    class HistoryTaskListAsyncTask extends AsyncTask<Void, Void, List<TaskDTO>> {
        private Integer userId;
        private String url;
        private List<TaskDTO> saveList;
        private Set<TaskDTO> saveSet;

        public HistoryTaskListAsyncTask(Integer userId, String url, List<TaskDTO> saveList, Set<TaskDTO> saveSet) {
            this.userId = userId;
            this.url = url;
            this.saveList = saveList;
            this.saveSet = saveSet;
        }

        @Override
        protected void onPostExecute(List<TaskDTO> taskDTOs) {
            if (taskDTOs != null && taskDTOs.size() >0) {
                saveSet.addAll(taskDTOs);
                saveList.clear();
                saveList.addAll(saveSet);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected List<TaskDTO> doInBackground(Void... params) {
            try {
                HttpUtil.Param param = new HttpUtil.Param();
                param.put("userId", String.valueOf(userId));
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/x-www-form-urlencoded");
                String json = HttpUtil.post(new URL(Const.BASE_URL + url), header, param);
                Log.d(TAG, "history tasks: " + json);
                JSONObject jsonObject = new JSONObject(json);
                String res = jsonObject.getString("result");
                ObjectMapper mapper = new ObjectMapper();
                Result result = mapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    String taskStr = jsonObject.getString("tasks");
                    List<TaskDTO> taskList = mapper.readValue(taskStr, new TypeReference<List<TaskDTO>>() {
                    });
                    return taskList;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
