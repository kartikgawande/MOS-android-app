package com.example.mos.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> dbUpdated = new MutableLiveData<>();

    public SharedViewModel() {
        this.dbUpdated.postValue(false);
    }

    public MutableLiveData<Boolean> isDbUpdated() {
        return dbUpdated;
    }

    public void setDbUpdated(boolean value) {
        this.dbUpdated.postValue(value);
    }
}
