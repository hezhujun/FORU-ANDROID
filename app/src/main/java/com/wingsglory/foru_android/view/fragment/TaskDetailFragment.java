package com.wingsglory.foru_android.view.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.PageBean;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.LogUtil;
import com.wingsglory.foru_android.view.activity.TaskDetailActivity;
import com.wingsglory.foru_android.view.adapter.TaskExpandableListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class TaskDetailFragment extends Fragment
        implements ExpandableListView.OnChildClickListener,
        AbsListView.OnScrollListener,
        SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "TaskDetailFragment";

    private List<Task> myTaskPublishedList = new ArrayList<>();
    private List<Task> myTaskAcceptedList = new ArrayList<>();
    private User user;
    private App app;
    private Map<Integer, Task> taskBuffer;
    // 管理获取到的任务数
    PageBean<Task> publishedPageBean;
    PageBean<Task> acceptedPageBean;

    private ExpandableListView taskDetailListView;
    private TaskExpandableListAdapter adapter;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static TaskDetailFragment newInstance() {
        TaskDetailFragment fragment = new TaskDetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
        user = app.getUser();
        taskBuffer = app.getTaskBuffer();
        adapter = new TaskExpandableListAdapter(getActivity(),
                myTaskPublishedList, myTaskAcceptedList);
        publishedPageBean = new PageBean<>();
        publishedPageBean.setRows(10);
        publishedPageBean.setBeans(myTaskPublishedList);
        acceptedPageBean = new PageBean<>();
        acceptedPageBean.setRows(10);
        acceptedPageBean.setBeans(myTaskAcceptedList);

        new HistoryTaskListAsyncTask(user.getId(), "/task/history/published",
                publishedPageBean).execute();
        new HistoryTaskListAsyncTask(user.getId(), "/task/history/accepted",
                acceptedPageBean).execute();
    }

    private void initView() {
        taskDetailListView =
                (ExpandableListView) view.findViewById(R.id.task_detail_list_view);
        taskDetailListView.setOnChildClickListener(this);
        taskDetailListView.setAdapter(adapter);
        taskDetailListView.setOnScrollListener(this);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_detail, container, false);
        initView();
        return view;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        Task task = null;
        if (groupPosition == 0) {
            task = myTaskPublishedList.get((int) id);
        } else if (groupPosition == 1) {
            task = myTaskAcceptedList.get((int) id);
        }
        if (task != null) {
            Intent intent = TaskDetailActivity.actionStart(getActivity(), task);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 滑动到底部时加载更多的任务信息
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            LogUtil.d(TAG, "-----------------滑动底部位置：" + view.getLastVisiblePosition());
            int lastPublishedTaskPosition = myTaskPublishedList.size();
            int lastAcceptedTaskPosition = lastPublishedTaskPosition + 1 +
                    myTaskAcceptedList.size();
            if (view.getLastVisiblePosition() == lastPublishedTaskPosition) {
                if (publishedPageBean.getTotalRows() != myTaskPublishedList.size()) {
                    new HistoryTaskListAsyncTask(user.getId(), "/task/history/published",
                            publishedPageBean).execute();
                }
            }
            if (view.getLastVisiblePosition() == lastAcceptedTaskPosition) {
                if (acceptedPageBean.getTotalRows() != myTaskAcceptedList.size()) {
                    new HistoryTaskListAsyncTask(user.getId(), "/task/history/accepted",
                            acceptedPageBean).execute();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

    }

    @Override
    public void onRefresh() {
        show();
    }

    class HistoryTaskListAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        private Integer userId;
        private String url;
        private PageBean<Task> pageManager;

        public HistoryTaskListAsyncTask(Integer userId, String url, PageBean<Task> taskPageBean) {
            this.userId = userId;
            this.url = url;
            this.pageManager = taskPageBean;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(getActivity(), "网络异常，获取我的任务信息失败", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        String tasksStr = jsonObject.getString("tasks");
                        PageBean<Task> taskPageBean = objectMapper.readValue(tasksStr,
                                new TypeReference<PageBean<Task>>() {
                                });
                        pageManager.setTotalRows(taskPageBean.getTotalRows());
                        pageManager.setPage(taskPageBean.getPage());
                        // 第一页清空数据
                        if (taskPageBean.getPage() == 1) {
                            pageManager.getBeans().clear();
                        }
                        if (taskPageBean.size() > 0) {
                            for (Task task :
                                    taskPageBean.getBeans()) {
                                pageManager.getBeans().add(task);
                                taskBuffer.put(task.getId(), task);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getActivity(), result.getErr(), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder builder = new FormBody.Builder()
                    .add("userId", String.valueOf(userId))
                    .add("rows", String.valueOf(pageManager.getRows()));
            if (pageManager.getPage() == 1 && pageManager.size() == 0) {
                builder.add("page", String.valueOf(1));
            } else {
                builder.add("page", String.valueOf(pageManager.getPage() + 1));
            }
            FormBody formBody = builder.build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, url + "返回" + json);
                    JSONObject jsonObject = new JSONObject(json);
                    return jsonObject;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void hide() {

    }

    /**
     * 重新进入时刷新数据
     */
    public void show() {
        publishedPageBean.setPage(0);
        publishedPageBean.setTotalRows(Integer.MAX_VALUE);
        publishedPageBean.setBeans(myTaskPublishedList);
        acceptedPageBean.setPage(0);
        publishedPageBean.setTotalRows(Integer.MAX_VALUE);
        acceptedPageBean.setBeans(myTaskAcceptedList);

        new HistoryTaskListAsyncTask(user.getId(), "/task/history/published",
                publishedPageBean).execute();
        new HistoryTaskListAsyncTask(user.getId(), "/task/history/accepted",
                acceptedPageBean).execute();
    }

}
