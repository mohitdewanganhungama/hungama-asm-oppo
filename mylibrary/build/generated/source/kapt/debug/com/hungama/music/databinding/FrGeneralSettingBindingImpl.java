package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class FrGeneralSettingBindingImpl extends FrGeneralSettingBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = new androidx.databinding.ViewDataBinding.IncludedLayouts(56);
        sIncludes.setIncludes(2, 
            new String[] {"layout_progress"},
            new int[] {6},
            new int[] {com.hungama.music.R.layout.layout_progress});
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.rlHeader, 4);
        sViewsWithIds.put(R.id.shimmerLayout, 7);
        sViewsWithIds.put(R.id.scrollView, 8);
        sViewsWithIds.put(R.id.tvHeaderNotification, 9);
        sViewsWithIds.put(R.id.rlGeneralNotification, 10);
        sViewsWithIds.put(R.id.rlEmail, 11);
        sViewsWithIds.put(R.id.tvEmail, 12);
        sViewsWithIds.put(R.id.svEmailNotification, 13);
        sViewsWithIds.put(R.id.divider1, 14);
        sViewsWithIds.put(R.id.llMobileSettings, 15);
        sViewsWithIds.put(R.id.tvMobile, 16);
        sViewsWithIds.put(R.id.svMobileNotification, 17);
        sViewsWithIds.put(R.id.tvHeaderLanguage, 18);
        sViewsWithIds.put(R.id.rlLanguage, 19);
        sViewsWithIds.put(R.id.rlLang, 20);
        sViewsWithIds.put(R.id.tvLang, 21);
        sViewsWithIds.put(R.id.llLang, 22);
        sViewsWithIds.put(R.id.tvLangName, 23);
        sViewsWithIds.put(R.id.ivSettingDetail1, 24);
        sViewsWithIds.put(R.id.tvHeaderExplicit, 25);
        sViewsWithIds.put(R.id.rlExplict, 26);
        sViewsWithIds.put(R.id.rlExplictContent, 27);
        sViewsWithIds.put(R.id.tvExplict, 28);
        sViewsWithIds.put(R.id.tvExplictSubtitle, 29);
        sViewsWithIds.put(R.id.ivExplicitOld, 30);
        sViewsWithIds.put(R.id.svExplicit, 31);
        sViewsWithIds.put(R.id.tvHeaderParentControl, 32);
        sViewsWithIds.put(R.id.rlParentalControl, 33);
        sViewsWithIds.put(R.id.tvParentalControl, 34);
        sViewsWithIds.put(R.id.tvParentalControlSubtitle, 35);
        sViewsWithIds.put(R.id.svParentalControl, 36);
        sViewsWithIds.put(R.id.hidden_chat_view, 37);
        sViewsWithIds.put(R.id.divider2, 38);
        sViewsWithIds.put(R.id.rlGeneralAudioContent, 39);
        sViewsWithIds.put(R.id.rlAudioContent, 40);
        sViewsWithIds.put(R.id.ivAudioContent, 41);
        sViewsWithIds.put(R.id.tvAudioContent, 42);
        sViewsWithIds.put(R.id.tvAudioContentSubtitle, 43);
        sViewsWithIds.put(R.id.ivExplicit, 44);
        sViewsWithIds.put(R.id.svAudioContent, 45);
        sViewsWithIds.put(R.id.divider3, 46);
        sViewsWithIds.put(R.id.rlVideoContent, 47);
        sViewsWithIds.put(R.id.ivVideoContent, 48);
        sViewsWithIds.put(R.id.tvVideoContent, 49);
        sViewsWithIds.put(R.id.svVideoContent, 50);
        sViewsWithIds.put(R.id.tvHeaderAgeConfirmation, 51);
        sViewsWithIds.put(R.id.rlAgeConfirmation, 52);
        sViewsWithIds.put(R.id.tvAgeTitle, 53);
        sViewsWithIds.put(R.id.tvAgeSubTitle, 54);
        sViewsWithIds.put(R.id.svAgeconfirmation, 55);
    }
    // views
    @Nullable
    private final com.hungama.music.databinding.CommonBlurViewBinding mboundView0;
    @NonNull
    private final android.widget.LinearLayout mboundView1;
    @Nullable
    private final com.hungama.music.databinding.SkeletonGeneralSettingBinding mboundView11;
    @NonNull
    private final android.widget.RelativeLayout mboundView2;
    @Nullable
    private final com.hungama.music.databinding.LayoutProgressBinding mboundView21;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public FrGeneralSettingBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 56, sIncludes, sViewsWithIds));
    }
    private FrGeneralSettingBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.view.View) bindings[14]
            , (android.view.View) bindings[38]
            , (android.view.View) bindings[46]
            , (android.widget.LinearLayout) bindings[37]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[41]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[44]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[30]
            , (android.widget.ImageView) bindings[24]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[48]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[22]
            , (android.widget.RelativeLayout) bindings[15]
            , (android.widget.RelativeLayout) bindings[52]
            , (android.widget.RelativeLayout) bindings[40]
            , (android.widget.RelativeLayout) bindings[11]
            , (android.widget.RelativeLayout) bindings[26]
            , (android.widget.RelativeLayout) bindings[27]
            , (android.widget.RelativeLayout) bindings[39]
            , (android.widget.RelativeLayout) bindings[10]
            , (bindings[4] != null) ? com.hungama.music.databinding.CommonHeaderActionBarBinding.bind((android.view.View) bindings[4]) : null
            , (android.widget.RelativeLayout) bindings[20]
            , (android.widget.RelativeLayout) bindings[19]
            , (android.widget.RelativeLayout) bindings[0]
            , (android.widget.RelativeLayout) bindings[33]
            , (android.widget.RelativeLayout) bindings[47]
            , (androidx.core.widget.NestedScrollView) bindings[8]
            , (com.facebook.shimmer.ShimmerFrameLayout) bindings[7]
            , (android.widget.Switch) bindings[55]
            , (android.widget.Switch) bindings[45]
            , (android.widget.Switch) bindings[13]
            , (android.widget.Switch) bindings[31]
            , (android.widget.Switch) bindings[17]
            , (android.widget.Switch) bindings[36]
            , (android.widget.Switch) bindings[50]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[54]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[53]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[42]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[43]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[12]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[28]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[29]
            , (android.widget.TextView) bindings[51]
            , (android.widget.TextView) bindings[25]
            , (android.widget.TextView) bindings[18]
            , (android.widget.TextView) bindings[9]
            , (android.widget.TextView) bindings[32]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[21]
            , (android.widget.TextView) bindings[23]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[16]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[34]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[35]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[49]
            );
        this.mboundView0 = (bindings[3] != null) ? com.hungama.music.databinding.CommonBlurViewBinding.bind((android.view.View) bindings[3]) : null;
        this.mboundView1 = (android.widget.LinearLayout) bindings[1];
        this.mboundView1.setTag(null);
        this.mboundView11 = (bindings[5] != null) ? com.hungama.music.databinding.SkeletonGeneralSettingBinding.bind((android.view.View) bindings[5]) : null;
        this.mboundView2 = (android.widget.RelativeLayout) bindings[2];
        this.mboundView2.setTag(null);
        this.mboundView21 = (com.hungama.music.databinding.LayoutProgressBinding) bindings[6];
        setContainedBinding(this.mboundView21);
        this.rlMain.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        mboundView21.invalidateAll();
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        if (mboundView21.hasPendingBindings()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    public void setLifecycleOwner(@Nullable androidx.lifecycle.LifecycleOwner lifecycleOwner) {
        super.setLifecycleOwner(lifecycleOwner);
        mboundView21.setLifecycleOwner(lifecycleOwner);
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
        executeBindingsOn(mboundView21);
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}