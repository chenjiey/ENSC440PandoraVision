package fortyonepost.com.ag;

import fortyonepost.com.ag.AccessGyroscope;

public class MyRunnable implements Runnable {
	public static String[] rpi_list;
	
	public MyRunnable(String rpi_ips) {
		rpi_list = rpi_ips.split(",");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		AccessGyroscope.twoVideo1.setText(
			String.format("rtsp://%s:8554/pi_encode.h264", fortyonepost.com.ag.MyRunnable.rpi_list[0]));

	}

}
