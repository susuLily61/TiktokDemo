package com.BDhomework.tiktokdemo.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.BDhomework.tiktokdemo.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";

    public static PlaceholderFragment newInstance(String title) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_placeholder, container, false);
        TextView titleView = root.findViewById(R.id.placeholder_title);
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE, "") : "";
        titleView.setText(title + " 页正在建设中");
        return root;
    }
}
