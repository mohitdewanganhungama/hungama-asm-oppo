package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class RowItype50BindingImpl extends RowItype50Binding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.episode_player_view, 1);
        sViewsWithIds.put(R.id.vTopBottom, 2);
        sViewsWithIds.put(R.id.preViewProgressBar, 3);
        sViewsWithIds.put(R.id.llMute, 4);
        sViewsWithIds.put(R.id.ivMuteUnmute, 5);
        sViewsWithIds.put(R.id.ivUserImage, 6);
        sViewsWithIds.put(R.id.llBottom, 7);
        sViewsWithIds.put(R.id.tvTitle, 8);
        sViewsWithIds.put(R.id.tvSubTitle, 9);
        sViewsWithIds.put(R.id.llRent, 10);
        sViewsWithIds.put(R.id.ivRent, 11);
        sViewsWithIds.put(R.id.txtRent, 12);
        sViewsWithIds.put(R.id.rlAction, 13);
        sViewsWithIds.put(R.id.ivAction, 14);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public RowItype50BindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 15, sIncludes, sViewsWithIds));
    }
    private RowItype50BindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (androidx.media3.ui.PlayerView) bindings[1]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[14]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[5]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[11]
            , (com.google.android.material.imageview.ShapeableImageView) bindings[6]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[7]
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[0]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[4]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[10]
            , (android.widget.ProgressBar) bindings[3]
            , (android.widget.RelativeLayout) bindings[13]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[9]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[8]
            , (android.widget.TextView) bindings[12]
            , (android.view.View) bindings[2]
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