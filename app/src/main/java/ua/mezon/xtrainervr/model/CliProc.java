package ua.mezon.xtrainervr.model;

import android.util.Log;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;


/**
 * Created by MezM on 05.06.2017.
 */

public class CliProc {

    private static final CliProc ourInstance = new CliProc();


    private CliProc() {
    }




    public PublishSubject<String> messsubject = PublishSubject.create();
    public ReplaySubject<String> pingsubject = ReplaySubject.create();

//


    public static CliProc getInstance() {
        return ourInstance;
    }


}

