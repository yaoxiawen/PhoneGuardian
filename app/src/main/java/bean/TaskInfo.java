package bean;

import android.graphics.drawable.Drawable;

public class TaskInfo {
    private Drawable icon;
    private String name;
    private long size;
    private String packageName;
    /**
     * 为用户app还是系统app
     */
    private boolean userTask;
    /**
     * 是否被勾选
     */
    private boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

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

    public boolean isUserTask() {
        return userTask;
    }

    public void setUserTask(boolean userApp) {
        this.userTask = userTask;
    }
}