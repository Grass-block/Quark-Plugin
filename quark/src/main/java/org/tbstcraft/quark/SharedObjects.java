package org.tbstcraft.quark;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public interface SharedObjects {
    DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.###");
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat DATE_FORMAT_FILE = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
}
