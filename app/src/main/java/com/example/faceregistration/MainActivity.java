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
    Calendar myCalender = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener myDatePicker = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            myCalender.set(Calendar.YEAR, year);
            myCalender.set(Calendar.MONTH, month);
            myCalender.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        }
    };

    // 달력에서 선택한 값 텍스트 영역에 반영
    private void updateLabel(){
        String myFormat = "yyyy.MM.dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myFormat, Locale.KOREA);

        EditText editText = findViewById(R.id.ebirth);
        editText.setText(simpleDateFormat.format(myCalender.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //생년월일 입력
        final EditText etbirth = findViewById(R.id.ebirth);
        etbirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, android.R.style.Theme_Holo_Dialog, myDatePicker,
                        myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH), myCalender.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // 개인정보 동의 보기 팝업
        Button prt_info = findViewById(R.id.protection_info);
        prt_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PopupActivity.class));
            }
        });

        // 확인 버튼 누르면
        final Button next_info = findViewById(R.id.next_info);
        final EditText etname = findViewById(R.id.ename);
        final EditText etemail = findViewById(R.id.eemail);
        final RadioButton male = findViewById(R.id.male);
        final RadioButton female = findViewById(R.id.female);

        next_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etname.getText().toString();
                String birth = etbirth.getText().toString();
                String email = etemail.getText().toString();
                String gender = "";
                String agreement = "";
                CheckBox agree = findViewById(R.id.agree);
                boolean flag = false;

                //개인정보 입력 확인
                if(name.length()==0){
                    Toast.makeText(MainActivity.this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                    etname.requestFocus();
                    return;
                }
                if(birth.length()==0){
                    Toast.makeText(MainActivity.this, "생년월일을 입력하세요.", Toast.LENGTH_SHORT).show();
                    etbirth.requestFocus();
                    return;
                }
                if(email.length()==0){
                    Toast.makeText(MainActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                    etemail.requestFocus();
                    return;
                }
                if(!male.isChecked() && !female.isChecked()){
                    Toast.makeText(MainActivity.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    female.requestFocus();
                    return;
                }
                if(!agree.isChecked()){
                    Toast.makeText(MainActivity.this, "동의함을 체크해주세요.", Toast.LENGTH_SHORT).show();
                    agree.requestFocus();
                    return;
                }
                else{
                    flag = true;
                }

                //남자, 여자 체크 값 가져오기
                if(male.isChecked()){
                    gender = "M";
                }
                else if(female.isChecked()){
                    gender = "F";
                }

                if(agree.isChecked()){
                    agreement = "Y";
                }

                if(flag){
                    next_info.setEnabled(true);
                    next_info.setBackgroundResource(R.color.button);
                    // 데이터 전달
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