package com.example.root.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;


public class ResultActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent data = getIntent();
        String name = data.getStringExtra("message");
        Toast.makeText(getApplicationContext(),
                name,
                Toast.LENGTH_LONG).show();

    }
}
