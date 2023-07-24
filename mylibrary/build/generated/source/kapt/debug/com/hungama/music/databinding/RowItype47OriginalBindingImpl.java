package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class RowItype47OriginalBindingImpl extends RowItype47OriginalBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.clMainView, 1);
        sViewsWithIds.put(R.id.ivUserImage, 2);
        sViewsWithIds.put(R.id.episode_player_view, 3);
        sViewsWithIds.put(R.id.preViewProgressBar, 4);
        sViewsWithIds.put(R.id.llMute, 5);
        sViewsWithIds.put(R.id.ivMuteUnmute, 6);
        sViewsWithIds.put(R.id.llHeaderTitle, 7);
        sViewsWithIds.put(R.id.tvTitle, 8);
        sViewsWithIds.put(R.id.ivMore, 9);
        sViewsWithIds.put(R.id.llHeaderSubTitle, 10);
        sViewsWithIds.put(R.id.tvSubTitle, 11);
        sViewsWithIds.put(R.id.tvEpisodesHeader, 12);
        sViewsWithIds.put(R.id.rvOriginalEpisodes, 13);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public RowItype47OriginalBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 14, sIncludes, sViewsWithIds));
    }
    private RowItype47OriginalBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[0]
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[1]
            , (androidx.media3.ui.PlayerView) bindings[3]
            , (android.widget.ImageView) bindings[9]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[6]
            , (com.google.android.material.imageview.ShapeableImageView) bindings[2]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[10]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[7]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[5]
            , (android.widget.ProgressBar) bindings[4]
            , (androidx.recyclerview.widget.RecyclerView) bindings[13]
            , (android.widget.TextView) bindings[12]
            , (com.hungama.music.utils.customview.ShowMoreTextView) bindings[11]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[8]
            );
        this.clMain.setTag(null);
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