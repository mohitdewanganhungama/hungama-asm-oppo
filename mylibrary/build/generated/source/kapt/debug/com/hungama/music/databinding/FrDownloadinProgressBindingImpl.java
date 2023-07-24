package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class FrDownloadinProgressBindingImpl extends FrDownloadinProgressBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = new androidx.databinding.ViewDataBinding.IncludedLayouts(13);
        sIncludes.setIncludes(0, 
            new String[] {"layout_progress"},
            new int[] {2},
            new int[] {com.hungama.music.R.layout.layout_progress});
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.rlHeader, 1);
        sViewsWithIds.put(R.id.llDownload, 3);
        sViewsWithIds.put(R.id.llPlayAll, 4);
        sViewsWithIds.put(R.id.pauseAll, 5);
        sViewsWithIds.put(R.id.cancel, 6);
        sViewsWithIds.put(R.id.rvPlaylist, 7);
        sViewsWithIds.put(R.id.clExplore, 8);
        sViewsWithIds.put(R.id.ivMusicNoContent, 9);
        sViewsWithIds.put(R.id.tvnoContentTitle, 10);
        sViewsWithIds.put(R.id.tvNoContentBody, 11);
        sViewsWithIds.put(R.id.btnExplore, 12);
    }
    // views
    @Nullable
    private final com.hungama.music.databinding.LayoutProgressBinding mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public FrDownloadinProgressBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 13, sIncludes, sViewsWithIds));
    }
    private FrDownloadinProgressBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[12]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[6]
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[8]
            , (android.widget.ImageView) bindings[9]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[3]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[4]
            , (android.widget.TextView) bindings[5]
            , (bindings[1] != null) ? com.hungama.music.databinding.CommonHeaderActionBarBinding.bind((android.view.View) bindings[1]) : null
            , (android.widget.RelativeLayout) bindings[0]
            , (androidx.recyclerview.widget.RecyclerView) bindings[7]
            , (android.widget.TextView) bindings[11]
            , (android.widget.TextView) bindings[10]
            );
        this.mboundView0 = (com.hungama.music.databinding.LayoutProgressBinding) bindings[2];
        setContainedBinding(this.mboundView0);
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