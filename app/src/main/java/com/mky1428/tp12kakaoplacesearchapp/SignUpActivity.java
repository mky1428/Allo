package com.mky1428.tp12kakaoplacesearchapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
    }

    //업버튼 클릭시 액티비티 종료

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home) onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    public void clickSignUp(View view) {
        //Firebase Firestore DB에 이메일사용자 정보 저장하기

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etPasswordConfirm = findViewById(R.id.et_password_confirm);

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String passwordConfirm = etPasswordConfirm.getText().toString();

        if ( !password.equals(passwordConfirm) ){
            new AlertDialog.Builder(this).setMessage("비밀번호를 다시 확인한 후 입력해주시기 바랍니다.").show();
            etPasswordConfirm.requestFocus();
            etPasswordConfirm.selectAll();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //먼저 같은 이메일이 있는지 확인
        db.collection("emailUsers").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //emailUsers컬렉션안에 여러개의 document가 존재하기에 queryDocumentSnapshots은 리스트임
                for (DocumentSnapshot document : queryDocumentSnapshots){
                    if (email.equals(document.getData().get("email"))){
                        new AlertDialog.Builder(SignUpActivity.this).setMessage(("중복된 이메일이 존재합니다. 다시 입력해주시기 바랍니다")).show();
                        etEmail.requestFocus();
                        etEmail.selectAll();
                        return;
                    }
                }

                //저장할 값들(이메일, 비밀번호)을 HashMap(0번방 1번방 이런게 아니라 로즈베리방 블루베리방 이런거) 으로 저장
                Map<String, String> user = new HashMap<>();
                user.put("email", email);
                user.put("password", password);

                db.collection("emailUsers").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        new AlertDialog.Builder(SignUpActivity.this)
                                .setMessage("회원가입이 완료되었습니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                });

            }
        });



    }
}
































