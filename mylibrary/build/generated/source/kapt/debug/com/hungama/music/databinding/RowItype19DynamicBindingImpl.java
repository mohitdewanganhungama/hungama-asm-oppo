package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class RowItype19DynamicBindingImpl extends RowItype19DynamicBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.llTop, 3);
        sViewsWithIds.put(R.id.ivHeader, 4);
        sViewsWithIds.put(R.id.tvHeaderSubTitle, 5);
        sViewsWithIds.put(R.id.tvHeaderTitle, 6);
        sViewsWithIds.put(R.id.ivUserImage, 7);
        sViewsWithIds.put(R.id.tvTitle, 8);
        sViewsWithIds.put(R.id.tvSubTitle, 9);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView1;
    @Nullable
    private final com.hungama.music.databinding.BlurLayoutViewBinding mboundView11;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public RowItype19DynamicBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 10, sIncludes, sViewsWithIds));
    }
    private RowItype19DynamicBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (com.hungama.music.utils.customview.circleimageview.CircleImageView) bindings[4]
            , (com.google.android.material.imageview.ShapeableImageView) bindings[7]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[0]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[3]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[5]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[6]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[9]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[8]
            );
        this.llMain.setTag(null);
        this.mboundView1 = (android.widget.RelativeLayout) bindings[1];
        this.mboundView1.setTag(null);
        this.mboundView11 = (bindings[2] != null) ? com.hungama.music.databinding.BlurLayoutViewBinding.bind((android.view.View) bindings[2]) : null;
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