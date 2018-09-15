package com.example.android.learnarchitecturecomponents.livedata;

import android.arch.lifecycle.LiveData;

import java.math.BigDecimal;

public class StockLiveData extends LiveData<BigDecimal> {

    /*private StockManager mStockManager;

    private SimplePriceListener mListener = new SimplePriceListener() {
        @Override
        public void onPriceChanged(BigDecimal price) {
            setValue(price);
        }
    };

    public StockLiveData(String symbol) {
        mStockManager = new StockManager(symbol);
    }

    @Override
    protected void onActive() {
        mStockManager.requestPriceUpdates(mListener);
    }

    @Override
    protected void onInactive() {
        mStockManager.removeUpdates(mListener);
    }

    interface SimplePriceListener {
        void onPriceChanged(BigDecimal price);
    }


    private class StockManager {

        String symbol;

        public StockManager(String symbol) {
            this.symbol = symbol;
        }

        public void requestPriceUpdates(SimplePriceListener mListener) {
            mListener.onPriceChanged();
        }
    }*/
}

