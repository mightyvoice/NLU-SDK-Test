package com.example.lj.asrttstest.asr;

/**
 * Created by lj on 16/6/29.
 */
import android.util.Log;

import com.nuance.dragon.toolkit.cloudservices.CloudServices;
import com.nuance.dragon.toolkit.cloudservices.DataParam;
import com.nuance.dragon.toolkit.cloudservices.Param;
import com.nuance.dragon.toolkit.cloudservices.Transaction;
import com.nuance.dragon.toolkit.cloudservices.TransactionError;
import com.nuance.dragon.toolkit.cloudservices.TransactionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.RecogSpec;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionResult;
import com.nuance.dragon.toolkit.cloudservices.recognizer.CloudRecognitionError;

import java.util.List;


/**
 * A cloud text recognizer performs a simple network text recognition using a cloud
 * services transaction.
 *
 * Copyright (c) 2015 Nuance Communications. All rights reserved.
 */
public class CloudTextRecognizer {
    /**
     * Listener interface for cloud recognition results.
     */
    public interface Listener {

        /**
         * A result was received.
         *
         * @param result the result
         */
        void onResult(CloudRecognitionResult result);

        /**
         * An error occurred.
         *
         * @param error the error
         */
        void onError(CloudRecognitionError error);
    }

    /**
     * The Class CloudTextRecognition.
     */
    private static final class CloudTextRecognition {

        /** The m transaction. */
        private Transaction mTransaction;

        // We only want to finalize the transaction after the app has called
        // processResult(), and the transaction has started (which is where the
        // delayed params are added)
        /** The m transaction started. */
        private boolean mTransactionStarted;

        /** The m processing. */
        private boolean mProcessing;
    }

    /** The m cloud services. */
    private final CloudServices mCloudServices;

    /** The m current text recog. */
    private CloudTextRecognition mCurrentTextRecog;

    /**
     * Instantiates a new cloud text recognizer.
     *
     * @param cloudServices The cloud services instance to use
     */
    public CloudTextRecognizer(CloudServices cloudServices) {
        if (cloudServices == null) throw new IllegalArgumentException("cloudServices must not be null");
        mCloudServices = cloudServices;
    }

    /**
     * Start a recognition.
     * @param recogSpec The specification for the recognition.
     * @param resultListener The result listener.
     */
    public void startRecognition(final RecogSpec recogSpec, final Listener resultListener) {

        if (recogSpec == null) throw new IllegalArgumentException("recogSpec must not be null");
        if (resultListener == null) throw new IllegalArgumentException("resultListener must not be null");

        // Cancel any recognition in progress
        cancel();

        final CloudTextRecognition textRecog = new CloudTextRecognition();
        mCurrentTextRecog = textRecog;

        textRecog.mTransaction = new Transaction(recogSpec.getCommand(), recogSpec.getSettings(), new Transaction.Listener() {

            @Override
            public void onTransactionStarted(Transaction t) {

                Log.d("sss", "start text recognition");

                if (textRecog != mCurrentTextRecog)
                    return;

                textRecog.mTransactionStarted = true;

                // Send the delayed parameters
                for (DataParam delayedParam : recogSpec.getDelayedParams()) {
                    textRecog.mTransaction.addParam(delayedParam);
                }

                if (textRecog.mProcessing)
                    textRecog.mTransaction.finish();
            }

            @Override
            public void onTransactionProcessingStarted(Transaction transaction) {

            }

            @Override
            public void onTransactionResult(Transaction t, TransactionResult result, boolean complete) {
                if (textRecog != mCurrentTextRecog)
                    return;

                if (result.isFinal())
                    mCurrentTextRecog = null;

                resultListener.onResult(new CloudRecognitionResult(result));
            }

            @Override
            public void onTransactionError(Transaction t, TransactionError error) {
                if (textRecog == mCurrentTextRecog)
                    mCurrentTextRecog = null;

                resultListener.onError(new CloudRecognitionError(error));
            }

            @Override
            public void onTransactionIdGenerated(String s) {

            }
        }, recogSpec.getTimeout());


        List<DataParam> params = recogSpec.getParams();
        for (Param p : params) {
            textRecog.mTransaction.addParam(p);
        }

        mCloudServices.addTransaction(textRecog.mTransaction, Transaction.DefaultPriorities.RECOGNITION);
    }

    /**
     * End the recognition command, and wait for a result from the server. No
     * more audio or parameters can be processed after calling this.
     */
    public void processResult() {
        if (mCurrentTextRecog == null) throw new IllegalStateException("No recognition in progress");
        if (mCurrentTextRecog.mProcessing) throw new IllegalStateException("Already processing");

        if (mCurrentTextRecog != null) {
            mCurrentTextRecog.mProcessing = true;

            if (mCurrentTextRecog.mTransactionStarted) {
                mCurrentTextRecog.mTransaction.finish();
            }
        }
    }

    /**
     * Cancel the recognition. This is safe to call even if there is no active
     * recognition.
     */
    public void cancel() {
        if (mCurrentTextRecog != null) {
            Transaction t = mCurrentTextRecog.mTransaction;
            mCurrentTextRecog = null;
            t.cancel();
        }
    }

    /**
     * Send an extra parameter. This must be called after calling {@link #startRecognition},
     * but before calling {@link #processResult}.
     * @param param The parameter to add to the recognition command
     */
    public void sendParam(Param param) {
        if (mCurrentTextRecog == null) throw new IllegalStateException("No recognition in progress");
        if (mCurrentTextRecog.mProcessing) throw new IllegalStateException("Already processing");

        if (mCurrentTextRecog != null) {
            mCurrentTextRecog.mTransaction.addParam(param);
        }
    }
}
