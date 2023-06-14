package com.hungama.music.ui.main.view.activity

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants.*
import android.view.View
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.activity_haptic_feedback.*

class HapticFeedback : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haptic_feedback)
        normalVibrationButton.setOnClickListener(this)
        clickVibrationButton.setOnClickListener(this)
        doubleClickVibrationButton.setOnClickListener(this)
        tickVibrationButton.setOnClickListener(this)
        heavyClickVibrationButton.setOnClickListener(this)
        btnGestureStart.setOnClickListener(this)
        btnKeyBoardPress.setOnClickListener(this)
        btnKeyBoardRelease.setOnClickListener(this)
        btnKeyboardTab.setOnClickListener(this)
        btnLongPress.setOnClickListener(this)
        btnReject.setOnClickListener(this)
        btnTextHangleMove.setOnClickListener(this)
        btnVirtualKey.setOnClickListener(this)
        btnVirtualKeyRelease.setOnClickListener(this)
    }

    @SuppressLint("NewApi")
    override fun onClick(v: View?) {
        val  vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (v == normalVibrationButton){
            val vibrationEffect1: VibrationEffect
            // this is the only type of the vibration which requires system version Oreo (API 26)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // this effect creates the vibration of default amplitude for 1000ms(1 sec)
                vibrationEffect1 = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                // it is safe to cancel other vibrations currently taking place
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect1)
            }else{
                setLog("HapticFeedback", "Demo-1:--Below API-26")
            }
        }else if (v == clickVibrationButton){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, CLOCK_TICK)
//                v?.performHapticFeedback(CLOCK_TICK,2)
            }else{
                setLog("HapticFeedback", "Demo-2:--Below API-29")
            }
        }else if (v == doubleClickVibrationButton){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, CONFIRM)
//                v?.performHapticFeedback(REJECT,2)
            }else{
                setLog("HapticFeedback", "Demo-3:--Below API-29")
            }
        }else if (v == tickVibrationButton){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, CONTEXT_CLICK)
//                v?.performHapticFeedback(CONTEXT_CLICK,2)
            }else{
                setLog("HapticFeedback", "Demo-4:--Below API-29")
            }
        }else if (v == heavyClickVibrationButton){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, GESTURE_END)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v == btnGestureStart){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, GESTURE_START)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v == btnKeyBoardPress){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, KEYBOARD_PRESS)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v ==btnKeyBoardRelease){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, KEYBOARD_RELEASE)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v ==btnKeyboardTab){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, KEYBOARD_TAP)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v ==btnLongPress){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, LONG_PRESS)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v ==btnReject){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, REJECT)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v ==btnVirtualKey){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, VIRTUAL_KEY)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v ==btnVirtualKeyRelease){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, VIRTUAL_KEY_RELEASE)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }
        else if (v ==btnTextHangleMove){
            val vibrationEffect2: VibrationEffect
            // this type of vibration requires API 29
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                // create vibrator effect with the constant EFFECT_CLICK
                vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                // it is safe to cancel other vibrations currently taking place
                //vibrator.cancel()
                //vibrator.vibrate(vibrationEffect2)
//                v?.isHapticFeedbackEnabled = true
                CommonUtils.hapticVibration(this, v!!, TEXT_HANDLE_MOVE)
//                v?.performHapticFeedback(CONFIRM,2)
            }else{
                setLog("HapticFeedback", "Demo-5:--Below API-29")
            }
        }

    }
}