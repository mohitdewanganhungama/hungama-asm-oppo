package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivitySplashBindingImpl extends ActivitySplashBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = new androidx.databinding.ViewDataBinding.IncludedLayouts(18);
        sIncludes.setIncludes(1, 
            new String[] {"layout_progress"},
            new int[] {2},
            new int[] {com.hungama.music.R.layout.layout_progress});
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.music_icon, 3);
        sViewsWithIds.put(R.id.bgSplash, 4);
        sViewsWithIds.put(R.id.imgSplash, 5);
        sViewsWithIds.put(R.id.tvVersion, 6);
        sViewsWithIds.put(R.id.nativeTemplateView, 7);
        sViewsWithIds.put(R.id.ivCloseAd, 8);
        sViewsWithIds.put(R.id.clProgressView, 9);
        sViewsWithIds.put(R.id.tvWillBack, 10);
        sViewsWithIds.put(R.id.tvProgress, 11);
        sViewsWithIds.put(R.id.pb, 12);
        sViewsWithIds.put(R.id.loadNativeBtn, 13);
        sViewsWithIds.put(R.id.showNativeBtn, 14);
        sViewsWithIds.put(R.id.hideNativeBtn, 15);
        sViewsWithIds.put(R.id.clStoryV2, 16);
        sViewsWithIds.put(R.id.tvStoryV2, 17);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivitySplashBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 18, sIncludes, sViewsWithIds));
    }
    private ActivitySplashBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 1
            , (androidx.appcompat.widget.AppCompatImageView) bindings[4]
            , (android.widget.RelativeLayout) bindings[9]
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[16]
            , (android.widget.Button) bindings[15]
            , (androidx.appcompat.widget.AppCompatImageView) bindings[5]
            , (android.widget.ImageView) bindings[8]
            , (android.widget.Button) bindings[13]
            , (android.widget.RelativeLayout) bindings[1]
            , (androidx.appcompat.widget.AppCompatImageView) bindings[3]
            , (com.google.android.ads.nativetemplates.TemplateView) bindings[7]
            , (android.widget.ProgressBar) bindings[12]
            , (com.hungama.music.databinding.LayoutProgressBinding) bindings[2]
            , (android.widget.Button) bindings[14]
            , (android.widget.TextView) bindings[11]
            , (android.widget.TextView) bindings[17]
            , (android.widget.TextView) bindings[6]
            , (android.widget.TextView) bindings[10]
            );
        this.mainContent.setTag(null);
        this.mboundView0 = (androidx.constraintlayout.widget.ConstraintLayout) bindings[0];
        this.mboundView0.setTag(null);
        setContainedBinding(this.progress);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x2L;
        }
        progress.invalidateAll();
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        if (progress.hasPendingBindings()) {
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
        progress.setLifecycleOwner(lifecycleOwner);
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
            case 0 :
                return onChangeProgress((com.hungama.music.databinding.LayoutProgressBinding) object, fieldId);
        }
        return false;
    }
    private boolean onChangeProgress(com.hungama.music.databinding.LayoutProgressBinding Progress, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x1L;
            }
            return true;
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
        executeBindingsOn(progress);
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): progress
        flag 1 (0x2L): null
    flag mapping end*/
    //end
}