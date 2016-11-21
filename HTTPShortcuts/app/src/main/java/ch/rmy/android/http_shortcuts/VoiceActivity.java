package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Promise;


import java.util.Locale;
import ch.rmy.android.http_shortcuts.http.Executor;


public class VoiceActivity extends Activity {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        final String shortcutName = getIntent().getStringExtra(SearchManager.QUERY);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.e("TTS", "Initilization Success!");
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }

                    Bundle params = new Bundle();
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
                    tts.speak("Shortcut " + shortcutName + " executed", TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
                    Log.e("TTS", "Saying: Shortcut " + shortcutName + " executed");
                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.e("TTS", "Started talking: " + utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                Log.e("adri", "Finished talking: " + utteranceId);
                finish();
            }

            @Override
            public void onError(String utteranceId) {
                Log.e("adri", "Error talking: " + utteranceId);
            }
        });

        Executor executor = new Executor(this);
        Promise<Void, Void, Void> promise = executor.execute(shortcutName);

        if (promise.isPending()) {
            promise.always(new AlwaysCallback<Void, Void>() {
                @Override
                public void onAlways(Promise.State state, Void resolved, Void rejected) {
                    finishWithoutAnimation();
                }
            });
        } else {
            finishWithoutAnimation();
        }
    }

    private void finishWithoutAnimation() {
        //finish();
        overridePendingTransition(0, 0);
    }


    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}



