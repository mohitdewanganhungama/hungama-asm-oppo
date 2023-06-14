package com.hungama.music.data.webservice.remote

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant.ISGOTODOWNLOADCLICKED


class VolleySinglton(private val context: Context) {

    val PUBLIC_KEY = "SERVER_PUBLIC_KEY"
    private var requestQueue: RequestQueue?



    companion object {
        var instance: VolleySinglton? = null
        @Synchronized
        fun getInstance(context: Context): VolleySinglton? {
            if (instance == null) {
                instance = VolleySinglton(context)
            }
            return instance
        }
    }

    init {
        requestQueue = getRequestQueue()
    }

    fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(
                context.applicationContext
            )
//            getSocketFactory(context.applicationContext)
//            HttpsTrustManager.allowAllSSL();
        }
        return requestQueue as RequestQueue
    }

    fun <T> addToRequestQueue(request: Request<T>?) {
        getRequestQueue().add(request)
        if (ConnectionUtil(context).isOnline(false)){
            ISGOTODOWNLOADCLICKED = false
        }
    }
//    private fun pinnedSSLSocketFactory(): SSLSocketFactory? {
//        try {
//            return TLSSocketFactory(PUBLIC_KEY)
//        } catch (e: KeyManagementException) {
//            e.printStackTrace()
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//        }
//        return null
//    }
//
//
//    fun getSocketFactory(context: Context): SSLSocketFactory? {
//
//        // Load CAs from an InputStream (could be from a resource or ByteArrayInputStream or ...)
//        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
//        val caInput: InputStream =
//            BufferedInputStream(context.resources.openRawResource(com.hungama.music.R.raw.hungama))
//        // I paste my myFile.crt in raw folder under res.
//        val ca: Certificate
//        try {
//            ca = cf.generateCertificate(caInput)
//            System.out.println("ca=" + (ca as X509Certificate).getSubjectDN())
//        } finally {
//            caInput.close()
//        }
//
//        // Create a KeyStore containing our trusted CAs
//        val keyStoreType = KeyStore.getDefaultType()
//        val keyStore = KeyStore.getInstance(keyStoreType)
//        keyStore.load(null, null)
//        keyStore.setCertificateEntry("ca", ca)
//
//        // Create a TrustManager that trusts the CAs in our KeyStore
//        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
//        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
//        tmf.init(keyStore)
//
//        // Create an SSLContext that uses our TrustManager
//        val sslContext = SSLContext.getInstance("TLS")
//        sslContext.init(null, tmf.trustManagers, null)
//        return sslContext.socketFactory
//    }
}