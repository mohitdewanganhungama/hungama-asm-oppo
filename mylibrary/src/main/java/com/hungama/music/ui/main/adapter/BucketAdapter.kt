package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.databinding.RowBucketBinding
import com.hungama.music.data.model.RowsItem

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class BucketAdapter(
    val context: Context,
    var arrayList: List<RowsItem?>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<BucketAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: MutableList<RowsItem?>) {
        arrayList = list
        arrayList.forEach {
            if (it?.itype == null && it?.items?.size!! > 0) {
                it.itype = it.items?.get(0)?.itype
            }
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowBucketBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_bucket,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowBucketBinding) : RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {

        if (arrayList.get(holder.adapterPosition) != null) {
            val bucketsItem = arrayList.get(holder.adapterPosition)
            if (bucketsItem?.heading != null) {
                holder.binding.tvTitle.text = bucketsItem.heading
                holder.binding.tvTitle.visibility = View.VISIBLE
            } else {
                holder.binding.tvTitle.visibility = View.GONE
            }

            if (bucketsItem?.more != null && bucketsItem.more == 1) {
                holder.binding.ivMore.visibility = View.VISIBLE
            } else {
                holder.binding.ivMore.visibility = View.GONE
            }

            if (bucketsItem?.subhead != null) {
                holder.binding.tvSubTitle.text = bucketsItem.subhead
                holder.binding.tvSubTitle.visibility = View.VISIBLE
            } else {
                holder.binding.tvSubTitle.visibility = View.GONE
            }

            if (bucketsItem?.items != null && bucketsItem.items?.size!! > 0) {
                holder.binding.rvBucketItem.visibility = View.VISIBLE


                if (bucketsItem.itype == 1) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }

                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val storyAdapter = iType1Adapter(
                        context,
                        bucketsItem.items,
                        object : iType1Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = storyAdapter

                    storyAdapter.addData(bucketsItem.items)
                } else if (bucketsItem.itype == 2) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val radioAdapter = iType2Adapter(
                        context, layoutManager.spanCount,
                        bucketsItem.items!!,
                        object : iType2Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = radioAdapter

                    radioAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 3) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val twoLayerSeriesAdapter = iType3Adapter(
                        context,
                        bucketsItem.items!!,
                        object : iType3Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = twoLayerSeriesAdapter

                    twoLayerSeriesAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 4) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val twoLayerSeriesAdapter = iType4Adapter(
                        context,
                        bucketsItem.items!!,
                        object : iType4Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = twoLayerSeriesAdapter

                    twoLayerSeriesAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 5) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val shortFilmAdapter = iType5Adapter(
                        context,
                        bucketsItem.items,
                        object : iType5Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = shortFilmAdapter

                    shortFilmAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 6) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val radioAdapter = iType6Adapter(
                        context,
                        bucketsItem.items!!,
                        object : iType6Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = radioAdapter

                    radioAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 7) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val twoLayerSeriesAdapter = iType7Adapter(
                        context,
                        bucketsItem.items!!,
                        object : iType7Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = twoLayerSeriesAdapter

                    twoLayerSeriesAdapter.addData(bucketsItem.items)

                } else if (bucketsItem.itype == 8) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val recentHistoryAdapter = iType8Adapter(
                        context,
                        bucketsItem.items,
                        object : iType8Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = recentHistoryAdapter

                    recentHistoryAdapter.addData(bucketsItem.items)
                } else if (bucketsItem.itype == 9) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val radioAdapter = iType9Adapter(
                        context, layoutManager.spanCount,
                        bucketsItem.items!!,
                        object : iType9Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = radioAdapter

                    radioAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 10) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val iType9Adapter = iType10Adapter(
                        context,
                        bucketsItem.items,
                        object : iType10Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }

                            }

                        })

                    holder.binding.rvBucketItem.adapter = iType9Adapter

                    iType9Adapter.addData(bucketsItem.items)
                } else if (bucketsItem.itype == 11) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val twoLayerSeriesAdapter = iType11Adapter(
                        context,
                        bucketsItem.items!!,
                        object : iType11Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = twoLayerSeriesAdapter

                    twoLayerSeriesAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 12) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val twoLayerSeriesAdapter = iType12Adapter(
                        context,
                        bucketsItem.items!!,
                        object : iType12Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = twoLayerSeriesAdapter

                    twoLayerSeriesAdapter.addData(bucketsItem.items)


                } else if (bucketsItem.itype == 13) {
                    var layoutManager: GridLayoutManager? = null
                    if (bucketsItem.numrow != null) {
                        layoutManager = GridLayoutManager(
                            context,
                            bucketsItem.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }
                    holder.binding.rvBucketItem.layoutManager = layoutManager

                    val twoLayerSeriesAdapter = iType13Adapter(
                        context,
                        bucketsItem.items,
                        object : iType13Adapter.OnItemClick {
                            override fun onUserClick(position: Int) {
                                if (onitemclick != null) {
                                    onitemclick.onUserClick(holder.adapterPosition, position)
                                }
                            }

                        })

                    holder.binding.rvBucketItem.adapter = twoLayerSeriesAdapter

                    twoLayerSeriesAdapter.addData(bucketsItem.items)


                } else {
                    holder.binding.rvBucketItem.visibility = View.GONE
                }


            } else {
                holder.binding.rvBucketItem.visibility = View.GONE
            }

        }
    }//onBind

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_1

    }

    interface OnItemClick {
        fun onUserClick(parentPosition: Int, childPosition: Int)
    }

//    fun updateList(data: ArrayList<BodyDataItemsItem>?, parentPosition: Int) {
//
//        for (stories in data!!.indices){
//            this.arrayList.get(parentPosition)!!.items!!.get(stories)!!.data = data[stories]
//        }
//        notifyDataSetChanged()
//    }
}