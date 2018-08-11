package ua.mezon.xtrainervr;

import android.support.v7.app.AppCompatActivity;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

public interface BLEConnector {

    void initBLE(AppCompatActivity activity);

    void stopBLEScan();

    PublishSubject<String> getMesssubject();

    ReplaySubject<String> getPingsubject();
}
