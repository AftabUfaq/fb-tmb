package com.orion.testmybloodft.api;

import android.util.Log;


/**
 * Created by Arun on 1/29/2017.
 * MVP Pattern Structure
 */

public class ApiViewPresenterImpl implements ApiViewPresenter, ApiInteractor.OnApiFinishedListener {
    private static final String TAG = ApiViewPresenterImpl.class.getSimpleName();
    private ApiResponseView responseView;
    private ApiInteractor interactorView;

    public ApiViewPresenterImpl(ApiResponseView responseView) {
        this.responseView = responseView;
        this.interactorView = new ApiInteractorImpl();
    }

    /*
     * onNoInternet @method in ApiInteractor.OnApiFinishedListener
     * */
    @Override
    public void onNoInternet(int serviceType) {
        if (null != responseView) {
            responseView.hideProgress(serviceType);
            responseView.onNoInternet(serviceType);
        } else {
            Log.i(TAG, "onNoInternet responseView is Null ");
        }
    }

    /*
     * showProgress @method in ApiInteractor.OnApiFinishedListener
     * */
    @Override
    public void showProgress(int serviceType) {
        if (null != responseView) {
            responseView.showProgress(serviceType);
        } else {
            Log.i(TAG, "showProgress responseView is Null ");
        }
    }

    /*
     * onSuccessData @method in ApiInteractor.OnApiFinishedListener
     * */
    @Override
    public void onSuccessData(String response, int serviceType) {
        if (null != responseView) {
            responseView.hideProgress(serviceType);
            responseView.onSuccessData(response, serviceType);
        } else {
            Log.i(TAG, "onSuccessData responseView is Null ");
        }
    }

    /*
     * onSuccessException @method in ApiInteractor.OnApiFinishedListener
     * */
    @Override
    public void onSuccessException(String response, Throwable throwable, int serviceType) {
        if (null != responseView) {
            responseView.hideProgress(serviceType);
            responseView.onSuccessException(response, throwable, serviceType);
        } else {
            Log.i(TAG, "onSuccessException responseView is Null ");
        }
    }

    /*
    * onExceptionError @method in ApiInteractor.OnApiFinishedListener
    * */
    @Override
    public void onErrorData(Throwable throwable, int serviceType) {
        if (null != responseView) {
            responseView.hideProgress(serviceType);
            responseView.onErrorData(throwable, serviceType);
        } else {
            Log.i(TAG, "onErrorData responseView is Null ");
        }
    }

    /*
     * validateToHitRequest @method in ApiViewPresenter
     * */
    @Override
    public void validateToHitRequest(Object apiModel) {
        if (null != interactorView) {
            interactorView.hitRequest(apiModel, this);
        }
    }

    /*
     * onDestroy @method in ApiViewPresenter
     * */
    @Override
    public void onDestroy() {
        responseView = null;
    }
}
