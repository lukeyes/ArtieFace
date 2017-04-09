package com.lukeyes.artieface;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;
import java.util.UUID;

class SpeechService {

    private TextToSpeech textToSpeech;
    private final UtteranceProgressListener _listener;
    private FullscreenActivity _context;

    SpeechService(FullscreenActivity context) {
        _listener = getListener();
        _context = context;
        textToSpeech =
                new TextToSpeech(
                        context,
                        new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(final int status) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (status == TextToSpeech.SUCCESS) {
                            // Set preferred language to US english.
                            // Note that a language may not be available, and the result will indicate this.
                            int result = textToSpeech.setLanguage(Locale.UK);
                            // Try this someday for some interesting results.
                            // int result mTts.setLanguage(Locale.FRANCE);
                            if (result == TextToSpeech.LANG_MISSING_DATA ||
                                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                // Lanuage data is missing or the language is not supported.
                                Log.d("TTS", "Language is not available.");
                            } else {
                                // Check the documentation for other possible result codes.
                                // For example, the language may be available for the locale,
                                // but not for the specified country and variant.

                                // The TTS engine has been successfully initialized.

                            }
                        } else {
                            // Initialization failed.
                            Log.e("TTS", "Could not initialize TextToSpeech.");
                        }

                        textToSpeech.setOnUtteranceProgressListener(_listener);
                    }
                }).start();

            }
        });

    }


    private UtteranceProgressListener getListener() {
        return new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onBeginSynthesis(String utteranceId,
                                         int sampleRateInHz,
                                         int audioFormat,
                                         int channelCount) {

            }

            @Override
            public void onDone(String utteranceId) {
                _context.stopSpeaking();

            }

            @Override
            public void onError(String utteranceId) {
               // view.talk(false);
            }
        };
    }

    int speak(CharSequence text) {
        return speak(text, TextToSpeech.QUEUE_FLUSH);
    }

    private int speak(CharSequence text, int queueMode) {

        return speak(text,queueMode,null);
    }

    private int speak(CharSequence text, int queueMode, Bundle params) {
        return speak(text,queueMode,params, UUID.randomUUID().toString());
    }

    private int speak(CharSequence text, int queueMode, Bundle params, String utteranceId) {

        displayText(text.toString());
        int result = textToSpeech.speak(text,queueMode,params,utteranceId);

        return result;
    }

    private void displayText(final String text) {
        _context.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                Toast.makeText(_context, text, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void close() {
        // Don't forget to shutdown!
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}
