package com.hungama.music.databinding;
import com.hungama.music.R;
import com.hungama.music.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActLoginMainBindingImpl extends ActLoginMainBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.constraintLayout3, 1);
        sViewsWithIds.put(R.id.viewpager, 2);
        sViewsWithIds.put(R.id.imageView2, 3);
        sViewsWithIds.put(R.id.textView2, 4);
        sViewsWithIds.put(R.id.textView3, 5);
        sViewsWithIds.put(R.id.ivImageThreeDots, 6);
        sViewsWithIds.put(R.id.clLanguage, 7);
        sViewsWithIds.put(R.id.ll_language, 8);
        sViewsWithIds.put(R.id.ll_Skip, 9);
        sViewsWithIds.put(R.id.txtSkip, 10);
        sViewsWithIds.put(R.id.ll_LoginMobile, 11);
        sViewsWithIds.put(R.id.ivDetailBtnIcon, 12);
        sViewsWithIds.put(R.id.txtLoginMobile, 13);
        sViewsWithIds.put(R.id.llOrSocialLogin, 14);
        sViewsWithIds.put(R.id.llAllLogins2, 15);
        sViewsWithIds.put(R.id.rvLoginPlateformSequence, 16);
        sViewsWithIds.put(R.id.llAllLogins, 17);
        sViewsWithIds.put(R.id.llEmail, 18);
        sViewsWithIds.put(R.id.llGoogle, 19);
        sViewsWithIds.put(R.id.llFacebook, 20);
        sViewsWithIds.put(R.id.llAppleLogin, 21);
        sViewsWithIds.put(R.id.llWhatsApp, 22);
        sViewsWithIds.put(R.id.tvTermCondtion, 23);
        sViewsWithIds.put(R.id.tvPrivacyPolicy, 24);
        sViewsWithIds.put(R.id.tvTermsOfServices, 25);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActLoginMainBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 26, sIncludes, sViewsWithIds));
    }
    private ActLoginMainBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[7]
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[1]
            , (android.widget.ImageView) bindings[3]
            , (com.hungama.music.utils.customview.fontview.FontAwesomeImageView) bindings[12]
            , (android.widget.LinearLayout) bindings[6]
            , (android.widget.LinearLayout) bindings[17]
            , (android.widget.LinearLayout) bindings[15]
            , (android.widget.LinearLayout) bindings[21]
            , (android.widget.LinearLayout) bindings[18]
            , (android.widget.LinearLayout) bindings[20]
            , (android.widget.LinearLayout) bindings[19]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[8]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[11]
            , (android.widget.LinearLayout) bindings[14]
            , (androidx.appcompat.widget.LinearLayoutCompat) bindings[9]
            , (android.widget.LinearLayout) bindings[22]
            , (androidx.recyclerview.widget.RecyclerView) bindings[16]
            , (android.widget.TextView) bindings[4]
            , (android.widget.TextView) bindings[5]
            , (android.widget.TextView) bindings[24]
            , (android.widget.TextView) bindings[23]
            , (android.widget.TextView) bindings[25]
            , (android.widget.TextView) bindings[13]
            , (android.widget.TextView) bindings[10]
            , (androidx.viewpager2.widget.ViewPager2) bindings[2]
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