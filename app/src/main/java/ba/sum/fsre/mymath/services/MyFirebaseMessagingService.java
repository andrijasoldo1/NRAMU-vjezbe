package ba.sum.fsre.mymath.services;

import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            this.showNotification(title, body);
        }
    }

    private void showNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyMath")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}