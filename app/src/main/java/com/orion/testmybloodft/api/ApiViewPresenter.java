package com.orion.testmybloodft.api;

/**
 * Created by Arun on 1/29/2017.
 * MVP Pattern Structure
 */

public interface ApiViewPresenter {

    void validateToHitRequest(Object apiModel);

    void onDestroy();
}
