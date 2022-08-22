package nicelee.bilibili.threads;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nicelee.bilibili.Main;
import nicelee.bilibili.SignalHandler;
import nicelee.bilibili.enums.StatusEnum;
import nicelee.bilibili.live.RoomDealer;
import nicelee.bilibili.live.domain.RoomInfo;
import nicelee.bilibili.plugin.Plugin;
import nicelee.bilibili.util.Logger;
import nicelee.bilibili.模型.录制参数;

public class ThRecord extends Thread {

	RoomDealer roomDealer;
	RoomInfo roomInfo;
	String cookie;
	Plugin plugin;

	Lock lockOfRecord;
	Lock lockOfCheck;
	录制参数 录制参数儿;

	@Deprecated
	public ThRecord(RoomDealer roomDealer, RoomInfo roomInfo, String cookie, Plugin plugin) {
		this.roomInfo = roomInfo;
		this.roomDealer = roomDealer;
		this.cookie = cookie;
		this.plugin = plugin;
		this.setName("thread-Record");
		this.lockOfRecord = new ReentrantLock(true);
		this.lockOfCheck = new ReentrantLock(true);
	}
	public ThRecord(RoomDealer roomDealer, RoomInfo roomInfo, String cookie, Plugin plugin,录制参数 录制参数儿) {
		this.roomInfo = roomInfo;
		this.roomDealer = roomDealer;
		this.cookie = cookie;
		this.plugin = plugin;
		this.录制参数儿=录制参数儿;
		this.setName("thread-Record");
		this.lockOfRecord = new ReentrantLock(true);
		this.lockOfCheck = new ReentrantLock(true);
	}

	@Override
	public void run() {

		String url = roomDealer.getLiveUrl((roomInfo.getRoomId()), "" + 录制参数儿.qn, roomInfo.getRemark(), cookie);
		Logger.println(url);
		System.out.println("开始录制，输入stop停止录制");
		List<String> fileList = new ArrayList<String>(); // 用于存放录制产生的初始flv文件
		// 在开启录制之前，添加对退出信号的捕捉处理
		lockOfRecord.lock();
		Runtime.getRuntime().addShutdownHook(new SignalHandler(lockOfRecord, lockOfCheck, roomDealer));
		record(roomDealer, roomInfo, url, fileList);

		try {
			while (true) {
				if ((roomDealer.util.getStatus() == StatusEnum.STOP && 录制参数儿.flagSplit)
						|| (roomDealer.util.getStatus() == StatusEnum.SUCCESS && !录制参数儿.flagStopAfterOffline)) {
					// 判断当前状态
					if (roomDealer.util.getStatus() == StatusEnum.STOP) {
						System.out.println("文件大小或录制时长超过阈值，重新尝试录制");
					} else {
						System.out.println("主播下播，等待下一次录制");
						// 另起线程处理媒体文件
						Thread th = new ThCheckMedia(roomDealer, fileList, lockOfCheck, plugin,录制参数儿);
						fileList = new ArrayList<String>();
						th.start();
						lockOfRecord.unlock();
						try {
							System.out.println(录制参数儿.retryAfterMinutes + "分钟左右后重试");
							sleep((long) (录制参数儿.retryAfterMinutes * 60000));
						} catch (InterruptedException e) {
							break;
						}
						roomInfo = Main.getRoomInfo(roomDealer,录制参数儿);
						if (roomInfo.getLiveStatus() != 1)
							break;
						lockOfRecord.lock();
					}

					// 重置状态
					roomDealer.util.init();
					录制参数儿.flagSplit = false;
					录制参数儿.failCnt = 0;
					url = roomDealer.getLiveUrl((roomInfo.getRoomId()), "" + 录制参数儿.qn, roomInfo.getRemark(), cookie);
					Logger.println(url);
					record(roomDealer, roomInfo, url, fileList);
				} else if (roomDealer.util.getStatus() == StatusEnum.FAIL && 录制参数儿.maxFailCnt >= 录制参数儿.failCnt) {
					// 判断当前状态 如果异常连接导致失败，那么重命名后重新录制
					录制参数儿.failCnt++;
					System.out.printf("连接异常，%.1fmin后重新尝试录制\r\n", 录制参数儿.failRetryAfterMinutes);
					try {
						sleep((long) (录制参数儿.failRetryAfterMinutes * 60000));
					} catch (InterruptedException e) {
						break;
					}
					url = roomDealer.getLiveUrl((roomInfo.getRoomId()), "" + 录制参数儿.qn, roomInfo.getRemark(), cookie);
					Logger.println(url);
					record(roomDealer, roomInfo, url, fileList);
				} else {
					break;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("下载停止");
		if (fileList.size() > 0) {
			Thread th = new ThCheckMedia(roomDealer, fileList, lockOfCheck, plugin,录制参数儿);
			th.start();
		}
		try {
			lockOfRecord.unlock();
		} catch (Exception e) {
		}
	}

	String pathFormat(String pattern, RoomInfo roomInfo, SimpleDateFormat sdf) {
		return pattern.replace("{name}", roomInfo.getUserName()).replace("{shortId}", roomInfo.getShortId())
				.replace("{roomId}", roomInfo.getRoomId()).replace("{liver}", 录制参数儿.liver)
				.replace("{startTime}", sdf.format(new Date()));
	}

	void record(RoomDealer roomDealer, RoomInfo roomInfo, String url, List<String> fileList) {
		SimpleDateFormat sdf = new SimpleDateFormat(录制参数儿.timeFormat);
		// "{name}-{shortId} 的{liver}直播{startTime}-{seq}";
		String realName = pathFormat(录制参数儿.fileName, roomInfo, sdf).replace("{seq}", "" + fileList.size())
				.replaceAll("[\\\\|\\/|:\\*\\?|<|>|\\||\\\"$]", ".");
		// 如果saveFolder不为空
		if (录制参数儿.saveFolder != null) {
			录制参数儿.saveFolder = pathFormat(录制参数儿.saveFolder, roomInfo, sdf);
			roomDealer.util.setSavePath(录制参数儿.saveFolder);
		}
		// 如果saveFolderAfterCheck不为空
		if (录制参数儿.autoCheck && 录制参数儿.saveFolderAfterCheck != null) {
			录制参数儿.saveFolderAfterCheck = pathFormat(录制参数儿.saveFolderAfterCheck, roomInfo, sdf);
			File f = new File(录制参数儿.saveFolderAfterCheck);
			if (!f.exists())
				f.mkdirs();
		}
		roomDealer.startRecord(url, realName, roomInfo.getShortId());// 此处一直堵塞， 直至停止
		File file = roomDealer.util.getFileDownload();

		File partFile = new File(file.getParent(), realName + roomDealer.getType() + ".part");
		File completeFile = new File(file.getParent(), realName + roomDealer.getType());
		realName = realName.replace("{endTime}", sdf.format(new Date()));
		File dstFile = new File(file.getParent(), realName + roomDealer.getType());

		if (partFile.exists())
			partFile.renameTo(dstFile);
		else
			completeFile.renameTo(dstFile);

		// 加入已下载列表
		fileList.add(dstFile.getAbsolutePath());
	}
}
