package ru.demo.messenger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Set;

import ru.demo.messenger.chats.single.SingleChatActivity;

public class MessengerFcmListenerService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        Intent intent = new Intent(this, MessengerRegistrationIntentService.class);
        startService(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map<String, String> data = remoteMessage.getData();
        if (data == null) {
            return;
        }
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            Set<String> keySet = data.keySet();


            String message = data.get("text");
            long chatId = 0;
            try {
                chatId = Long.parseLong(data.get("chain_id"));
            } catch (Exception e) {
                return;
            }

            // String got_server_key = data.getString("server_key");
            // if (!got_server_key.equals(saved_server_key)) {return;}

            final Intent intent = SingleChatActivity.openChatWithId(this, chatId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
//                            .setSmallIcon(R.drawable.push_icon)
                            .setSmallIcon(android.R.drawable.ic_dialog_email)
                            .setColor(ContextCompat.getColor(this, R.color.main_blue))
                            .setContentTitle(this.getString(R.string.app_name))
                            .setAutoCancel(true)
                            .setContentText(message)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, mBuilder.build());
        }
    }

}
