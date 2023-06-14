/*
 * Designed and developed by 2019 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hungama.music.utils.customview.customspinnerview

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat

/** A definition of the out-ranged color. */
internal const val outRangeColor = 65555

/** dp size to px size. */
internal fun Context.dp2Px(dp: Int): Int {
  val scale = resources.displayMetrics.density
  return (dp * scale).toInt()
}

/** dp size to px size. */
internal fun View.dp2Px(dp: Int): Int {
  return context.dp2Px(dp)
}

/** dp size to px size. */
internal fun Context.dp2Px(dp: Float): Int {
  val scale = resources.displayMetrics.density
  return (dp * scale).toInt()
}

/** dp size to px size. */
internal fun View.dp2Px(dp: Float): Int {
  return context.dp2Px(dp)
}

/** gets a drawable from the resource. */
internal fun Context.contextDrawable(resource: Int): Drawable? {
  return ContextCompat.getDrawable(this, resource)
}
