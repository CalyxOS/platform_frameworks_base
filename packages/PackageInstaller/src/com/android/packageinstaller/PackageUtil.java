/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.android.packageinstaller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.security.cert.CertificateException;
import java.util.Set;

/**
 * This is a utility class for defining some utility methods and constants
 * used in the package installer application.
 */
public class PackageUtil {
    private static final String LOG_TAG = "PackageInstaller";

    private static final String BELLIS_PACKAGE_NAME = "org.calyxos.bellis";

    /**
     * Signatures of CalyxOS repo apps that are allowed to install packages when this would
     * otherwise be prevented by garlic level Safest.
     */
    private static final Set<Pair<String, String>> GARLIC_LEVEL_TRUSTED_INSTALLERS = Set.of(
            new Pair<>("com.aurora.store", "3082035F30820247A0030201020204746B1B22300D06092A864886F70D01010B05003060310B300906035504061302554B310C300A060355040813034F5247310C300A060355040713034F524731133011060355040A130A6664726F69642E6F7267310F300D060355040B13064644726F6964310F300D060355040313064644726F6964301E170D3139303431333035343835355A170D3436303832393035343835355A3060310B300906035504061302554B310C300A060355040813034F5247310C300A060355040713034F524731133011060355040A130A6664726F69642E6F7267310F300D060355040B13064644726F6964310F300D060355040313064644726F696430820122300D06092A864886F70D01010105000382010F003082010A0282010100C21FFD1B6D1DCAF1AF18F4AD483714A2261BB8A71465CF5D831C3DC0383AF949B8BDE433594C4476CC9E6EC5EA21DF3147BBAB13305AC22A841BBCEBF0192A00A19EB79C1F1117F4CFDCA9B05A38EE24AC737906470BA7193A9981BF3BB413790A99D7CC11F878CE4885123434C86EF22DE4DDD396821B5211B168862D37EB11705F41DC493CF9F5B28FA7F8E64578D32BAFBC0C817742E58779746012445C8D716BC9170B225E64F3EFE6F0534E8E464C13180DEB0AC719E8EA75D003798AF44848FF1253263A4B1DF9D522E4D4699E04F4F5DCF1E7C56615AD76ED821033852CC521CA69BFE3FD9C2B2867663BE303BB936111638FF9A8241F8FCC8D66E1290203010001A321301F301D0603551D0E0416041402443ECFB8376A2F8356212A3B4FD7B3987BBEBE300D06092A864886F70D01010B050003820101008E8FE70E2A156F4A22FB2A20C4C7ED2D680B379E71A5D19FB51E380674AD94CD27DD36FB77A781E1E23616FF30CC35A280EC824E7E392F6868FFD7C21252EFD86226621CB01D8271E0D9646AA529C184D796F189F20C6595AA4A5E9EE748BBF589A1D48B0BC71B54E053E5093ABD64B85D8933BCBD8315A5522D98797D2BF2DA15FC1DAE043C9983AB85C9D1A120336591E7105CCD71EC244EA9D744DE70BE167F17CBF8EC50F7A794FF027F94591CA37B689912027EE6BFD3660CA924294BC9C0C30067D4169B44E513FA086C4763EAFF90B05A66993045BFC28032FF8122E38A31E9467D06BD5E0BCEEC8DFA811FBCAD29915126A69327D98F0E655D1CDB1B"),
            new Pair<>("org.fdroid.basic", "3082036130820249A00302010202042A3D3590300D06092A864886F70D01010B05003060310B300906035504061302554B310C300A060355040813034F5247310C300A060355040713034F524731133011060355040A130A6664726F69642E6F7267310F300D060355040B13064644726F6964310F300D060355040313064644726F69643020170D3233303430353038303235315A180F32303530303832313038303235315A3060310B300906035504061302554B310C300A060355040813034F5247310C300A060355040713034F524731133011060355040A130A6664726F69642E6F7267310F300D060355040B13064644726F6964310F300D060355040313064644726F696430820122300D06092A864886F70D01010105000382010F003082010A02820101009B339156D36085FC67EDD920F8278D01D65DD9EFF64042560D0F6C440FFD1E1A3478B35138B46403E1D79C48ACDBE6D8BE37D112F70DF948DBA16BE82E3A1112AA07E280C5EF25D3F82FB1F0E7CD2FE53D263517C5E5C9EFB871975E61EA581E887D6FABBA7A02E6574D38BB3D7979770C91D521E98C00A8C8872E6B44D5BB7581B73D99E7528083488B6DF374B2C1DAD0FFC58B593DE0EEF0BE07F349541AEE69843F18730412FDC6B7D6ABD9B951643C6289D5EB39934A53F296836A95472982A83B478F0FB094CFF373489214413A90C84D01F281AC5BD90C0BDDD29321CFAD48F5B68F68FE9C207EB12478BAF1EB2A2C29385DA1F9C92554C8E7320190E50203010001A321301F301D0603551D0E041604149E71EB04BCBA667200CBD1400EC41C6CF0FE9BD3300D06092A864886F70D01010B050003820101001B005117E824AF14A43543373AD3A20D998390EC11C188AC762045AC7291C5BC1FFB4577BA74BB30873F2EBE9930DF0E51830B57FD4147FE7389EB414607FB7F698CD1E8D1BAE46AC38B9280B6B36FF2E40C0C4E128EFCDA6EA06427BC0BAFD0A322B2E5329BFEEC71BF5EDC2D91DBFEEC33463E17E6D74F548548FA3B9CEF3BEB32965EF4EDC885241256CFE4DA81AB1C5CAAA315E3D4ADD23C7A2C98E0D099BA16269E58374D44DD9FE548D97CB3A225D9F89EC8CA41A14D912A09457C47C42FC03048E7C163B2355E005A4954B009EFDDEDD8EC525685E5AD8BC4DD617C819D11606B639FEE49BD356C9288DBE458104CF36A4AF9DEBF2A342370E4243E65")
    );

