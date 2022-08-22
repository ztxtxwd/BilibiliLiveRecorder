package nicelee.bilibili;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nicelee.bilibili.live.RoomDealer;
import nicelee.bilibili.live.domain.RoomInfo;
import nicelee.bilibili.plugin.Plugin;
import nicelee.bilibili.threads.ThCommand;
import nicelee.bilibili.threads.ThMonitor;
import nicelee.bilibili.threads.ThRecord;
import nicelee.bilibili.模型.录制参数;

public class Main {

	final static String version = "v2.18.0";
	public static Thread thRecord;
	public static Thread thMonitor;
	public static Thread thCommand;

	/**
	 * 程序入口
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
//		 args = new String[]{"debug=true&liver=bili&id=3291024&delete=false&check=true"};  			// 清晰度全部可选，可不需要cookie
//		args = new String[] {
//				"plugin=true&debug=false&check=true&retryAfterMinutes=0.5&retryIfLiveOff=true&liver=douyu&qnPri=蓝光4M>高清>蓝光8M>超清>蓝光>流畅&qn=-1&id=262537&fileName=测试{liver}-{name}-{startTime}-{endTime}-{seq}&" }; // 清晰度全部可选，但部分高清需要cookie
//		args = new String[] { "debug=true&check=true&liver=kuaishou&id=3xh62hmw79fmc32&qn=0&delete=false&fileName=测试{liver}-{name}-{startTime}-{endTime}-{seq}&timeFormat=yyyyMMddHHmm" }; // 清晰度全部可选，可不需要cookie
//		args = new String[]{"debug=true&check=true&liver=huya&id=11342412"}; 				// 清晰度全部可选，可不需要cookie 
//		args = new String[]{"debug=true&check=true&liver=yy&id=28581146&qn=1"}; 		// 只支持默认清晰度 54880976
//		args = new String[] { "debug=true&check=true&liver=zhanqi&id=90god" }; 			// 清晰度全部可选，可不需要cookie 90god huashan ydjs
//		args = new String[] { "debug=true&check=true&liver=huajiao&id=278581432&qn=1" }; // 只支持默认清晰度(似乎只有一种清晰度)
//		args = new String[] { "debug=true&check=true&liver=acfun&id=378269" };
//		args = new String[]{"debug=true&liver=douyin&id=https://v.douyin.com/dFfDBcU&delete=false&check=false"};  			// 清晰度全部可选，可不需要cookie 
		args = new String[]{"debug=false&liver=douyin&id=286348522726&delete=true&check=true&format=mp4"};  			// 清晰度全部可选，可不需要cookie
//		args = new String[]{"debug=true&liver=douyin&id=https://v.douyin.com/EQBYoH&delete=false&check=false"};  			// 清晰度全部可选，可不需要cookie 
//		args = new String[] { "debug=true&liver=douyin_web&id=https://v.douyin.com/EQBYoH&delete=false&check=false" }; // 清晰度全部可选，可不需要cookie
//		args = new String[]{"debug=true&liver=douyin_web&id=227807351025&delete=false&check=false"};  			// 清晰度全部可选，可不需要cookie 

		final Plugin plugin = new Plugin();
		if (args != null && args[0].contains("plugin=true")) {
			try {
				plugin.compile();
				plugin.runBeforeInit(args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 根据参数初始化配置
		录制参数 录制参数儿 = new 录制参数(args);
		if (录制参数儿.flagPlugin) {
			try {
				plugin.runAfterInit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(录制参数儿.liver + " 直播录制 version " + version);

		// 如果没有传入房间号，等待输入房间号
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		if (录制参数儿.shortId == null) {
			System.out.println("请输入房间号(直播网址是https://xxx.com/xxx，那么房间号就是xxx)");
			String line = reader.readLine();
			录制参数儿.shortId = line;
		}

		// 加载cookies
		String cookie = null;
		try {
			BufferedReader buReader = new BufferedReader(new FileReader(录制参数儿.liver + "-cookie.txt"));
			cookie = buReader.readLine();
			buReader.close();
			// Logger.println(cookie);
		} catch (Exception e) {
		}
		RoomDealer roomDealer = getRoomDealer(录制参数儿.liver);
		if (cookie != null) {
			roomDealer.setCookie(cookie);
		}
		RoomInfo roomInfo = getRoomInfo(roomDealer,录制参数儿);
		// 清晰度获取
		// 先使用预设的优先级获取
		if (录制参数儿.qnPriority != null) {
			String qnDescs[] = roomInfo.getAcceptQualityDesc();
			boolean findQn = false;
			for (int i = 0; i < 录制参数儿.qnPriority.length; i++) {
				// 遍历qnDescs, 如果符合要求，则设置清晰度
				for (int j = 0; j < qnDescs.length; j++) {
					if (qnDescs[j].equals(录制参数儿.qnPriority[i])) {
						录制参数儿.qn = roomInfo.getAcceptQuality()[j];
						findQn = true;
						break;
					}
				}
				if (findQn)
					break;
			}
		}
		// qn = -1, 使用最高画质
		if ("-1".equals(录制参数儿.qn)) {
			录制参数儿.qn = roomInfo.getAcceptQuality()[0];
		}
		// 没有获取到清晰度，则提示输入
		if (录制参数儿.qn == null) {
			// 输入清晰度后，获得直播视频地址
			System.out.println("请输入清晰度代号(:之前的内容，不含空格)");
			录制参数儿.qn = reader.readLine();
		}
		// 检查清晰度的合法性
		boolean qnIsValid = false;
		String validQN[] = roomInfo.getAcceptQuality();
		for (int i = 0; i < validQN.length; i++) {
			if (validQN[i].equals(录制参数儿.qn)) {
				qnIsValid = true;
				break;
			}
		}
		if (!qnIsValid) {
			System.err.println("输入的qn值不在当前可获取清晰度列表中");
			System.exit(-1);
		}
		// String url = roomDealer.getLiveUrl(roomInfo.getRoomId(),
		// roomInfo.getAcceptQuality()[0]);

		// 开始录制
		thRecord = new ThRecord(roomDealer, roomInfo, cookie, plugin,录制参数儿);
		thRecord.start();

		// 输出进度，超过指定大小后重新开始一次
		thMonitor = new ThMonitor(roomDealer,录制参数儿);
		thMonitor.start();

		// 接收输入指令，停止录制
		thCommand = new ThCommand(roomDealer, thRecord, reader,录制参数儿);
		thCommand.start();

	}

	/**
	 * 获取房间信息，当配置恰当时，将一直轮询，直到获得已经开播的房间信息
	 * 
	 * @param roomDealer
	 * @return 房间信息
	 */
	public static RoomInfo getRoomInfo(RoomDealer roomDealer,录制参数 录制参数儿) {
		// 获取房间信息
		RoomInfo rroomInfo = roomDealer.getRoomInfo(录制参数儿.shortId);

		if (rroomInfo == null) {
			System.err.println("解析失败！！");
			System.exit(-2);
		}
		// 查看是否在线
		if (rroomInfo.getLiveStatus() != 1) {
			System.out.println("当前没有在直播");
			int retryCntLiveOff = 0;
			if (录制参数儿.retryIfLiveOff) {
				while (rroomInfo.getLiveStatus() != 1
						&& (录制参数儿.maxRetryIfLiveOff == 0 || 录制参数儿.maxRetryIfLiveOff > retryCntLiveOff)) {
					retryCntLiveOff++;
					try {
						System.out.println(录制参数儿.retryAfterMinutes + "分钟左右后重试");
						Thread.sleep((long) (录制参数儿.retryAfterMinutes * 60000));
					} catch (InterruptedException e) {
						break;
					}
					rroomInfo = roomDealer.getRoomInfo(录制参数儿.shortId);
					if (rroomInfo == null) {
						System.err.println("解析失败！！");
						System.exit(-2);
					}
				}
			} else {
				System.exit(3);
			}
		}
		return rroomInfo;
	}

	/**
	 * 获取正确的视频录制器
	 * 
	 * @param liver
	 * @return
	 */
	private static RoomDealer getRoomDealer(String liver) {
		return RoomDealer.createRoomDealer(liver);
	}

}
