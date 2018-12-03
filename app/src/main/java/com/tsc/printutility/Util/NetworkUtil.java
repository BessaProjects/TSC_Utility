package com.tsc.printutility.Util;

import java.io.IOException;

public class NetworkUtil {

    public interface OnPingResultCallback{
        void onPingResult(boolean isReachable);
    }

    public static void isReachable(final String ip, final int port, final OnPingResultCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process p1 = null;
                try {
                    p1 = Runtime.getRuntime().exec("ping -c 1 " + ip + ":22368");
                    int returnVal = p1.waitFor();
                    boolean reachable = (returnVal==0);
                    System.out.println("isReachable:" + ip + ", " + reachable);
                    callback.onPingResult(reachable);
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static boolean isReachable(final String ip, final int port) {
        Process p1 = null;
        try {
            p1 = Runtime.getRuntime().exec("ping -c 1 " + ip + ":22368");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
