package nicelee.bilibili.live.convert;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Flv2Mp4Converter {
    public void convert(String path,String format,int count) throws IOException {
        FFmpeg ffmpeg = new FFmpeg("ffmpeg");
        FFprobe ffprobe = new FFprobe("ffprobe");
        path = path.replaceFirst(".flv$", "-checked" + count + ".flv");
//        FFmpegProbeResult probe = ffprobe.probe(path);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setVerbosity(FFmpegBuilder.Verbosity.ERROR)
                .setInput(path)     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput(path.split("\\.")[0]+path.split("\\.")[1]+"."+format)   // Filename for the destination
                .setFormat(format)        // Format is inferred from filename, or can be set
                .setVideoCodec("copy")
                .setAudioCodec("copy")
//                .setVideoBitRate(probe.getFormat().bit_rate)
//                .setVideoWidth(probe.getStreams().get(0).width)
//                .setVideoHeight(probe.getStreams().get(0).height)
//                .setVideoCodec("h264_videotoolbox")
//                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                .done();

//        System.out.println(path.split("\\.")[0]+path.split("\\.")[1]+"."+format);
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
// Run a one-pass encode
//        executor.createJob(builder, new ProgressListener() {
//
//            // Using the FFmpegProbeResult determine the duration of the input
//            final double duration_ns = probe.getFormat().duration * TimeUnit.SECONDS.toNanos(1);
//
//            @Override
//            public void progress(Progress progress) {
//                double percentage = progress.out_time_ns / duration_ns;
//
//                // Print out interesting information about the progress
//                System.out.println(String.format(
//                        "[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
//                        percentage * 100,
//                        progress.status,
//                        progress.frame,
//                        FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
//                        progress.fps.doubleValue(),
//                        progress.speed
//                ));
//            }
//        }).run();
        executor.createJob(builder).run();
// Or run a two-pass encode (which is better quality at the cost of being slower)
//        executor.createTwoPassJob(builder).run();

    }

    public static void main(String[] args) {
        Flv2Mp4Converter flv2Mp4Converter = new Flv2Mp4Converter();
        try {
            flv2Mp4Converter.convert("/Users/ztx/Downloads/lvs/BilibiliLiveRecorder/download/1.flv","mp4",0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
