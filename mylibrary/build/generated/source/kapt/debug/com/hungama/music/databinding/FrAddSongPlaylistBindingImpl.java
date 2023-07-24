package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class FrAddSongPlaylistBindingImpl extends FrAddSongPlaylistBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = new androidx.databinding.ViewDataBinding.IncludedLayouts(15);
        sIncludes.setIncludes(0, 
            new String[] {"layout_progress"},
            new int[] {3},
            new int[] {com.hungama.music.R.layout.layout_progress});
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.headBarBlur, 2);
        sViewsWithIds.put(R.id.rlBack, 4);
        sViewsWithIds.put(R.id.ivBack, 5);
        sViewsWithIds.put(R.id.ivBackIcon, 6);
        sViewsWithIds.put(R.id.tvActionBarHeading, 7);
        sViewsWithIds.put(R.id.rlMenu, 8);
        sViewsWithIds.put(R.id.ivMenu, 9);
        sViewsWithIds.put(R.id.rlSearch, 10);
        sViewsWithIds.put(R.id.llSearch, 11);
        sViewsWithIds.put(R.id.et_Search, 12);
        sViewsWithIds.put(R.id.rvRecomandedSong, 13);
        sViewsWithIds.put(R.id.rvTab, 14);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public FrAddSongPlaylistBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 15, sIncludes, sViewsWithIds));
    }
    private FrAddSongPlaylistBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 1
            , (androidx.appcompat.widget.AppCompatEditText) bindings[12]
            , (bindings[2] != null) ? com.hungama.music.databinding.CommonBlurViewBinding.bind((android.view.View) bindings[2]) : null
            , (android.widget.ImageView) bindings[5]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[6]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[9]
            , (android.widget.LinearLayout) bindings[11]
            , (com.hungama.music.databinding.LayoutProgressBinding) bindings[3]
            , (android.widget.RelativeLayout) bindings[1]
            , (android.widget.RelativeLayout) bindings[4]
            , (android.widget.RelativeLayout) bindings[0]
            , (android.widget.RelativeLayout) bindings[8]
            , (android.widget.RelativeLayout) bindings[10]
            , (androidx.recyclerview.widget.RecyclerView) bindings[13]
            , (androidx.recyclerview.widget.RecyclerView) bindings[14]
            , (android.widget.TextView) bindings[7]
            );
        setContainedBinding(this.progress);
        this.rlActionBarHeader.setTag(null);
        this.rlMain.setTag(null);
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