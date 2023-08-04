package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class RowDownloadDetailBindingImpl extends RowDownloadDetailBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.vNowPlaying, 1);
        sViewsWithIds.put(R.id.llMain, 2);
        sViewsWithIds.put(R.id.rlImageViewParent, 3);
        sViewsWithIds.put(R.id.rlImageView, 4);
        sViewsWithIds.put(R.id.ivUserImage, 5);
        sViewsWithIds.put(R.id.ivEqualizer, 6);
        sViewsWithIds.put(R.id.ivEqualizerAnim, 7);
        sViewsWithIds.put(R.id.rlGoldStrip, 8);
        sViewsWithIds.put(R.id.ivGoldStrips, 9);
        sViewsWithIds.put(R.id.tvGoldLabel, 10);
        sViewsWithIds.put(R.id.tvTitle, 11);
        sViewsWithIds.put(R.id.ivE, 12);
        sViewsWithIds.put(R.id.tvSubTitle, 13);
        sViewsWithIds.put(R.id.ivMore, 14);
        sViewsWithIds.put(R.id.ivDownload, 15);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public RowDownloadDetailBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 16, sIncludes, sViewsWithIds));
    }
    private RowDownloadDetailBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[15]
            , (android.widget.ImageView) bindings[12]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[6]
            , (com.airbnb.lottie.LottieAnimationView) bindings[7]
            , (android.widget.ImageView) bindings[9]
            , (android.widget.ImageView) bindings[14]
            , (com.google.android.material.imageview.ShapeableImageView) bindings[5]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[2]
            , (android.widget.RelativeLayout) bindings[8]
            , (android.widget.RelativeLayout) bindings[4]
            , (android.widget.RelativeLayout) bindings[3]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[10]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[13]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[11]
            , (android.view.View) bindings[1]
            );
        this.mboundView0 = (androidx.constraintlayout.widget.ConstraintLayout) bindings[0];
        this.mboundView0.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
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