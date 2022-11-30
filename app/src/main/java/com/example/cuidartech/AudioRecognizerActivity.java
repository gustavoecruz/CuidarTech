package com.example.cuidartech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AudioRecognizerActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;

    public static String fileName = "recorder.MPEG4";
    String file = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName;
    private ImageButton btnRecord;
    public EditText usuarioEditText;
    private final int RCODE = 28;
    String nameFile;
    TextView resultText;
    String serviceValue;

    // Create a Cloud Storage reference from the app
    private StorageReference storageRef;

    //Data
    String usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recognizer);

        //Obtengo el email del usuario
        Bundle extras = getIntent().getExtras();
        usuario = extras.getString("usuario");
        Log.i("Usuario", usuario);
        inicializador();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AudioRecognizerActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
        }

        //Storage
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void inicializador(){
        btnRecord = (ImageButton) findViewById(R.id.btnRecord);
        usuarioEditText = findViewById(R.id.usuarioET);
        usuarioEditText.setText(this.usuario);
        usuarioEditText.setFocusable(false);
        this.nameFile = "";
        resultText = (TextView)findViewById(R.id.textView3);
    }

    public void onClick(View v){
        if (v.getId() == R.id.btnRecord) {
            record();
        }
        else if (v.getId() == R.id.btnPlay) {
            playAudio();
        }
        else if (v.getId() == R.id.buttonIdentificar) {
            identificar();
        }
    }

    private void record() {
        if(mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(16);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setOutputFile(file);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            btnRecord.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_baseline_stop_24));
            Toast.makeText(AudioRecognizerActivity.this, "Record", Toast.LENGTH_LONG).show();
        }
        else{
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            btnRecord.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_baseline_keyboard_voice_24));
            Toast.makeText(AudioRecognizerActivity.this, "Stop", Toast.LENGTH_LONG).show();
        }
    }

    private void playAudio() {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(file);
            mp.prepare();
        }catch (IOException e){
            e.printStackTrace();
        }
        mp.start();
        Toast.makeText(AudioRecognizerActivity.this, "Play", Toast.LENGTH_LONG).show();
    }

    private void uploadAudio() {
        Uri file = Uri.fromFile(new File(this.file));
        this.nameFile = "pruebas/" + this.usuario + "/" + System.currentTimeMillis() + "." + "MPEG4";
        StorageReference riversRef = storageRef.child(this.nameFile);
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(AudioRecognizerActivity.this, "Fallo la carga de audio", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AudioRecognizerActivity.this, "La carga del audio fue exitosa", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void identificar(){
        uploadAudio();
        InvocarServicio ws = new InvocarServicio();
        ws.execute();
    }

    private class InvocarServicio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                servicio();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            resultText.setText(serviceValue);
            Toast.makeText(AudioRecognizerActivity.this, "Servicio consultado", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(AudioRecognizerActivity.this, "Servicio por consultar", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    private void servicio() throws IOException {
        // NOTE: you must manually set API_KEY below using information retrieved from your IBM Cloud account.

        String API_KEY = "API_KEY_HERE";

        HttpURLConnection tokenConnection = null;
        HttpURLConnection scoringConnection = null;
        BufferedReader tokenBuffer = null;
        BufferedReader scoringBuffer = null;
        try {
            // Getting IAM token
            URL tokenUrl = new URL("https://iam.cloud.ibm.com/identity/token?grant_type=urn:ibm:params:oauth:grant-type:apikey&apikey=" + API_KEY);
            tokenConnection = (HttpURLConnection) tokenUrl.openConnection();
            tokenConnection.setDoInput(true);
            tokenConnection.setDoOutput(true);
            tokenConnection.setRequestMethod("POST");
            tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            tokenConnection.setRequestProperty("Accept", "application/json");
            tokenBuffer = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()));
            StringBuffer jsonString = new StringBuffer();
            String line;
            while ((line = tokenBuffer.readLine()) != null) {
                jsonString.append(line);
            }
            // Scoring request
            URL scoringUrl = new URL("https://us-south.ml.cloud.ibm.com/ml/v4/deployments/9d7b9686-4e69-432b-a43f-0a7a8aa097ce/predictions?version=2022-10-25");
            String iam_token = "Bearer " + jsonString.toString().split(":")[1].split("\"")[1];
            scoringConnection = (HttpURLConnection) scoringUrl.openConnection();
            scoringConnection.setDoInput(true);
            scoringConnection.setDoOutput(true);
            scoringConnection.setRequestMethod("POST");
            scoringConnection.setRequestProperty("Accept", "application/json");
            scoringConnection.setRequestProperty("Authorization", iam_token);
            scoringConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(scoringConnection.getOutputStream(), "UTF-8");

            // NOTE: manually define and pass the array(s) of values to be scored in the next line
            String payload = "{\"input_data\": [{\"values\": \"" + this.nameFile + "\"}]}";
            writer.write(payload);
            writer.close();

            scoringBuffer = new BufferedReader(new InputStreamReader(scoringConnection.getInputStream()));
            StringBuffer jsonStringScoring = new StringBuffer();
            String lineScoring;
            while ((lineScoring = scoringBuffer.readLine()) != null) {
                jsonStringScoring.append(lineScoring);
            }
            JSONObject jsonObject = new JSONObject(jsonStringScoring.toString());
            JSONArray predictions = jsonObject.getJSONArray("predictions");
            JSONObject values = predictions.getJSONObject(0);
            String value = values.getString("values");
            this.serviceValue = value;
        } catch (IOException | JSONException e) {
            System.out.println("The URL is not valid.");
            System.out.println(e.getMessage());
        }
        finally {
            if (tokenConnection != null) {
                tokenConnection.disconnect();
            }
            if (tokenBuffer != null) {
                tokenBuffer.close();
            }
            if (scoringConnection != null) {
                scoringConnection.disconnect();
            }
            if (scoringBuffer != null) {
                scoringBuffer.close();
            }
        }
    }


}
