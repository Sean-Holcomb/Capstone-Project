package com.seantholcomb.goalgetter.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.seantholcomb.goalgetter.DashBoardActivity;
import com.seantholcomb.goalgetter.R;

/**
 * Created by seanholcomb on 10/24/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodoWidgetProvider extends AppWidgetProvider {
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            // Perform this loop procedure for each App Widget that belongs to this provider
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_todo);

                // Create an Intent to launch MainActivity
                Intent intent = new Intent(context, DashBoardActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);

                // Set up the collection
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    setRemoteAdapter(context, views);
                } else {
                    setRemoteAdapterV11(context, views);
                }

                // Tell the AppWidgetManager to perform an update on the current app widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }

        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            super.onReceive(context, intent);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

        }

        /**
         * Sets the remote adapter used to fill in the list items
         *
         * @param views RemoteViews to set the RemoteAdapter
         */
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
            views.setRemoteAdapter(R.id.widget_list,
                    new Intent(context, TodoWidgetRemoteViewsService.class));
        }

        /**
         * Sets the remote adapter used to fill in the list items
         *
         * @param views RemoteViews to set the RemoteAdapter
         */
        @SuppressWarnings("deprecation")
        private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
            views.setRemoteAdapter(0, R.id.widget_list,
                    new Intent(context, TodoWidgetRemoteViewsService.class));
        }
    }
