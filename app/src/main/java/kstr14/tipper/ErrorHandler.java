package kstr14.tipper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;


/**
 * Created by Kristine on 04-06-2015.
 */
public class ErrorHandler {

    /**
     * Shows an AlertDialog telling the user there is a problem with the internet connection.
     * Clicking OK on the dialog sends the user back to the previous activity.
     */
    public static void showConnectionErrorAlert(final Activity activity, final Intent intent) {
        new AlertDialog.Builder(activity)
                .setTitle("Connection error")
                .setMessage("The application cannot connect to the database in the moment. Please check the internet connection and try again.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(intent);
                    }
                })
                .setIcon(R.drawable.ic_action_warning)
                .show();
    }
}
