package com.lukeyes.artieface;

import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

/**
 * Created by luke on 4/9/2017.
 */

public enum Emotion {

    HAPPY {
        @Override
        public void startSpeaking(ImageView mouth) {
            mouth.setAdjustViewBounds(true);
            mouth.setBackgroundResource(R.drawable.happy_mouth);
            AnimationDrawable happyMouthSpeak =(AnimationDrawable) mouth.getBackground();
            happyMouthSpeak.start();
        }

        @Override
        public void stopSpeaking(ImageView mouth) {
            AnimationDrawable happyMouthSpeak = (AnimationDrawable) mouth.getBackground();
            if(happyMouthSpeak != null)
                happyMouthSpeak.stop();
            mouth.setBackgroundResource(R.drawable.happy_mouth_closed);
        }

        @Override
        public void blink(ImageView eyes) {
            eyes.setAdjustViewBounds(true);
            eyes.setBackgroundResource(R.drawable.happy_eyes);
            AnimationDrawable happyEyesBlink =(AnimationDrawable) eyes.getBackground();
            happyEyesBlink.stop();
            happyEyesBlink.start();
        }

        @Override
        public void init(ImageView base, ImageView eyes, ImageView mouth) {
            base.setBackgroundResource(R.drawable.happy);
            eyes.setBackgroundResource(R.drawable.happy_eyes);
            mouth.setBackgroundResource(R.drawable.happy_mouth_closed);
        }
    };

    abstract public void startSpeaking(ImageView mouth);
    abstract public void stopSpeaking(ImageView mouth);
    abstract public void blink(ImageView eyes);
    abstract public void init(ImageView base, ImageView eyes, ImageView mouth);

}
