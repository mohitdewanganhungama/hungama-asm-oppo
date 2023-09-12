package com.hungama.music.data.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\"\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH&J\"\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH&\u00a8\u0006\u000b"}, d2 = {"Lcom/hungama/music/data/model/BannerItemClick;", "", "bannerItemClick", "", "isClicked", "", "pos", "", "bodyData", "Lcom/hungama/music/data/model/BodyRowsItemsItem;", "onCheckSatusplaylist", "mylibrary_debug"})
public abstract interface BannerItemClick {
    
    public abstract void bannerItemClick(boolean isClicked, int pos, @org.jetbrains.annotations.Nullable
    com.hungama.music.data.model.BodyRowsItemsItem bodyData);
    
    public abstract void onCheckSatusplaylist(boolean isClicked, int pos, @org.jetbrains.annotations.Nullable
    com.hungama.music.data.model.BodyRowsItemsItem bodyData);
}