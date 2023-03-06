package com.example.pepperrobot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

//Sources:
//https://medium.com/voice-tech-podcast/android-speech-to-text-tutorial-8f6fa71606ac
//https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/say.html
//https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch2_principles/sync_async.html
//https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/perception/tuto/take_picture_tutorial.html
//https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/perception/tuto/people_characteristics_tutorial.html
//https://www.baeldung.com/httpurlconnection-post

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private TextView textView;
    private ImageView micButton;
    String humanResponse = "";
    QiContext globalQiContext;

    private Animate animate;
    private Animation animation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        textView = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                textView.setText("");
                textView.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                textView.setText(data.get(0));
                Log.d("RESULT", data.get(0));
                humanResponse = data.get(0);

                new Thread(() -> {
                    try {
                        //Pepper sends the message to the server and outputs a response
                        //Start the server from Anaconda prompt with these steps:
                        //1. Download the Server folder from Gitlab
                        //2. In anaconda prompt, type 'conda activate uvicorn'
                        //3. Run 'python main.py' from the Server folder


                        //Get Pepper's response from the server
                        EditText edit= (EditText)findViewById(R.id.edittext);
                        URL url = new URL("http://" + edit.getText().toString().trim() + ":8888" + "/NLP/getResponse");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/json");
                        con.setRequestProperty("Accept", "application/json");
                        con.setDoOutput(true);
                        String jsonInputString = "{\"question\":" + '\"' + humanResponse + "\"}";
                        try(OutputStream os = con.getOutputStream()) {
                            byte[] input = jsonInputString.getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }

                        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(con.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            JSONObject json = new JSONObject(response.toString());
                            String answer = json.getString("answer");
                            System.out.println(answer);


                            //Pepper talks
                            if(!answer.contains("[CLS]") && !answer.equals("")) { // Pepper understands the sentence
                                Future<Say> say = SayBuilder.with(globalQiContext) // Create the builder with the context.
                                        .withText(answer) // Set the text to say.
                                        .buildAsync(); // Build the say action.

                                // Execute the action.
                                say.get().async().run();
                            }
                            else{ //Pepper does not understand the sentence
                                Future<Say> say = SayBuilder.with(globalQiContext) // Create the builder with the context.
                                        .withText("I'm sorry, but I don't understand what you said.") // Set the text to say.
                                        .buildAsync(); // Build the say action.

                                // Execute the action.
                                say.get().async().run();
                            }

                            //BASELINE method: randomly get an emotion for pepper
                            //String[] emotions = {"joy", "sadness", "love", "angry", "fear", "surprised"};
                            //Random r = new Random();
                            //String emotion = emotions[r.nextInt(emotions.length)];

                            String emotion = json.getString("emotion");
                            System.out.println(json.getString("emotion"));
                            //Express gestures
                            //Source: https://github.com/softbankrobotics-labs/pepper-core-anims
                            switch(emotion) {
                                case "joy":
                                    animation = AnimationBuilder.with(globalQiContext)
                                            .withResources(R.raw.happy)
                                            .build();
                                    break;
                                case "sadness":
                                    animation = AnimationBuilder.with(globalQiContext)
                                            .withResources(R.raw.sad)
                                            .build();
                                    break;
                                case "love":
                                    animation = AnimationBuilder.with(globalQiContext)
                                            .withResources(R.raw.love)
                                            .build();
                                    break;
                                case "anger":
                                    animation = AnimationBuilder.with(globalQiContext)
                                            .withResources(R.raw.angry)
                                            .build();
                                    break;
                                case "fear":
                                    animation = AnimationBuilder.with(globalQiContext)
                                            .withResources(R.raw.fear)
                                            .build();
                                    break;
                                case "surprise":
                                    animation = AnimationBuilder.with(globalQiContext)
                                            .withResources(R.raw.surprised)
                                            .build();
                                    break;
                            }

                            animate = AnimateBuilder.with(globalQiContext)
                                    .withAnimation(animation)
                                    .build();

                            animate.async().run();

                        }

                        con.disconnect();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    };
                }).start();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });


    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Set the qiContext to create a new say action in onResults(...).
        globalQiContext = qiContext;
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
        if (animate != null) {
            animate.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    protected void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }
}