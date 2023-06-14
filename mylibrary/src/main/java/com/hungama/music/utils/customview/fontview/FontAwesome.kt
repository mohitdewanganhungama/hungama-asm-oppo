package com.hungama.music.utils.customview.fontview

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.Log
import com.hungama.music.utils.CommonUtils.setLog
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Hashtable

object FontAwesome {

    /* Root Directory. src/main/assets/fonts/ */
    private val ROOT = "fonts/"

    private val FILE_NAME = "font_awesome.ttf"

    /* FontAwesome font file name */
    private val FONT_AWESOME = ROOT + FILE_NAME

    /* Cache Font asset to avoid extra memory head */
    private val cache = Hashtable<String, Typeface>()


    /**
     * @param context context passed
     * *
     * @return returns TypeFace object from cache
     */
    /* Returns FontAwesome TypeFace */
    fun getTypeface(context: Context): Typeface? {
        synchronized(cache) {
            if (!cache.containsKey(FONT_AWESOME)) {
                cache.put(FONT_AWESOME, processFontFromRaw(context))
            } else {
                setLog(FontAwesome::class.java.simpleName, "Loading typeface from cache")
            }
            return cache[FONT_AWESOME]
        }
    }

    private fun processFontFromRaw(context: Context): Typeface? {
        var typeface: Typeface? = null

        //typeface = FontCache.get(context, "icomoon.ttf")
        var inputStream: InputStream? = null

        val outPath = context.cacheDir.toString() + "/tmp" + System.currentTimeMillis() + ".raw"

        try {
            //inputStream = context.resources.openRawResource(R.raw.font_awesome)
            inputStream = context.resources.assets.open("icomoon.ttf")
        } catch (ignored: Resources.NotFoundException) {
        }

        try {
            assert(inputStream != null)
            val buffer = ByteArray(inputStream!!.available())
            val stream = BufferedOutputStream(FileOutputStream(outPath))
            var num: Int
            num = inputStream.read(buffer)
            while (num > 0) {
                stream.write(buffer, 0, num)
                num = inputStream.read(buffer)
            }
            stream.close()
            typeface = Typeface.createFromFile(outPath)
            if (File(outPath).delete()) {
                setLog(FontAwesome::class.java.simpleName, "Deleted " + outPath)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return typeface
    }
}