package cn.edu.uestc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	public static int count = 0;
	public static long lastTime = System.currentTimeMillis();
	public static ProcessHandle currentProcess = ProcessHandle.current();
	public static long lastCPUAccum = currentProcess.info().totalCpuDuration().get().toMillis();

	public static long startTime;
	public static void info(String msg) {
		System.out.print(msg + "\t");
	}

	public static void log(String msg) {
		long curCPUTime = currentProcess.info().totalCpuDuration().get().toMillis() - lastCPUAccum;
		long curTime = System.currentTimeMillis();
		System.out.println("耗时:\t" + String.format("%5d", curTime - lastTime) + "\tCPU时间:\t"
				+ String.format("%5d", curCPUTime) + "\t");
		System.out.print(msg + "\t");
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss" + "\t");
		System.out.print(sdf.format(d));
		lastTime = curTime;
		lastCPUAccum = currentProcess.info().totalCpuDuration().get().toMillis();
	}

	public static void serverLog(Short serverId, String msg) {
//		System.out.println("服务器S" + serverId + ": " + msg);
	}
	public static void debugLog(String msg){
		System.out.println(msg);
	}

	public static void start() {
	    startTime = System.currentTimeMillis();
    }
    public static void end() {
	    System.out.println("耗时: " + (System.currentTimeMillis() - startTime));
    }
}
