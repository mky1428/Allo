package com.mky1428.tp12kakaoplacesearchapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    int choiceID = R.id.choice_wc;

    //kakao 키워드 로컬 검색 API의 요청 파라미터
    //1. 장소명(검색어)
    String searchQuery = "화장실";

    //2. 내 위치정보객체 (위도,경도 정보 보유한 객체)
    Location mylocation;

    //Fused Location 관리 객체
    FusedLocationProviderClient locationProviderClient;

    //검색결과 데이터를 가진 객체
    public SearchLocalApiResponse searchLocalApiResponse;

    TabLayout tabLayout;

    EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //툴바를 제목줄(ActionBar)로 설정하기
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //SearchListFragment를 우선 화면에 붙이기(처음에 빈화면으로 두지 않기 위해)
        getSupportFragmentManager().beginTransaction().add(R.id.container, new SearchListFragment()).commit();

        //탭레이아웃의 탭버튼을 클릭하는 것을 처리
        tabLayout = findViewById(R.id.layout_tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("LIST")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new SearchListFragment()).commit();
                } else if (tab.getText().equals("MAP")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new SearchMapFragment()).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        etSearch = findViewById(R.id.et_search);
        //소프트키보드의 검색버튼을 눌렀을 때
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //검색어 얻어오기
                searchQuery = etSearch.getText().toString();
                searchPlace();

                return false;
            }
        });


        //우선 내 위치 사용에 대한 허용 동적퍼미션[다이얼로그로 하락을 득하는 퍼미션] 받기
        //FINE 하나만 하면 COARSE는 자동으로 허락 받는것으로 됨
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        int checkResult = checkSelfPermission(permissions[0]);

        if (checkResult == PackageManager.PERMISSION_DENIED) {
            requestPermissions(permissions, 100);   //다이얼로그 띄워줌
        } else {
            requestMyLocation();
        }

    }

    //requestPermissions()의 다이얼로그에서 선택하면 자동으로 실행되는 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //위치정보 제공 허락 시
            //내 위치 얻어오기
            requestMyLocation();
        } else {
            Toast.makeText(this, "내 위치정보를 제공하지 않아 검색기능을 온전히 사용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //내 위치 얻어오는 기능 메소드
    void requestMyLocation() {
        //Google Map에서 사용하고 있는 내 위치검색 API 라이브러리 적용 [Fused Location API : play-services-location 라이브러리 추가]
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //실시간 위치검색 조건 설정
        LocationRequest request = LocationRequest.create();
        request.setInterval(1000);  //위치정보 갱신 간격
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    //위치정보 받았을떄 반응하는 객체
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull  LocationResult locationResult) {
            super.onLocationResult(locationResult);

            //위치결과객체로부터 내 위치 정보 얻기
            mylocation = locationResult.getLastLocation();
//            Toast.makeText(MainActivity.this, ""+mylocation.getLatitude(), Toast.LENGTH_SHORT).show();

            //위치얻어왔으니 더이상 업데이트 하지마
            locationProviderClient.removeLocationUpdates(locationCallback);

            //위치정보를 얻었으니 카카오 키워드 로컬 검색 시작
            searchPlace();
        }
    };

    //카카오 키워드 로컬 검색 API 호출 기능 메소드
    void searchPlace(){
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl("https://dapi.kakao.com");
        builder.addConverterFactory(ScalarsConverterFactory.create());
        builder.addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<SearchLocalApiResponse> call = retrofitService.searchPlace(searchQuery, mylocation.getLongitude()+"", mylocation.getLatitude()+"");

        call.enqueue(new Callback<SearchLocalApiResponse>() {
            @Override
            public void onResponse(Call<SearchLocalApiResponse> call, Response<SearchLocalApiResponse> response) {
                searchLocalApiResponse = response.body();

                //ListFragment가 먼저 보이도록
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SearchListFragment()).commit();

                //TapLayout을 List탭으로 변경
                tabLayout.getTabAt(0).select();

                //확인용
//                PlaceMeta meta = searchLocalApiResponse.meta;
//                List<Place> documents = searchLocalApiResponse.documents;
//
//                new AlertDialog.Builder(MainActivity.this).setMessage(meta.total_count+"\n"+documents.get(0).place_name+"\n"+documents.get(0).distance).show();
            }

            @Override
            public void onFailure(Call<SearchLocalApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "서버 오류\n잠시 뒤 다시 시도해 주세요^^", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //옵션메뉴를 만들어주는 기능 메소드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    public void clickChoice(View view) {
        findViewById(choiceID).setBackgroundResource(R.drawable.bg_choice);
        view.setBackgroundResource(R.drawable.bg_choice_select);
        choiceID = view.getId();

        if (G.user==null){  //로그인안했으면
            if (choiceID!=R.id.choice_wc && choiceID!=R.id.choice_movie){
                new AlertDialog.Builder(this).setMessage("로그인을이 필요한 서비스입니다").show();
                return;
            }
        }

        switch (choiceID){
            case R.id.choice_wc: searchQuery="화장실"; break;
            case R.id.choice_movie: searchQuery="영화관"; break;
            case R.id.choice_gas_station: searchQuery="주유소"; break;
            case R.id.choice_ev_station: searchQuery="전기충전소"; break;
            case R.id.choice_drugstore: searchQuery="약국"; break;
            case R.id.choice_restaurant: searchQuery="맛집"; break;
            case R.id.choice_mart: searchQuery="마트"; break;
            case R.id.choice_convenience_store: searchQuery="편의점"; break;
            case R.id.choice_car_center: searchQuery="카센터"; break;
            case R.id.choice_bookstore: searchQuery="서점"; break;
        }

        //검색작업 요청
        searchPlace();

        //editText의 글씨 제거
        etSearch.setText("");
        etSearch.clearFocus();
    }
}


























