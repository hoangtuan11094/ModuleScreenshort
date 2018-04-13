package com.hoangtuan.modulescreenshort;



import com.hoangtuan.modulescreenshort.Libs.screenshotobserver.ScreenshotObserverService;

/**
 * Created by atbic on 12/4/2018.
 */


public class ScreenShotSevice extends ScreenshotObserverService {

    @Override
    protected void onScreenShotTaken(String path, String fileName) {
        ScreenShotActivity.startActivityToScreenShotActivity(this,path,fileName);
    }
}
