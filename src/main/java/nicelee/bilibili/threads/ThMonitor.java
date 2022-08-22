package nicelee.bilibili.threads;

import nicelee.bilibili.enums.StatusEnum;
import nicelee.bilibili.live.RoomDealer;
import nicelee.bilibili.模型.录制参数;

public class ThMonitor extends Thread {

	
	long beginTime;
	long fileBeginTime;
	RoomDealer roomDealer;
	录制参数 录制参数儿;
	
	@Deprecated
	public ThMonitor(RoomDealer roomDealer) {
		this.roomDealer = roomDealer;
		this.beginTime = System.currentTimeMillis();
		this.fileBeginTime = beginTime;
		this.setName("thread-monitoring");
		this.setDaemon(true);
	}
	public ThMonitor(RoomDealer roomDealer,录制参数 录制参数儿) {
		this.roomDealer = roomDealer;
		this.beginTime = System.currentTimeMillis();
		this.fileBeginTime = beginTime;
		this.录制参数儿=录制参数儿;
		this.setName("thread-monitoring");
		this.setDaemon(true);
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(10000); // 每10s汇报一次情况
			} catch (InterruptedException e) {
			}
			// 查看当前文件大小, 如果超过阈值，那么重新开始新的录制
			if (录制参数儿.splitFileSize != 0 && roomDealer.util.getDownloadedFileSize() >= 录制参数儿.splitFileSize) {
				fileBeginTime = System.currentTimeMillis();
				录制参数儿.flagSplit = true;
				roomDealer.stopRecord();
			}
			// 查看当前录制时长, 如果超过阈值，那么重新开始新的录制
			if (录制参数儿.splitRecordPeriod != 0
					&& System.currentTimeMillis() - fileBeginTime >= 录制参数儿.splitRecordPeriod) {
				fileBeginTime = System.currentTimeMillis();
				录制参数儿.flagSplit = true;
				roomDealer.stopRecord();
			}
			if (".flv".equals(roomDealer.getType())) {
				if (roomDealer.util.getStatus() == StatusEnum.DOWNLOADING) {
					int period = (int) ((System.currentTimeMillis() - beginTime) / 1000);
					int hour = period / 3600;
					int minute = period / 60 - hour * 60;
					int second = period - minute * 60 - hour * 3600;
					if (hour == 0) {
						System.out.printf("已经录制了%dm%ds, ", minute, second);
					} else {
						System.out.printf("已经录制了%dh%dm%ds, ", hour, minute, second);
					}
					System.out.println(
							"当前进度： " + RoomDealer.transToSizeStr(roomDealer.util.getDownloadedFileSize()));
				} else if (roomDealer.util.getStatus() == StatusEnum.SUCCESS && !录制参数儿.flagStopAfterOffline) {
					// 主播下播后的等待时间
				} else {
					System.out.println("正在处理，请稍等 ");
				}
			} else {
				int period = (int) ((System.currentTimeMillis() - beginTime) / 1000);
				int hour = period / 3600;
				int minute = period / 60 - hour * 60;
				int second = period - minute * 60 - hour * 3600;
				if (hour == 0) {
					System.out.printf("已经录制了%dm%ds, ", minute, second);
				} else {
					System.out.printf("已经录制了%dh%dm%ds, ", hour, minute, second);
				}
				System.out.println("当前进度： " + RoomDealer
						.transToSizeStr(roomDealer.util.getTotalFileSize() * roomDealer.currentIndex));
			}

		}
	}
}
