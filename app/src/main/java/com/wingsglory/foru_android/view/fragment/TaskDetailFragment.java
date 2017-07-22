package com.wingsglory.foru_android.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.TaskDTO;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;
import com.wingsglory.foru_android.view.activity.TaskDetailActivity;
import com.wingsglory.foru_android.view.adapter.TaskExpandableListAdapter;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class TaskDetailFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, ExpandableListView.OnChildClickListener {
    private static final String TAG = "TaskDetailFragment";

    private Map<Integer, TaskDTO> myTaskPublishedBuffer;
    private Map<Integer, TaskDTO> myTaskAcceptedBuffer;
    private List<TaskDTO> myTaskPublishedList;
    private List<TaskDTO> myTaskAcceptedList;
    private User user;

    private ExpandableListView taskDetailListView;
    private TaskExpandableListAdapter adapter;

    public static TaskDetailFragment newInstance(Map<Integer, TaskDTO> myTaskPublishedBuffer, Map<Integer, TaskDTO> myTaskAcceptedBuffer, User user) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.myTaskAcceptedBuffer = myTaskAcceptedBuffer;
        fragment.myTaskPublishedBuffer = myTaskPublishedBuffer;

        fragment.myTaskPublishedList = new ArrayList<>();
        fragment.myTaskAcceptedList = new ArrayList<>();

        for (Map.Entry<Integer, TaskDTO> entry :
                myTaskPublishedBuffer.entrySet()) {
            TaskDTO task = entry.getValue();
            fragment.myTaskPublishedList.add(task);
        }

        for (Map.Entry<Integer, TaskDTO> entry :
                myTaskAcceptedBuffer.entrySet()) {
            TaskDTO task = entry.getValue();
            fragment.myTaskAcceptedList.add(task);
        }

        fragment.user = user;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new TaskExpandableListAdapter(getActivity(), myTaskPublishedList, myTaskAcceptedList);
        new HistoryTaskListAsyncTask(user.getId(), "/task/history/published", myTaskPublishedList, myTaskPublishedBuffer).execute();
        new HistoryTaskListAsyncTask(user.getId(), "/task/history/accepted", myTaskAcceptedList, myTaskAcceptedBuffer).execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail, container, false);
        taskDetailListView = (ExpandableListView) view.findViewById(R.id.task_detail_list_view);
        taskDetailListView.setOnItemClickListener(this);
        taskDetailListView.setOnItemSelectedListener(this);
        taskDetailListView.setOnChildClickListener(this);
        try {
            taskDetailListView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "页面初始化失败", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick" + position + " " + id);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected" + position + " " + id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.d(TAG, "onChildClick " + groupPosition + " " + childPosition + " " + id);
        TaskDTO task = null;
        if (groupPosition == 0) {
            task = myTaskPublishedList.get((int) id);
        } else if (groupPosition == 1) {
            task = myTaskAcceptedList.get((int) id);
        }
        if (task != null) {
            Intent intent = TaskDetailActivity.startActivity(getActivity(), task, user);
            startActivity(intent);
        }
        return true;
    }

    class HistoryTaskListAsyncTask extends AsyncTask<Void, Void, List<TaskDTO>> {
        private Integer userId;
        private String url;
        private List<TaskDTO> saveList;
        private Map<Integer, TaskDTO> saveBuffer;

        public HistoryTaskListAsyncTask(Integer userId, String url, List<TaskDTO> saveList, Map<Integer, TaskDTO> saveBuffer) {
            this.userId = userId;
            this.url = url;
            this.saveList = saveList;
            this.saveBuffer = saveBuffer;
        }

        @Override
        protected void onPostExecute(List<TaskDTO> taskDTOs) {
            if (taskDTOs != null && taskDTOs.size() > 0) {
                for (TaskDTO task :
                        taskDTOs) {
                    saveBuffer.put(task.getTask().getId(), task);
                }

                for (Map.Entry<Integer, TaskDTO> entry :
                        saveBuffer.entrySet()) {
                    TaskDTO task = entry.getValue();
                    saveList.add(task);
                }

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
                String json = HttpUtil.post(new URL(App.BASE_URL + url), header, param);
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
