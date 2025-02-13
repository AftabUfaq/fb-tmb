package com.orion.testmybloodft.api;

/**
 * Created by Arun on 1/29/2017.
 * MVP Pattern Structure
 */

public interface ApiInteractor {

    interface OnApiFinishedListener {
        void onNoInternet(int serviceType);

        void showProgress(int serviceType);

        void onSuccessData(String response, int serviceType);

        void onSuccessException(String response, Throwable throwable, int serviceType);

        void onErrorData(Throwable throwable, int serviceType);
    }

    void hitRequest(Object apiModel, OnApiFinishedListener listener);
}
