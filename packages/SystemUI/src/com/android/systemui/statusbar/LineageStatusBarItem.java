package com.android.systemui.statusbar;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewParent;

class LineageStatusBarItem {
    public interface Manager {
        public void addDarkReceiver(DarkReceiver darkReceiver);
        public void addVisibilityReceiver(VisibilityReceiver visibilityReceiver);
    }

    public interface DarkReceiver {
        public void onDarkChanged(Rect area, float darkIntensity, int tint);
        public void setFillColors(int darkColor, int lightColor);
    }

    public interface VisibilityReceiver {
        public void onVisibilityChanged(boolean isVisible);
    }

    // Locate parent LineageStatusBarItem.Manager
    public static Manager findManager(View v) {
        ViewParent parent = v.getParent();
        if (parent == null) {
            return null;
        } else if (parent instanceof Manager) {
            return (Manager) parent;
        } else if (parent instanceof View) {
            return findManager((View) parent);
        } else {
            return null;
        }
    }
}