package com.orion.testmybloodft.config;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Arun on 10/3/2016.
 */

public class MyVolley {

    private static MyVolley mInstance;
    private RequestQueue mRequestQueue;

    public MyVolley() {
        // no instances
    }

    public MyVolley(Context context){
        initImgOkHttp3Stack(context);
    }

    /**
     * Singleton construct design pattern.
     *
     * @param context parent context
     * @return single instance of VolleySingleton
     */
    public static synchronized MyVolley getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyVolley(context);
        }
        return mInstance;
    }

    public void initOkHttp3Stack(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        // mImageLoader = new SimpleImageLoader(mRequestQueue, BitmapImageCache.getInstance(null));
    }

    public void initImgOkHttp3Stack(Context context) {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     *
     * @return
     */

    /**
     * Get image loader.
     *
     * @return ImageLoader
     */
}
