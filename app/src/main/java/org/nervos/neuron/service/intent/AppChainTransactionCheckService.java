package org.nervos.neuron.service.intent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;

import org.nervos.neuron.service.http.AppChainTransactionService;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionCheckService extends JobIntentService {

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AppChainTransactionCheckService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        AppChainTransactionService.checkTransactionStatus(this);
    }

}
