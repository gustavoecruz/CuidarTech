package com.example.cuidartech;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cuidartech.Interface.vdgBackendAPI;
import com.example.cuidartech.Model.RolDeUsuario;
import com.example.cuidartech.Model.Ubicacion;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private EditText usuario;
    private EditText password;
    private Button loginButton;
    private Button openMainButton;
    private Button demoButton;
    private SharedPreferences myPreferences;
    private SharedPreferences.Editor editorPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inicializador();

        if(checkSessionActive()){
            finish();
            openMainActivity();
        }

        //Ingresar usuario
        loginButton = (Button) findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        //Entrar a Demo
        demoButton = (Button) findViewById(R.id.buttonDemo);
        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                demo();
            }
        });
    }

    private void login(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vdg-back.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        vdgBackendAPI vdgBackendAPI = retrofit.create(com.example.cuidartech.Interface.vdgBackendAPI.class);

        usuario = findViewById(R.id.editTextUsuario);
        password = findViewById(R.id.editTextPassword);

        Map<String, String> info = new HashMap<String, String>();
        info.put("email", "agresor1@agresor1.com");
        info.put("contrasena","a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");

        String sha = sha256(password.getText().toString());

//        info.put("email", "" + usuario.getText().toString());
//        info.put("contrasena", "" + sha);

        openMainActivity();

        Call<RolDeUsuario> call = vdgBackendAPI.loginApp(info);

        call.enqueue(new Callback<RolDeUsuario>() {
            @Override
            public void onResponse(Call<RolDeUsuario> call, Response<RolDeUsuario> response) {
                if(!response.isSuccessful()){
                    Log.i("Error", "Código: " + response.errorBody());
                    return;
                }
                Log.i("Login", " " + response.body());
                if( RolDeUsuario.DAMNIFICADA.toString() == response.body().toString() ||
                    RolDeUsuario.VICTIMARIO.toString() == response.body().toString()){
                        //gaurdado de inicio de sesion
                        editorPreferences.putString("emailUsuarioCuidarTech", usuario.getText().toString());
                        editorPreferences.putBoolean("sessionUsuarioCuidarTech", true);
                        editorPreferences.apply();
                        openMainActivity();
                }
            }

            @Override
            public void onFailure(Call<RolDeUsuario> call, Throwable t) {
                Log.i("Error", "Error en la llamada");
                Toast.makeText(Login.this, "Usuario o contraseña incorrecta", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void demo(){
        usuario = findViewById(R.id.editTextUsuario);
        openDemoActivity();
    }

    public static String sha256(final String base) {
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        if(checkSessionActive()){
            intent.putExtra("usuario", this.myPreferences.getString("emailUsuarioCuidarTech", ""));
        }
        else{
            intent.putExtra("usuario", usuario.getText().toString());
        }
        startActivity(intent);
    }

    public void inicializador(){
        myPreferences = this.getSharedPreferences("CuidarTech", Context.MODE_PRIVATE);
        editorPreferences = myPreferences.edit();
    }

    public Boolean checkSessionActive(){
        Boolean sessionActive = this.myPreferences.getBoolean("sessionUsuarioCuidarTech", false);
        return sessionActive;
    }

    public void openDemoActivity(){
        Intent intent = new Intent(this, DemoActivity.class);
        intent.putExtra("usuario", usuario.getText().toString());
        startActivity(intent);
    }

}