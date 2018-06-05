package utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;
//废弃
public class ServiceStatusUtils {
    /**
     * 检测服务是否正在运行
     * @param ctx
     * @param service
     * @return
     */
    public static boolean isServiceRunning(Context ctx,String service){
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        //getRunningServices该方法已被废弃
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo runningService:runningServices) {
            String name = runningService.service.getClassName();
            if(service.equals(name)){
                return true;
            }
        }
        return false;
    }
}
