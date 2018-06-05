package bean;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable icon;
    private String name;
    private long size;
    private String packageName;
    /**
     * 为用户app还是系统app
     */
    private boolean userApp;
    /**
     * 安装在内存中还是SD卡中
     */
    private boolean isRom;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isUserApp() {
        return userApp;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }

    public boolean isRom() {
        return isRom;
    }

    public void setRom(boolean rom) {
        isRom = rom;
    }
}