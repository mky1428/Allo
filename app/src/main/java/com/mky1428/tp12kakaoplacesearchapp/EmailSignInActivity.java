package com.mky1428.tp12kakaoplacesearchapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class EmailSignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_in);

        //툴바에 업버튼
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
    }

    //업버튼 눌렀을떄 액티비티 종료
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public void clickSignIn(View view) {
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        //Firebase Firestore DB에서 이메일과 비밀번호 확인
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("emailUsers").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots){
                    if (email.equals(document.getData().get("email").toString())){
                        if (password.equals(document.getData().get("password").toString())){
                            //로그인 성공
                            String id = document.getId();
                            G.user = new UserAccount(id, email);

                            //Main화면으로 이동
                            Intent intent = new Intent(EmailSignInActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);   //기존남아있던 액티비티들 다 없애기
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     //기존남아있던 액티비티들 다 없애기
                            startActivity(intent);
                            finish();

                            return;
                        }else{
                            break;
                        }
                    }
                }

                //로그인 실패
                new AlertDialog.Builder(EmailSignInActivity.this).setMessage("이메일과 비밀번호를 다시 확인해주시기 바랍니다").show();
                etEmail.requestFocus();
                etEmail.selectAll();
            }
        });
    }
}



























