package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class FrBlankBindingImpl extends FrBlankBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.header, 3);
        sViewsWithIds.put(R.id.tvCommingSoon, 7);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView0;
    @Nullable
    private final com.hungama.music.databinding.ShimmerLayoutHeaderBinding mboundView1;
    @NonNull
    private final android.widget.LinearLayout mboundView2;
    @Nullable
    private final com.hungama.music.databinding.ShimmerLayoutHeaderBinding mboundView21;
    @Nullable
    private final com.hungama.music.databinding.ShimmerLayoutBinding mboundView22;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public FrBlankBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 8, sIncludes, sViewsWithIds));
    }
    private FrBlankBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (com.facebook.shimmer.ShimmerFrameLayout) bindings[1]
            , (bindings[3] != null) ? com.hungama.music.databinding.HeaderMainBinding.bind((android.view.View) bindings[3]) : null
            , (android.widget.TextView) bindings[7]
            );
        this.comingSoonShimmerLayout.setTag(null);
        this.mboundView0 = (android.widget.RelativeLayout) bindings[0];
        this.mboundView0.setTag(null);
        this.mboundView1 = (bindings[4] != null) ? com.hungama.music.databinding.ShimmerLayoutHeaderBinding.bind((android.view.View) bindings[4]) : null;
        this.mboundView2 = (android.widget.LinearLayout) bindings[2];
        this.mboundView2.setTag(null);
        this.mboundView21 = (bindings[5] != null) ? com.hungama.music.databinding.ShimmerLayoutHeaderBinding.bind((android.view.View) bindings[5]) : null;
        this.mboundView22 = (bindings[6] != null) ? com.hungama.music.databinding.ShimmerLayoutBinding.bind((android.view.View) bindings[6]) : null;
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