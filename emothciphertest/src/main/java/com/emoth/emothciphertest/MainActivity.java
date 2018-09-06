package com.emoth.emothciphertest;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.emoth.emothcipher.sm3.SM3;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public static void onButtonClick(View view) {
        EditText editText = view.getRootView().findViewById(R.id.editText);
        TextView textView = view.getRootView().findViewById(R.id.textView);
        try {
            textView.setText(SM3.sm3(editText.getText().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
