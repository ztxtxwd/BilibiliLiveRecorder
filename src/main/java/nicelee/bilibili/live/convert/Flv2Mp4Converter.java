package nicelee.bilibili.live.convert;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.IOException;

public class Flv2Mp4Converter {
    public void convert(String path,String format,int count) throws IOException {
        FFmpeg ffmpeg = new FFmpeg("ffmpeg");
        FFprobe ffprobe = new FFprobe("ffprobe");
        path = path.replaceFirst(".flv$", "-checked" + count + ".flv");
        FFmpegProbeResult probe = ffprobe.probe(path);
        System.out.println(probe.getFormat().bit_rate);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(path)     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput(path.split("\\.")[0]+path.split("\\.")[1]+"."+format)   // Filename for the destination
                .setFormat(format)        // Format is inferred from filename, or can be set
                .setVideoBitRate(probe.getFormat().bit_rate)
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                .done();
        System.out.println(path.split("\\.")[0]+path.split("\\.")[1]+"."+format);
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

// Run a one-pass encode
//        executor.createJob(builder).run();

// Or run a two-pass encode (which is better quality at the cost of being slower)
        executor.createTwoPassJob(builder).run();


    }
}
