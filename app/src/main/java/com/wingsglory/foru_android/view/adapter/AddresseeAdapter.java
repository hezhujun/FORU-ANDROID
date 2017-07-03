package com.wingsglory.foru_android.view.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hezhujun on 2017/7/1.
 */

public class AddresseeAdapter extends ArrayAdapter<Addressee> {

    private int resourceId;
    private View.OnClickListener onClickListener;
    private Set<View> editViewSet = new HashSet<>();
    private Set<View> deleteViewSet = new HashSet<>();

    public AddresseeAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Addressee> objects, View.OnClickListener onClickListener) {
        super(context, resource, objects);
        resourceId = resource;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Addressee addressee = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            TextView addresseeView = (TextView) view.findViewById(R.id.addressee);
            viewHolder.addresseeView = addresseeView;
            TextView phoneView = (TextView) view.findViewById(R.id.phone);
            viewHolder.phoneView = phoneView;
            TextView addressView = (TextView) view.findViewById(R.id.address);
            viewHolder.addressView = addressView;
            View editView = view.findViewById(R.id.address_item_edit);
            viewHolder.editView = editView;
            editViewSet.add(view.findViewById(R.id.address_item_edit_view));
            View deleteView = view.findViewById(R.id.address_item_delete);
            viewHolder.deleteView = deleteView;
            deleteViewSet.add(view.findViewById(R.id.address_item_delete_view));
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.position = position;
        viewHolder.addresseeView.setText(addressee.getName());
        viewHolder.phoneView.setText(addressee.getPhone());
        viewHolder.addressView.setText(addressee.getAddress() + " " + addressee.getAddressDetail());
        viewHolder.editView.setTag(position);
        viewHolder.deleteView.setTag(position);
        if (onClickListener != null) {
            viewHolder.editView.setOnClickListener(onClickListener);
            viewHolder.deleteView.setOnClickListener(onClickListener);
        }
        return view;
    }

    class ViewHolder{
        int position;
        TextView addresseeView;
        TextView phoneView;
        TextView addressView;
        View editView;
        View deleteView;

        public ViewHolder() {
        }

        public ViewHolder(TextView addresseeView, TextView phoneView, TextView addressView, View editView, View deleteView) {
            this.addresseeView = addresseeView;
            this.phoneView = phoneView;
            this.addressView = addressView;
            this.editView = editView;
            this.deleteView = deleteView;
        }
    }

    public void showEditAndDeleteButton(boolean isShow) {
        if (isShow) {
            for (View button :
                    editViewSet) {
                button.setVisibility(View.VISIBLE);
            }
            for (View button :
                    deleteViewSet) {
                button.setVisibility(View.VISIBLE);
            }
        } else {
            for (View button :
                    editViewSet) {
                button.setVisibility(View.GONE);
            }
            for (View button :
                    deleteViewSet) {
                button.setVisibility(View.GONE);
            }
        }
    }

}
