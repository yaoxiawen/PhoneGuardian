package service;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;
import com.example.admin.phoneguardian.R;
import java.util.Timer;
import java.util.TimerTask;
import receiver.WidgetTest;

//更新widget的服务
public class WidgetService extends Service {

    private AppWidgetManager awm;
    private Timer timer;
    private TimerTask task;
    private int count = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取AppWidgetManager对象
        awm = AppWidgetManager.getInstance(this);
        //定时器
        timer = new Timer();
        //定时任务
        task = new TimerTask() {
            @Override
            public void run() {
                //获取布局，传入包名和布局id
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.view_widget);
                //获取widget，传入上下文和widget类，上下文要用整个应用的上下文
                ComponentName provider = new ComponentName(getApplicationContext(), WidgetTest.class);
                //对布局设置内容，不是通过id获取组件，
                // 还是通过setTextViewText方法，传入组件id和组件内容
                views.setTextViewText(R.id.tv_widget,"This is widget test! "+(++count));
                //AppWidgetManager更新widget，传入widget和布局对象
                awm.updateAppWidget(provider,views);
            }
        };
        //每隔1秒更新widget
        timer.schedule(task,0,1000);
    }

    @Override
    public void onDestroy() {
        //取消定时器和定时任务
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        super.onDestroy();
    }
}
