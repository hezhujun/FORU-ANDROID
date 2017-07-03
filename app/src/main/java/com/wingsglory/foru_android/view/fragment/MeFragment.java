package com.wingsglory.foru_android.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.view.activity.AddressActivity;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class MeFragment extends Fragment implements View.OnClickListener {

    private View view;

    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_me, container, false);
        View myAddressView = view.findViewById(R.id.my_address);
        myAddressView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_address:
                Intent intent = new Intent(getActivity(), AddressActivity.class);
                startActivity(intent);
                break;
        }
    }
}
