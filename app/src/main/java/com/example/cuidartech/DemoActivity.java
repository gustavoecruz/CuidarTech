package com.example.cuidartech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DemoActivity extends AppCompatActivity {

    private EditText usuarioDemo;
    private Button entrenarButton;
    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        init();

        entrenarButton = (Button) findViewById(R.id.buttonEntrenarModelo);
        entrenarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAudioRecorderActivity();
            }
        });

        testButton = (Button) findViewById(R.id.buttonTestearModelo);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAudioRecognizerActivity();
            }
        });
    }

    private void init(){
        usuarioDemo = findViewById(R.id.editTextUsuarioDemo);
    }

    private void openAudioRecorderActivity(){
        String usuario = usuarioDemo.getText().toString();
        if(usuario.length() != 0){
            Intent intent = new Intent(this, AudioRecorderActivity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        }
        else{
            Toast.makeText(DemoActivity.this, "Se debe ingresar el usuario", Toast.LENGTH_LONG).show();
        }
    }

    private void openAudioRecognizerActivity(){
        String usuario = usuarioDemo.getText().toString();
        if(usuario.length() != 0){
            Intent intent = new Intent(this, AudioRecognizerActivity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        }
        else{
            Toast.makeText(DemoActivity.this, "Se debe ingresar el usuario", Toast.LENGTH_LONG).show();
        }
    }

}