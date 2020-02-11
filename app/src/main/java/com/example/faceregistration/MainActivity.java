package com.example.faceregistration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // 생년월일 달력
    Calendar calendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        }
    };

    // 달력에서 선택한 값 텍스트 반영
    private void updateLabel(){
        String dateformat = "yyyy.MM.dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateformat, Locale.KOREA);
        EditText editText = findViewById(R.id.ebirth);
        editText.setText(simpleDateFormat.format(calendar.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 생년월일 입력
        final EditText edbirth = findViewById(R.id.ebirth);
        edbirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, android.R.style.Theme_Holo_Dialog, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // 개인정보 동의 보기 팝업
        Button protection = findViewById(R.id.protection_info);
        protection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PopupActivity.class));
            }
        });

        // 확인 버튼 눌렀을 때
        final Button next = findViewById(R.id.next_info);
        final EditText edname = findViewById(R.id.ename);
        final EditText edemail = findViewById(R.id.eemail);
        final RadioButton male = findViewById(R.id.male);
        final RadioButton female = findViewById(R.id.female);
        final CheckBox agree = findViewById(R.id.agree);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edname.getText().toString();
                String birth = edbirth.getText().toString();
                String email = edemail.getText().toString();
                String gender = "";
                String agreement = "";
                boolean flag = false;

                // 개인정보 입력 확인
                if(name.length()==0){
                    Toast.makeText(MainActivity.this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if(birth.length()==0){
                    Toast.makeText(MainActivity.this, "생년월일을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if(email.length()==0){
                    Toast.makeText(MainActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if(!male.isChecked() && !female.isChecked()){
                    Toast.makeText(MainActivity.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                } else if(!agree.isChecked()){
                    Toast.makeText(MainActivity.this, "동의함을 체크해주세요.", Toast.LENGTH_SHORT).show();
                } else{
                    flag = true;
                }

                // 남자, 여자 체크 값 가져오기
                if(male.isChecked()){
                    gender = "M";
                } else if(female.isChecked()){
                    gender = "F";
                }

                // 동의함 체크 여부
                if(agree.isChecked()){
                    agreement = "Y";
                }

                if(flag){
                    next.setEnabled(true);
                    next.setBackgroundResource(R.color.button);
                    Privacy privacy = new Privacy(name, birth, email, gender, agreement);
                    Intent intentSend = new Intent(getApplicationContext(), Simulation.class);
                    intentSend.putExtra("class", privacy);
                    startActivity(intentSend);
                    finish();
                }


            }
        });
    }
}
