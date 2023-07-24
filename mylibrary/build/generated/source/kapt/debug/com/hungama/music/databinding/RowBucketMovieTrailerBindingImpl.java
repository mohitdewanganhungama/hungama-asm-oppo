package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class RowBucketMovieTrailerBindingImpl extends RowBucketMovieTrailerBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = new androidx.databinding.ViewDataBinding.IncludedLayouts(7);
        sIncludes.setIncludes(0, 
            new String[] {"row_itype_5"},
            new int[] {1},
            new int[] {com.hungama.music.R.layout.row_itype_5});
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.llHeaderTitle, 2);
        sViewsWithIds.put(R.id.tvHeading, 3);
        sViewsWithIds.put(R.id.tvSubTitle, 4);
        sViewsWithIds.put(R.id.switchPublic, 5);
        sViewsWithIds.put(R.id.ivMore, 6);
    }
    // views
    @Nullable
    private final com.hungama.music.databinding.RowItype5Binding mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public RowBucketMovieTrailerBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 7, sIncludes, sViewsWithIds));
    }
    private RowBucketMovieTrailerBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[6]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[2]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[0]
            , (androidx.appcompat.widget.SwitchCompat) bindings[5]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[3]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[4]
            );
        this.llMain.setTag(null);
        this.mboundView0 = (com.hungama.music.databinding.RowItype5Binding) bindings[1];
        setContainedBinding(this.mboundView0);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        mboundView0.invalidateAll();
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        if (mboundView0.hasPendingBindings()) {
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
        mboundView0.setLifecycleOwner(lifecycleOwner);
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
        executeBindingsOn(mboundView0);
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