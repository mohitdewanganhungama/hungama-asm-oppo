package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class RowChartDetailV3BindingImpl extends RowChartDetailV3Binding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.ivUserImage, 1);
        sViewsWithIds.put(R.id.rlFreeStrip, 2);
        sViewsWithIds.put(R.id.ivfreeStrips, 3);
        sViewsWithIds.put(R.id.llDetails, 4);
        sViewsWithIds.put(R.id.tvTitle, 5);
        sViewsWithIds.put(R.id.tvSubTitle, 6);
        sViewsWithIds.put(R.id.ivMore, 7);
        sViewsWithIds.put(R.id.tvSubTitle2, 8);
        sViewsWithIds.put(R.id.ivDownload, 9);
        sViewsWithIds.put(R.id.ivPlay, 10);
        sViewsWithIds.put(R.id.tvTime, 11);
        sViewsWithIds.put(R.id.pbSong, 12);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public RowChartDetailV3BindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 13, sIncludes, sViewsWithIds));
    }
    private RowChartDetailV3BindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[9]
            , (android.widget.ImageView) bindings[7]
            , (android.widget.ImageView) bindings[10]
            , (com.google.android.material.imageview.ShapeableImageView) bindings[1]
            , (android.widget.ImageView) bindings[3]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[4]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[0]
            , (android.widget.ProgressBar) bindings[12]
            , (android.widget.RelativeLayout) bindings[2]
            , (android.widget.TextView) bindings[6]
            , (com.hungama.music.utils.customview.ShowMoreTextView) bindings[8]
            , (android.widget.TextView) bindings[11]
            , (android.widget.TextView) bindings[5]
            );
        this.llMain.setTag(null);
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