    public static final String PREFIX="com.android.packageinstaller.";
    public static final String INTENT_ATTR_INSTALL_STATUS = PREFIX+"installStatus";
    public static final String INTENT_ATTR_APPLICATION_INFO=PREFIX+"applicationInfo";
    public static final String INTENT_ATTR_PERMISSIONS_LIST=PREFIX+"PermissionsList";
    //intent attribute strings related to uninstall
    public static final String INTENT_ATTR_PACKAGE_NAME=PREFIX+"PackageName";
    private static final String DOWNLOADS_AUTHORITY = "downloads";
    private static final String SPLIT_BASE_APK_END_WITH = "base.apk";

    /**
     * Utility method to get package information for a given {@link File}
     */
    @Nullable
    public static PackageInfo getPackageInfo(Context context, File sourceFile, int flags) {
        String filePath = sourceFile.getAbsolutePath();
        if (filePath.endsWith(SPLIT_BASE_APK_END_WITH)) {
            File dir = sourceFile.getParentFile();
            if (dir.listFiles().length > 1) {
                // split apks, use file directory to get archive info
                filePath = dir.getPath();
            }
        }
        try {
            return context.getPackageManager().getPackageArchiveInfo(filePath, flags);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static View initSnippet(View snippetView, CharSequence label, Drawable icon) {
        ((ImageView)snippetView.findViewById(R.id.app_icon)).setImageDrawable(icon);
        ((TextView)snippetView.findViewById(R.id.app_name)).setText(label);
        return snippetView;
    }

    /**
     * Utility method to display a snippet of an installed application.
     * The content view should have been set on context before invoking this method.
     * appSnippet view should include R.id.app_icon and R.id.app_name
     * defined on it.
     *
     * @param pContext context of package that can load the resources
     * @param componentInfo ComponentInfo object whose resources are to be loaded
     * @param snippetView the snippet view
     */
    public static View initSnippetForInstalledApp(Context pContext,
            ApplicationInfo appInfo, View snippetView) {
        return initSnippetForInstalledApp(pContext, appInfo, snippetView, null);
    }

    /**
     * Utility method to display a snippet of an installed application.
     * The content view should have been set on context before invoking this method.
     * appSnippet view should include R.id.app_icon and R.id.app_name
     * defined on it.
     *
     * @param pContext context of package that can load the resources
     * @param componentInfo ComponentInfo object whose resources are to be loaded
     * @param snippetView the snippet view
     * @param UserHandle user that the app si installed for.
     */
    public static View initSnippetForInstalledApp(Context pContext,
            ApplicationInfo appInfo, View snippetView, UserHandle user) {
        final PackageManager pm = pContext.getPackageManager();
        Drawable icon = appInfo.loadIcon(pm);
        if (user != null) {
            icon = pContext.getPackageManager().getUserBadgedIcon(icon, user);
        }
        return initSnippet(
                snippetView,
                appInfo.loadLabel(pm),
                icon);
    }

    static final class AppSnippet implements Parcelable {
        @NonNull public CharSequence label;
        @NonNull public Drawable icon;
        public int iconSize;

        AppSnippet(@NonNull CharSequence label, @NonNull Drawable icon, Context context) {
            this.label = label;
            this.icon = icon;
            final ActivityManager am = context.getSystemService(ActivityManager.class);
            this.iconSize = am.getLauncherLargeIconSize();
        }

        private AppSnippet(Parcel in) {
            label = in.readString();
            byte[] b = in.readBlob();
            Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
            icon = new BitmapDrawable(Resources.getSystem(), bmp);
            iconSize = in.readInt();
        }

        @Override
        public String toString() {
            return "AppSnippet[" + label + " (has icon)]";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(label.toString());

            Bitmap bmp = getBitmapFromDrawable(icon);
            dest.writeBlob(getBytesFromBitmap(bmp));
            bmp.recycle();

            dest.writeInt(iconSize);
        }

        private Bitmap getBitmapFromDrawable(Drawable drawable) {
            // Create an empty bitmap with the dimensions of our drawable
            final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            // Associate it with a canvas. This canvas will draw the icon on the bitmap
            final Canvas canvas = new Canvas(bmp);
            // Draw the drawable in the canvas. The canvas will ultimately paint the drawable in the
            // bitmap held within
            drawable.draw(canvas);

            // Scale it down if the icon is too large
            if ((bmp.getWidth() > iconSize * 2) || (bmp.getHeight() > iconSize * 2)) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, iconSize, iconSize, true);
                if (scaledBitmap != bmp) {
                    bmp.recycle();
                }
                return scaledBitmap;
            }
            return bmp;
        }

        private byte[] getBytesFromBitmap(Bitmap bmp) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                bmp.compress(CompressFormat.PNG, 100, baos);
            } finally {
                try {
                    if (baos != null) {
                        baos.close();
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "ByteArrayOutputStream was not closed");
                }
            }
            return baos.toByteArray();
        }

        public static final Parcelable.Creator<AppSnippet> CREATOR = new Parcelable.Creator<>() {
            public AppSnippet createFromParcel(Parcel in) {
                return new AppSnippet(in);
            }

            public AppSnippet[] newArray(int size) {
                return new AppSnippet[size];
            }
        };
    }

    /**
     * Utility method to load application label
     *
     * @param pContext context of package that can load the resources
     * @param appInfo ApplicationInfo object of package whose resources are to be loaded
     * @param sourceFile File the package is in
     */
    public static AppSnippet getAppSnippet(
            Activity pContext, ApplicationInfo appInfo, File sourceFile) {
        final String archiveFilePath = sourceFile.getAbsolutePath();
        PackageManager pm = pContext.getPackageManager();
        appInfo.publicSourceDir = archiveFilePath;

        if (appInfo.splitNames != null && appInfo.splitSourceDirs == null) {
            final File[] files = sourceFile.getParentFile().listFiles();
            final String[] splits = Arrays.stream(appInfo.splitNames)
                    .map(i -> findFilePath(files, i + ".apk"))
                    .filter(Objects::nonNull)
                    .toArray(String[]::new);

            appInfo.splitSourceDirs = splits;
            appInfo.splitPublicSourceDirs = splits;
        }

        CharSequence label = null;
        // Try to load the label from the package's resources. If an app has not explicitly
        // specified any label, just use the package name.
        if (appInfo.labelRes != 0) {
            try {
                label = appInfo.loadLabel(pm);
            } catch (Resources.NotFoundException e) {
            }
        }
        if (label == null) {
            label = (appInfo.nonLocalizedLabel != null) ?
                    appInfo.nonLocalizedLabel : appInfo.packageName;
        }
        Drawable icon = null;
        // Try to load the icon from the package's resources. If an app has not explicitly
        // specified any resource, just use the default icon for now.
        try {
            if (appInfo.icon != 0) {
                try {
                    icon = appInfo.loadIcon(pm);
                } catch (Resources.NotFoundException e) {
                }
            }
            if (icon == null) {
                icon = pContext.getPackageManager().getDefaultActivityIcon();
            }
        } catch (OutOfMemoryError e) {
            Log.i(LOG_TAG, "Could not load app icon", e);
        }
        return new PackageUtil.AppSnippet(label, icon, pContext);
    }

    private static String findFilePath(File[] files, String postfix) {
        for (File file : files) {
            final String path = file.getAbsolutePath();
            if (path.endsWith(postfix)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Get the maximum target sdk for a UID.
     *
     * @param context The context to use
     * @param uid The UID requesting the install/uninstall
     *
     * @return The maximum target SDK or -1 if the uid does not match any packages.
     */
    static int getMaxTargetSdkVersionForUid(@NonNull Context context, int uid) {
        PackageManager pm = context.getPackageManager();
        final String[] packages = pm.getPackagesForUid(uid);
        int targetSdkVersion = -1;
        if (packages != null) {
            for (String packageName : packages) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                    targetSdkVersion = Math.max(targetSdkVersion, info.targetSdkVersion);
                } catch (PackageManager.NameNotFoundException e) {
                    // Ignore and try the next package
                }
            }
        }
        return targetSdkVersion;
    }


    /**
     * Quietly close a closeable resource (e.g. a stream or file). The input may already
     * be closed and it may even be null.
     */
    static void safeClose(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ioe) {
                // Catch and discard the error
            }
        }
    }

    /**
     * A simple error dialog showing a message
     */
    public static class SimpleErrorDialog extends DialogFragment {
        private static final String MESSAGE_KEY =
                SimpleErrorDialog.class.getName() + "MESSAGE_KEY";

        static SimpleErrorDialog newInstance(@StringRes int message) {
            SimpleErrorDialog dialog = new SimpleErrorDialog();

            Bundle args = new Bundle();
            args.putInt(MESSAGE_KEY, message);
            dialog.setArguments(args);

            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getArguments().getInt(MESSAGE_KEY))
                    .setPositiveButton(R.string.ok, (dialog, which) -> getActivity().finish())
                    .create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }
    }

    /**
     * Determines if the UID belongs to the system downloads provider and returns the
     * {@link ApplicationInfo} of the provider
     *
     * @param uid UID of the caller
     * @return {@link ApplicationInfo} of the provider if a downloads provider exists,
     *          it is a system app, and its UID matches with the passed UID, null otherwise.
     */
    public static ApplicationInfo getSystemDownloadsProviderInfo(PackageManager pm, int uid) {
        final ProviderInfo providerInfo = pm.resolveContentProvider(
                DOWNLOADS_AUTHORITY, 0);
        if (providerInfo == null) {
            // There seems to be no currently enabled downloads provider on the system.
            return null;
        }
        ApplicationInfo appInfo = providerInfo.applicationInfo;
        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && uid == appInfo.uid) {
            return appInfo;
        }
        return null;
    }

    /**
      Return true if the provided package should be considered an allowed source, when
      garlic level is Safest. It is assumed that the garlic level is Safest if Bellis
      is the organization owner and if there is a global restriction on unknown sources.
     */
    public static boolean isTrustedSourceForGarlicLevel(@NonNull final Context context,
            @Nullable final String callingPackage) {
        if (!isOrganizationOwnerBellis(context)) {
            return false;
        }
        final UserManager um = context.getSystemService(UserManager.class);
        if (!um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)) {
            return false;
        }
        // At this point, garlic level Safest is effectively confirmed.
        // Now we check to see if the unknown source is a trusted installer.
        return isGarlicLevelTrustedInstaller(context, callingPackage);
    }

    /**
     * Return true if the provided package is a trusted installation source for garlic level
     * and its signature matches the expected trusted signature.
     */
    private static boolean isGarlicLevelTrustedInstaller(@NonNull final Context context,
            @Nullable final String packageName) {
        for (Pair<String, String> pair : GARLIC_LEVEL_TRUSTED_INSTALLERS) {
            final String trustedPackageName = pair.first;
            final String trustedPackageSignature = pair.second;
            if (!trustedPackageName.equals(packageName)) {
                continue;
            }
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        packageName, PackageManager.PackageInfoFlags.of(
                                PackageManager.GET_SIGNING_CERTIFICATES));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                for (Signature signature : packageInfo.signingInfo.getApkContentsSigners()) {
                    outputStream.write(signature.toByteArray());
                }
                if (Signature.areEffectiveMatch(new Signature(trustedPackageSignature),
                        new Signature(outputStream.toByteArray()))) {
                    return true;
                }
            } catch (NameNotFoundException | IOException | CertificateException ignored) {
            }
        }
        return false;
    }

    /**
     * Return true if the device is organization-owned and if the system user's first
     * managed profile is owned by Bellis.
     */
    private static boolean isOrganizationOwnerBellis(@NonNull final Context context) {
        final DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
        if (!dpm.isOrganizationOwnedDeviceWithManagedProfile()) {
            return false;
        }
        final UserManager um = context.getSystemService(UserManager.class);
        for (int profileId : um.getProfileIds(UserHandle.USER_SYSTEM, false /* enabledOnly */)) {
            if (!um.isManagedProfile(profileId)) {
                continue;
            }
            // Check if the system's first managed profile is owned by the Bellis package.
            final ComponentName componentName = dpm.getProfileOwnerAsUser(profileId);
            return componentName != null
                    && BELLIS_PACKAGE_NAME.equals(componentName.getPackageName());
        }
        return false;
    }
}
