package com.example.cuidartech.Interface;

import com.example.cuidartech.Model.RolDeUsuario;
import com.example.cuidartech.Model.Ubicacion;
import com.example.cuidartech.Model.Usuario;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface vdgBackendAPI {

    //Usuario
    @GET("Usuario")
    Call<List<Usuario>> getUsuarios();

    @POST("Usuario/loginApp")
    Call<RolDeUsuario> loginApp(@Body Map<String, String> info);

    //Ubicaciones
    @POST("Ubicacion/postUbi/{emailUsuario}")
    Call<Ubicacion> agregarUbicacion(@Path("emailUsuario") String emailUsuario, @Body Map<String, Double> posicion);

}
