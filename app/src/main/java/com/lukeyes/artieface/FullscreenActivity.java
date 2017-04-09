package com.lukeyes.artieface;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FullscreenActivity extends AppCompatActivity {

    public Button connectButton;

    public Button disconnectButton;
    SpeechService speechService;
    Context context;

    private VisibilityModule mVisibilityModule;

    private static final String AUTO_ADDRESS = "192.168.1.20";

    Thread blinkThread = null;
    Emotion currentEmotion;
    ObjectMapper objectMapper;
    final private String MY_ID = "client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        this.context = this;

        View mControlsView = findViewById(R.id.fullscreen_content_controls);
        View mContentView = findViewById(R.id.fullscreen_content);
        ActionBar actionBar = getSupportActionBar();
        mVisibilityModule =
                new VisibilityModule(
                        mContentView,
                        mControlsView,
                        actionBar);

        connectButton = (Button) findViewById(R.id.button_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnect();
            }
        });
        // connectButton.setOnTouchListener(mDelayHideTouchListener);
        connectButton.setEnabled(true);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVisibilityModule.toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        disconnectButton = (Button) findViewById(R.id.disconnect_button);
        //    disconnectButton.setOnTouchListener(mDelayHideTouchListener);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDisconnect();
            }
        });
        disconnectButton.setEnabled(false);

        speechService = new SpeechService(this);
        setHappy();
        makeBlinkThread();
        blinkThread.start();
        objectMapper = new ObjectMapper();
    }

    private void setHappy() {

        ImageView base = (ImageView) findViewById(R.id.faceBase);
        ImageView eyes = (ImageView) findViewById(R.id.eyes);
        ImageView mouth = (ImageView) findViewById(R.id.mouth);

        currentEmotion = Emotion.HAPPY;
        currentEmotion.init(base, eyes, mouth);

    }

    private void startSpeaking() {
        ImageView mouth = (ImageView) findViewById(R.id.mouth);
        currentEmotion.startSpeaking(mouth);
    }

    public void stopSpeaking() {
        System.out.println("Stop speaking");
        ImageView mouth = (ImageView) findViewById(R.id.mouth);
        currentEmotion.stopSpeaking(mouth);
    }

    private void blink() {
        ImageView eyes = (ImageView) findViewById(R.id.eyes);
        currentEmotion.blink(eyes);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        mVisibilityModule.delayedHide(100);
    }

    @Override
    public void onDestroy() {

        speechService.close();

        super.onDestroy();
    }

    public void onConnect() {
        connectWebSocket(AUTO_ADDRESS);
    }

    public void onDisconnect() {
        Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
            }
        });
    }

    public void displayString(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        speechService.speak(message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startSpeaking();
            }
        });
    }

    private void connectWebSocket(String address) {
        URI uri;
        try {
            String socketAddress = String.format("ws://%s:8080", address);
            String toastText = String.format("Connecting to %s", socketAddress);
            Toast.makeText(this,toastText,Toast.LENGTH_SHORT).show();
            uri = new URI(socketAddress);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        WebSocketClient mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(false);
                        disconnectButton.setEnabled(true);
                        displayString("Opened");
                    }
                });

                Message message = new Message();
                message.sender = MY_ID;
                message.recipient = "server";
                message.message = "Hello from " + Build.MANUFACTURER + " " + Build.MODEL;

                try {
                    String jsonMessage = objectMapper.writeValueAsString(message);
                    this.send(jsonMessage);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(final String message) {
                Log.i("Websocket", message);
                try {
                    final Message parsedMessage = objectMapper.readValue(message, Message.class);
                    if(MY_ID.equals(parsedMessage.recipient)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayString(parsedMessage.message);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("Websocket", "Closed " + reason);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setEnabled(true);
                        disconnectButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onError(Exception ex) {
                Log.i("Websocket", "Error " + ex.getMessage());
            }
        };

        mWebSocketClient.connect();
    }

    private void makeBlinkThread() {
        blinkThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            blink();
                        }
                    });


                    // blink every 2 = 10 seconds
                    long blinkSeconds = (long) ((Math.random() * 8000) + 2000);
                    System.out.println("Blink ms - " + blinkSeconds);

                    try {
                        Thread.sleep(blinkSeconds);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
