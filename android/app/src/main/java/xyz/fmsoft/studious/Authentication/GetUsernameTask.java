package xyz.fmsoft.studious.Authentication;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

import xyz.fmsoft.studious.MainActivity;

/**
 * Created by fredericmurry on 1/1/17.
 */

public class GetUsernameTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetUsernameTask";

    AppCompatActivity mActivity;
    String mScope;
    String mEmail;
    String authToken;

    public GetUsernameTask(AppCompatActivity activity, String name, String scope) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = name;
    }

    /**
     * Executes the asynchronous job. This runs when you call execute()
     * on the AsyncTask instance.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            String token = fetchToken();
            if (token != null) {
                Log.d(TAG, "Got the token");
                Log.d(TAG, token);
                // **Insert the good stuff here.**
                // Use the token to access the user's Google data.
                authToken=token;


            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TIP: Check for network connectivity before starting the AsyncTask.
            Log.d(TAG,e.getMessage());

        }
        return null;
    }

    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
            Log.d(TAG, "Exception: "+userRecoverableException.getMessage());
            if(mActivity instanceof MainActivity){
                ((MainActivity)mActivity).handleException(userRecoverableException);
            }
            else{
                ((LoginActivity)mActivity).handleException(userRecoverableException);
            }

        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            Log.d(TAG,fatalException.getMessage());

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(mActivity instanceof MainActivity){
            ((MainActivity)mActivity).googleLogin(authToken);
        }
        else{
            ((LoginActivity)mActivity).googleLogin(authToken);
        }

    }
}