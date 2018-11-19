package com.bdltz.calculegor;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentImpostazioni extends Fragment {
    View rootview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater infilater, ViewGroup container, Bundle savedInstanceState)
    {
        rootview = infilater.inflate(R.layout.fragment_impostazioni, container, false);
        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}
