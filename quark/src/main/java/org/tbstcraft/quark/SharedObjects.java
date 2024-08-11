package org.tbstcraft.quark;

import com.google.gson.JsonParser;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface SharedObjects {
    DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.###");
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    ExecutorService SHARED_THREAD_POOL = Executors.newFixedThreadPool(4);

    Random RANDOM = new Random();
    JsonParser JSON_PARSER = new JsonParser();
}
