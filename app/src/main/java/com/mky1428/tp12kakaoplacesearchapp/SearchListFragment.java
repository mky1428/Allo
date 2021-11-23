package com.mky1428.tp12kakaoplacesearchapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class SearchListFragment extends Fragment {

    @Nullable
    @Override
    //뷰를 만들 때
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_list, container, false);
    }

    RecyclerView recyclerView;
    PlaceListRecyclerAdapter placeListRecyclerAdapter;

    @Override
    //뷰가 만들어졌을 때
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {    //view = fragment_search_list.xml의 RelativeLayout
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerview);

        //MainActivity의 Place데이터들을 가져오기 위해 MainActivity를 참조
        MainActivity ma = (MainActivity)getActivity();
        if (ma.searchLocalApiResponse == null) return;  //메인액티비티에서 서버로부터 데이터 받아오기 전이면(없으면) return하라
        placeListRecyclerAdapter = new PlaceListRecyclerAdapter(getActivity(), ma.searchLocalApiResponse.documents);
        recyclerView.setAdapter(placeListRecyclerAdapter);
    }
}
