package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class RowFavoritedSongsDetailBindingImpl extends RowFavoritedSongsDetailBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.vNowPlaying, 1);
        sViewsWithIds.put(R.id.llMain, 2);
        sViewsWithIds.put(R.id.rlImageView, 3);
        sViewsWithIds.put(R.id.ivUserImage, 4);
        sViewsWithIds.put(R.id.ivEqualizer, 5);
        sViewsWithIds.put(R.id.ivEqualizerAnim, 6);
        sViewsWithIds.put(R.id.tvTitle, 7);
        sViewsWithIds.put(R.id.ivE, 8);
        sViewsWithIds.put(R.id.tvSubTitle, 9);
        sViewsWithIds.put(R.id.ivMore, 10);
        sViewsWithIds.put(R.id.ivDownload, 11);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public RowFavoritedSongsDetailBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 12, sIncludes, sViewsWithIds));
    }
    private RowFavoritedSongsDetailBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[11]
            , (android.widget.ImageView) bindings[8]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[5]
            , (com.airbnb.lottie.LottieAnimationView) bindings[6]
            , (android.widget.ImageView) bindings[10]
            , (com.google.android.material.imageview.ShapeableImageView) bindings[4]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[2]
            , (android.widget.RelativeLayout) bindings[3]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[9]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[7]
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