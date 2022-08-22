package nicelee.bilibili.threads;

import nicelee.bilibili.Config;
import nicelee.bilibili.live.RoomDealer;
import nicelee.bilibili.live.check.FlvCheckerWithBufferEx;
import nicelee.bilibili.live.convert.Flv2Mp4Converter;
import nicelee.bilibili.plugin.Plugin;
import nicelee.bilibili.util.ZipUtil;
import nicelee.bilibili.模型.录制参数;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThConvert extends Thread {
    RoomDealer roomDealer;
    List<String> fileList;
    Plugin plugin;
    int count;
    录制参数 录制参数儿;
    @Deprecated
    public ThConvert(RoomDealer roomDealer, List<String> fileList,  Plugin plugin,int count) {
        this.roomDealer = roomDealer;
        this.fileList = fileList;
        this.plugin = plugin;
        this.count = count;
    }
    public ThConvert(RoomDealer roomDealer, List<String> fileList,  Plugin plugin,int count,录制参数 录制参数儿) {
        this.roomDealer = roomDealer;
        this.fileList = fileList;
        this.plugin = plugin;
        this.count = count;
        this.录制参数儿=录制参数儿;
    }

    @Override
    public void run() {
        System.out.println("文件转码中");
        if (".flv".equals(roomDealer.getType())) {
            if (!"".equals(录制参数儿.outputFormat)){
                try {
                    for (String path : fileList) {
                        System.out.println("文件转码开始...");
                        new Flv2Mp4Converter().convert(path, 录制参数儿.outputFormat,count);
                        System.out.println("文件转码完毕。");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (".ts".equals(roomDealer.getType())) {
//				System.out.println("正在合并...");
//				M3u8Downloader m3u8 = new M3u8Downloader();
//				m3u8.merge(file, roomDealer.currentIndex, true);
//				// 删除可能存在的part文件
//				String part = String.format("%s-%d%s.part", filename, roomDealer.currentIndex,
//						roomDealer.getType());
//				new File(file.getParent(), part).delete();
//				// 将ts文件移动到上一层文件夹
//				dstFile.renameTo(new File(dstFile.getParentFile().getParentFile(), dstFile.getName()));
//				dstFile.getParentFile().delete();
//				System.out.println("合并结束...");
        }

    }
}
