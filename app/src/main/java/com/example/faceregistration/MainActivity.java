package com.example.faceregistration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // 생년월일 달력
    Calendar calendar = Calendar.getInstance();
//    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
//        @Override
//        public void onDateSet(DatePicker view, int year, int month, int day) {
//            calendar.set(Calendar.YEAR, year);
//            calendar.set(Calendar.MONTH, month);
//            calendar.set(Calendar.DAY_OF_MONTH, day);
//            updateLabel();
//        }
//    };

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
//                new DatePickerDialog(MainActivity.this, R.style.translucentDialog, dateSetListener,
//                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, android.R.style.Theme_Holo_Light_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        try {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.getDatePicker().setCalendarViewShown(false);
                datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                datePickerDialog.show();

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(datePickerDialog.getWindow().getAttributes());
                lp.width = 1000;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                Window window = datePickerDialog.getWindow();
                window.setAttributes(lp);

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

        EditText.OnKeyListener keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String name = edname.getText().toString();
                String birth = edbirth.getText().toString();
                String email = edemail.getText().toString();

                // 개인정보 입력 확인
                if(name.length()==0 || birth.length()==0 || email.length()==0 || (!male.isChecked() && !female.isChecked()) || !agree.isChecked() ){
                    next.setEnabled(false);
                    next.setBackgroundResource(R.color.dont);
                } else{
                    next.setEnabled(true);
                    next.setBackgroundResource(R.color.button);
                }
                return false;
            }
        };

        edname.setOnKeyListener(keyListener);
        edbirth.setOnKeyListener(keyListener);
        edemail.setOnKeyListener(keyListener);

        RadioGroup radioGroup = findViewById(R.id.gender_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String name = edname.getText().toString();
                String birth = edbirth.getText().toString();
                String email = edemail.getText().toString();

                // 개인정보 입력 확인
                if(name.length()==0 || birth.length()==0 || email.length()==0 || (!male.isChecked() && !female.isChecked()) || !agree.isChecked() ){
                    next.setEnabled(false);
                    next.setBackgroundResource(R.color.dont);
                } else{
                    next.setEnabled(true);
                    next.setBackgroundResource(R.color.button);
                }
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edname.getText().toString();
                String birth = edbirth.getText().toString();
                String email = edemail.getText().toString();

                // 개인정보 입력 확인
                if(name.length()==0 || birth.length()==0 || email.length()==0 || (!male.isChecked() && !female.isChecked()) || !agree.isChecked() ){
                    next.setEnabled(false);
                    next.setBackgroundResource(R.color.dont);
                } else{
                    next.setEnabled(true);
                    next.setBackgroundResource(R.color.button);
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edname.getText().toString();
                String birth = edbirth.getText().toString();
                String email = edemail.getText().toString();
                String gender = "";
                String agreement = "";

                if(male.isChecked()){
                    gender = "M";
                } else if(female.isChecked()){
                    gender = "F";
                }

                if(agree.isChecked()){
                    agreement = "Y";
                }

                Privacy privacy = new Privacy(name, birth, email, gender, agreement);
                Intent intentSend = new Intent(getApplicationContext(), Simulation.class);
                intentSend.putExtra("class", privacy);
                startActivity(intentSend);
                finish();
            }
        });
    }
}
