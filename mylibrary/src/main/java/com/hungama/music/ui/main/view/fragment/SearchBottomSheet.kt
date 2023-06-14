package com.hungama.music.ui.main.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.VoiceTabEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.activity.TAG
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.PermissionCallbacks
import com.hungama.music.utils.PermissionUtils
import com.hungama.music.utils.customview.RippleBackground
import com.hungama.music.utils.hide
import com.hungama.music.utils.show
import com.hungama.music.R
import kotlinx.android.synthetic.main.fragment_search_bottom_sheet.*
import java.util.*

const val KEY_RECORD_PERMISSION_ALREADY_ASKED = "recordPermissionAlreadyAsked"
const val RECORD_AUDIO_REQ_CODE: Int = 101
class SearchBottomSheet : BottomSheetDialogFragment(), PermissionCallbacks {
    val RecordAudioRequestCode = 1
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var rippleBackGround: RippleBackground

    private var data :ArrayList<String>?=null

    private lateinit var permissionUtils: PermissionUtils
    private var bAlreadyAskedForRecordPermission: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rippleBackGround = view.findViewById(R.id.content)
        permissionUtils = PermissionUtils()
        if (savedInstanceState != null) {
            bAlreadyAskedForRecordPermission =
                savedInstanceState.getBoolean(KEY_RECORD_PERMISSION_ALREADY_ASKED, false)
        }

        if (!bAlreadyAskedForRecordPermission) {

            bAlreadyAskedForRecordPermission = permissionUtils.requestPermissionsWithRationale(
                this,
                Manifest.permission.RECORD_AUDIO,
                RECORD_AUDIO_REQ_CODE,
                this
            )
        }

        /*if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }*/
    }

    private fun setUpView(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(bundle: Bundle) {
                setLog(TAG, "onRmsChanged onReadyForSpeech bundle:${bundle}")


            }

            @SuppressLint("SetTextI18n")
            override fun onBeginningOfSpeech() {
                setLog(TAG, "onRmsChanged onBeginningOfSpeech: ")
//                    editText.setText("")
//                    editText.setHint("Listening...")
            }

            override fun onRmsChanged(v: Float) {
                setLog(TAG, "onRmsChanged:v  " + v)
            }

            override fun onBufferReceived(bytes: ByteArray) {
                setLog(TAG, "onRmsChanged: bytes " + bytes)
            }

            override fun onEndOfSpeech() {
//                try {
//                    if (isAdded) {
//                        setLog(TAG, "onRmsChanged: onEndOfSpeech size::${data?.size} data:${data} isEmpty:${data?.isNullOrEmpty()}")
//                        if (data?.isNullOrEmpty()!!){
//                            tvSearchTitle.text = getString(R.string.search_title)
//                            tvSearchSubTitle.text = getString(R.string.search_subtitle)
//                        }
//                        rippleBackGround.stopRippleAnimation()
//                    }
//                }catch (e:Exception){
//                    e.printStackTrace()
//                }


            } //stop animation

            override fun onError(i: Int) {
                try {
                    if(isAdded){
                        setLog(TAG, "onRmsChanged: onError"+i)
                        if(i==7){
                            tvSearchTitle.text = getString(R.string.search_title)
                            tvSearchSubTitle.text = getString(R.string.search_subtitle)
                            llThreeDot.visibility = View.VISIBLE
                            rippleBackGround.stopRippleAnimation()
                        }
                    }
                }catch (exp:Exception){
                    exp.printStackTrace()
                }

            }

            override fun onResults(bundle: Bundle) {
                try {
                    if (isAdded){
                        data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!!
                        setLog(TAG, "onRmsChanged: data" + data)
                        if (!data.isNullOrEmpty() && data?.size!! > 0 && !TextUtils.isEmpty(""+data?.get(0))){
                            ItemClick?.SearchView(data)
                            speechRecognizer?.stopListening()
                            rippleBackGround.stopRippleAnimation()
                            data?.let {
                                val recognizedText = it[0]
                                tvSearchResult?.text = recognizedText
                                tvSearchResult?.show()
                            }

                            image?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_mic_tick))

                            val hashMap = HashMap<String, String>()
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
                            //https://hungama.atlassian.net/browse/HU-5698
                            //As per jira we removed this property
                            //hashMap.put(EventConstant.KEYWORD_DERIVED_EPROPERTY,data?.get(0)!!)
                            EventManager.getInstance().sendEvent(VoiceTabEvent(hashMap))
                            Handler().postDelayed({
                                dismiss()
                            }, 1000)
                        }else{
                            tvSearchTitle.text = getString(R.string.search_title)
                            tvSearchSubTitle.text = getString(R.string.search_subtitle)
                            llThreeDot.visibility = View.VISIBLE
                            rippleBackGround.stopRippleAnimation()
                            tvSearchResult?.hide()
                        }

                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }



            }

            override fun onPartialResults(bundle: Bundle) {
                setLog(TAG, "onRmsChanged  onPartialResults bundle:${bundle}")
            }

            override fun onEvent(i: Int, bundle: Bundle) {
                setLog(TAG, "onRmsChanged  onEvent bundle:${bundle}")

            }
        })
        image.setOnClickListener {
            tvSearchTitle.text = getString(R.string.lbl_listening)
            tvSearchSubTitle.text = getString(R.string.lbl_plz_go_ahed)
            speechRecognizer?.startListening(speechRecognizerIntent)
            rippleBackGround?.startRippleAnimation()
        }
        rippleBackGround.setOnClickListener {
            rippleBackGround.stopRippleAnimation()
            speechRecognizer?.stopListening()
        }

        image?.performClick()
    }

    var ItemClick: clickItem? = null
    fun setItem(listener: clickItem) {
        this.ItemClick = listener
    }

    interface clickItem {
        fun SearchView(data: ArrayList<String>?)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RecordAudioRequestCode
            )
        }
    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RecordAudioRequestCode && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    requireContext(),
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                Toast.makeText(
                    requireContext(),
                    "Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }
    }*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog?.behavior?.isDraggable = true
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onShowPermissionRationale(permission: String, requestCode: Int) {

        /*Snackbar.make(rootLayout, R.string.permissions_rationale, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.discover_str_13) {
                PermissionUtils().requestPermission(this, permission, requestCode)
            }
            .show()*/
        val alertDialog = AlertDialog.Builder(requireContext())

        alertDialog.apply {
            //setIcon(R.drawable.hungama_text_icon)
            //setTitle("Hello")
            setMessage(R.string.record_audio_permission)
            setPositiveButton(getString(R.string.library_all_str_6)) { _, _ ->
                callPermission(permission, requestCode)
            }
            setNegativeButton(getString(R.string.download_str_3)) { _, _ ->
                dismiss()
            }
            /*setNeutralButton("Neutral") { _, _ ->
                toast("clicked neutral button")
            }*/
        }.create().show()
        //Toast.makeText(requireContext(),R.string.permissions_rationale, Toast.LENGTH_LONG).show()
    }

    private fun callPermission(permission: String, requestCode: Int) {
        PermissionUtils().requestPermission(this, permission, requestCode)
    }

    override fun onPermissionGranted(permission: String) {
        setUpView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_AUDIO_REQ_CODE -> {

                bAlreadyAskedForRecordPermission = false

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpView()
                } else {
                    onShowPermissionRationale(permissions[0], RECORD_AUDIO_REQ_CODE)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(
            KEY_RECORD_PERMISSION_ALREADY_ASKED,
            bAlreadyAskedForRecordPermission
        )
        super.onSaveInstanceState(outState)
    }

}