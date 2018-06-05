package receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import service.WidgetService;
//桌面小控件
public class WidgetTest extends AppWidgetProvider {
    /**
     * 创建第一个桌面小控件时被调用，类似Activity的onCreate方法
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        //开启更新widget的服务
        context.startService(new Intent(context, WidgetService.class));
    }

    /**
     * 创建新的桌面小控件时被调用（多个桌面小控件）
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //开启更新widget的服务
        context.startService(new Intent(context, WidgetService.class));
    }

    /**
     * 删除桌面小控件时被调用（多个桌面小控件）
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 删除最后一个桌面小控件时被调用
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //停止更新widget的服务
        context.stopService(new Intent(context, WidgetService.class));
    }
}
