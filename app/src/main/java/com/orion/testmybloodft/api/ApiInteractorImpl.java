package com.orion.testmybloodft.api;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.orion.testmybloodft.api.multipart.AppHelper;
import com.orion.testmybloodft.api.multipart.VolleyMultipartRequest;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.utils.Constants;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Arun on 1/29/2017.
 * MVP Pattern Structure
 */

public class ApiInteractorImpl implements ApiInteractor {

    @Override
    public void hitRequest(Object apiModel, OnApiFinishedListener listener) {

        ApiModel apiModelData = (ApiModel) apiModel;
        //new ApiCallAsyncTak(apiModel,listener).execute();
       /* ApiCallAsyncTak apiCallAsyncTak = new ApiCallAsyncTak(apiModel,listener);
        apiCallAsyncTak.execute();*/

        if (Constants.isNetworkAvailable()) {
            listener.showProgress(apiModelData.serviceType);
            new ApiCallAsyncTak(apiModelData, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Toast.makeText(apiModelData.context, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            listener.onNoInternet(((ApiModel) apiModel).serviceType);
        }
    }

    class ApiCallAsyncTak extends AsyncTask<Void, Void, Void> {
        //private final String TAG = ApiCallAsyncTak.class.getSimpleName();
        private final String TAG = "CHECK_MY_THING";

        private String FROM_TAG = "FROM_TAG";
        private Context context;
        private Activity activity;
        private int DATA_OR_FILE_Type = 0;
        public String TOKEN_HEADER;
        private Drawable drawable;
        private String filename;
        JSONObject jsonBody;
        private final Map<String, String> paramss;
        String mRequestBody;
        private OnApiFinishedListener resultListener;
        private int serviceType;
        private String url;
        int method;
        //private String tag_json_obj = "jobj_req";
        //private String serverStatusCode = "-1";
        private boolean volleyDataCache = false;
        private Request.Priority priority;
        public String CATCH_KEY = "CATCH_KEY";

        public ApiCallAsyncTak(Object apiModel, OnApiFinishedListener resultListener) {
            ApiModel apiModelData = (ApiModel) apiModel;
            this.FROM_TAG = apiModelData.FROM_TAG;
            this.TOKEN_HEADER = apiModelData.TOKEN_HEADER;
            this.DATA_OR_FILE_Type = apiModelData.DATA_OR_FILE_Type;
            this.drawable = apiModelData.drawable;
            this.filename = apiModelData.filename;
            this.context = apiModelData.context;
            this.activity = apiModelData.activity;
            this.paramss = apiModelData.params;
            this.url = apiModelData.url;
            this.serviceType = apiModelData.serviceType;
            this.method = apiModelData.method;
            this.resultListener = resultListener;
            volleyDataCache = apiModelData.volleyDataCache;
            priority = apiModelData.priority;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.i(TAG, "Request FROM_TAG : " + FROM_TAG);
            Log.i(TAG, "Request 0-> DATA or 1-> IMAGE, DATA_OR_FILE_Type : " + DATA_OR_FILE_Type);
            Log.i(TAG, "0 -> GET or 1 -> POST, Request Method : " + method);
            Log.i(TAG, "Request Service Type Code : " + serviceType);

            url += ServerApi.getApiType(serviceType);

            Log.i(TAG, "Request url : " + url);
            Log.i(TAG, "Request Catch : " + volleyDataCache);
            Log.i(TAG, "Request Priority : " + priority);
            Log.i(TAG, "Request filename : " + filename);

            /*Request Data Format */
            Log.i(TAG, "Request TOKEN_HEADER : " + TOKEN_HEADER);

            //http://www.alloyteam.com/2015/06/yong-volley-cai-di-keng/comment-page-1/
            if (paramss != null) {//method == 1 &&
                if (method == 1) {
                    jsonBody = new JSONObject(this.paramss);
                    mRequestBody = jsonBody.toString();
                    for (Map.Entry<String, String> entry : paramss.entrySet()) {
                        Log.i(TAG, "Request Params : Key = " + entry.getKey() + ", Value = " + entry.getValue());
                    }
                } else {
                   /* for (Map.Entry<String, String> entry : paramss.entrySet()) {
                        Log.i(TAG, "Request Params : Key = " + entry.getKey() + ", Value = " + entry.getValue());
                    }*/
                    String values = "";
                    for (String value : paramss.values()) {
                        // do something with tab
                        if (!TextUtils.isEmpty(values)) {
                            values += ",";
                        }
                        values += value;
                    }
                    Log.i(TAG, "Request Params : Values = " + values);
                    url += values;
                }
                Log.i(TAG, "params size= " + paramss.size());
            } else {
                jsonBody = null;
                mRequestBody = null;
            }

            if (DATA_OR_FILE_Type == 0) {
                StringRequest myReq = new StringRequest(method,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i(TAG, response.toString());
                                try {
                                    resultListener.onSuccessData(response.toString(), serviceType);
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                    resultListener.onSuccessException(response, e, serviceType);
                                }

                            }
                        },
                        new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.d(TAG, "Error: " + error.getMessage());
                                Log.i(TAG, "onErrorResponse Exception ", error);
                                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                                    Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "onErrorResponse TimeoutError or NoConnectionError ");
                                } else if (error instanceof AuthFailureError) {
                                    //TODO
                                    Log.i(TAG, "onErrorResponse AuthFailureError ");
                                } else if (error instanceof ServerError) {
                                    //TODO
                                    Log.i(TAG, "onErrorResponse ServerError ");
                                } else if (error instanceof NetworkError) {
                                    //TODO
                                    Log.i(TAG, "onErrorResponse NetworkError ");
                                } else if (error instanceof ParseError) {
                                    //TODO
                                    Log.i(TAG, "onErrorResponse ParseError ");
                                }

                                resultListener.onErrorData(error, serviceType);
                            }
                        }) {

                /*protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {

                    Log.i("params", "params= "+ paramss.size());
                    Log.i(TAG, "Url= "+ url+ paramss.toString());
                    return paramss;
                };*/

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("authorization", TOKEN_HEADER);
                        return headers;
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    public String getCacheKey() {
                        String temp = super.getCacheKey();
                        if (method == 1) {
                            try {
                                for (Map.Entry<String, String> entry : paramss.entrySet())
                                    temp += entry.getKey() + "=" + entry.getValue();
                                CATCH_KEY = temp;
                                Log.i(TAG, "Post Method Cache Key : " + CATCH_KEY);
                                return temp;
                            } catch (Exception uee) {
                                Log.i(TAG, "getCacheKey Exception" + temp);
                            }
                        }
                        CATCH_KEY = temp;
                        Log.i(TAG, "Get Method Cache Key : " + CATCH_KEY);
                        return temp;
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        //serverStatusCode = String.valueOf(response.statusCode);
                        Log.i(TAG, " Server Response Status Code : " + String.valueOf(response.statusCode));
                /*String responseString = "";
                if (response != null) {
                responseString = String.valueOf(response.statusCode);
                // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));*/
                        try {
                            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                            return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
                        } catch (Exception e) {
                            return Response.error(new ParseError(e));
                        }

                    }

                    @Override
                    public Priority getPriority() {
                        return priority;
                    }
                };
                // Adding request to request queue
                myReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                myReq.setShouldCache(volleyDataCache);
                myReq.setTag(FROM_TAG);
                //MyVolley.getRequestQueue().add(myReq);
                MyVolley.getInstance(context).getRequestQueue().add(myReq);
            } else {
                //https://gist.github.com/anggadarkprince/a7c536da091f4b26bb4abf2f92926594
                VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        Log.i(TAG, "VolleyMultipartRequest :\n" + resultResponse.toString());
                        try {
                            resultListener.onSuccessData(resultResponse, serviceType);
                        } catch (Exception e) {
                            //e.printStackTrace();
                            Log.i(TAG, "VolleyMultipartRequest onResponse Exception", e);
                            resultListener.onSuccessException(resultResponse, e, serviceType);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = "Unknown error";
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = "Request timeout";
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = "Failed to connect server";
                            }
                            Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                        } else {
                            String result = new String(networkResponse.data);
                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");

                                Log.i("Error Status", status);
                                Log.i("Error Message", message);

                                if (networkResponse.statusCode == 404) {
                                    errorMessage = "Resource not found";
                                } else if (networkResponse.statusCode == 401) {
                                    errorMessage = message + " Please login again";
                                } else if (networkResponse.statusCode == 400) {
                                    errorMessage = message + " Check your inputs";
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = message + " Something is getting wrong";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        resultListener.onErrorData(error, serviceType);
                        Log.i(TAG, "VolleyMultipartRequest Error : " + errorMessage);
                        error.printStackTrace();
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("authorization", TOKEN_HEADER);
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Log.i(TAG, "getParams");
                        Map<String, String> params = paramss;
                        /*params.put("api_token", "gh659gjhvdyudo973823tt9gvjf7i6ric75r76");
                        params.put("name", mNameInput.getText().toString());
                        params.put("location", mLocationInput.getText().toString());
                        params.put("about", mAvatarInput.getText().toString());
                        params.put("contact", mContactInput.getText().toString());*/
                        return params;
                    }

                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        // file name could found file base or direct access from real path
                        // for now just get bitmap data from ImageView
                        Log.d(TAG, "getByteData FILE TYPE: " + DATA_OR_FILE_Type);
                        if (DATA_OR_FILE_Type == 1) {
                            params.put("profile_pic", new DataPart("avatar.jpg", AppHelper.getFileDataFromDrawable(context, drawable), "image/jpeg|image/png"));
                        }
                        return params;
                    }
                };
                multipartRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                //multipartRequest.setShouldCache(volleyDataCache);
                multipartRequest.setTag(FROM_TAG);
                // Adding request to request queue
                MyVolley.getInstance(context).getRequestQueue().add(multipartRequest);
            }
            return null;
        }
    }
}
