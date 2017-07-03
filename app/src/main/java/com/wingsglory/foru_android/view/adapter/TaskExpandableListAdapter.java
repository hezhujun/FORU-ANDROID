package com.wingsglory.foru_android.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.TaskDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by hezhujun on 2017/7/2.
 */

public class TaskExpandableListAdapter extends BaseExpandableListAdapter {

    private String[] group = {"我发布的", "我接受的"};
    private List<List<TaskDTO>> child;

    private Context context;

    public TaskExpandableListAdapter(Context context, List<TaskDTO> taskMyPublished, List<TaskDTO> taskMyAccepted) {
        child = new ArrayList<>();
        child.add(taskMyPublished);
        child.add(taskMyAccepted);
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return group.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return child.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view;
        GroupViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.string_item, null);
            TextView textView = (TextView) view.findViewById(R.id.text_view);
            holder = new GroupViewHolder();
            holder.textView = textView;
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (GroupViewHolder) view.getTag();
        }
        holder.textView.setText(group[groupPosition]);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view;
        ChildViewHolder holder;
        if (convertView == null){
            view = LayoutInflater.from(context).inflate(R.layout.task_list_item, null);
            TextView taskTitle = (TextView) view.findViewById(R.id.task_title);
            TextView taskReward = (TextView) view.findViewById(R.id.task_reward);
            TextView taskState = (TextView) view.findViewById(R.id.task_state);
            TextView taskContent = (TextView) view.findViewById(R.id.task_content);
            holder = new ChildViewHolder();
            holder.taskTitle = taskTitle;
            holder.taskReward = taskReward;
            holder.taskState = taskState;
            holder.taskContent = taskContent;
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ChildViewHolder) view.getTag();
        }

        TaskDTO task = child.get(groupPosition).get(childPosition);
        holder.taskTitle.setText(task.getTaskContent().getTitle());
        holder.taskContent.setText(task.getTaskContent().getContent());
        holder.taskReward.setText("赏" + task.getTaskContent().getReward() + "元");
        holder.taskState.setText(task.getTask().getState());
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class GroupViewHolder{
        private TextView textView;

        public GroupViewHolder() {
        }
    }

    class ChildViewHolder{
        private TextView taskTitle;
        private TextView taskReward;
        private TextView taskState;
        private TextView taskContent;
    }
}
