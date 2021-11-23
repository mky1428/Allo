package com.mky1428.tp12kakaoplacesearchapp;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //카카오 SDK 초기화설정
        KakaoSdk.init(this, "af8b94aa68a05030d432b503eb8d2c96");
    }
}
