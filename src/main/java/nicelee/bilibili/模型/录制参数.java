package nicelee.bilibili.模型;

import nicelee.bilibili.annotations.Option;
import nicelee.bilibili.live.check.TagOptions;
import nicelee.bilibili.util.Logger;
import nicelee.bilibili.util.TrustAllCertSSLUtil;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class 录制参数 {
    @Option(name = "check", defaultValue = "true")
    public boolean autoCheck;

    @Option(name = "splitScriptTags", defaultValue = "false")
    public boolean splitScriptTagsIfCheck;

    @Option(name = "splitAVHeaderTags", followField = "splitScriptTagsIfCheck", defaultValue = "")
    public Boolean splitAVHeaderTagsIfCheck;

    @Option(name = "delete", defaultValue = "true")
    public boolean deleteOnchecked;

    @Option(name = "checkWithBuffer", defaultValue = "true")
    public boolean flvCheckWithBuffer;

    @Option(name = "zip", defaultValue = "false")
    public boolean flagZip;

    @Option(name = "plugin", defaultValue = "false")
    public boolean flagPlugin;

    @Option(name = "liver", defaultValue = "bili")
    public String liver;

    @Option(name = "format", defaultValue = "")
    public String outputFormat;

    @Option(name = "id", defaultValue = "")
    public String shortId;

    @Option(name = "qn", defaultValue = "")
    public String qn;

    @Option(name = "qnPri", defaultValue = "")
    public String[] qnPriority;

    @Option(name = "retry", defaultValue = "5")
    public int maxFailCnt;
    public int failCnt = 0;

    @Option(name = "retryIfLiveOff", defaultValue = "false")
    public boolean retryIfLiveOff;

    @Option(name = "maxRetryIfLiveOff", defaultValue = "0")
    public int maxRetryIfLiveOff;

    @Option(name = "retryAfterMinutes", defaultValue = "5")
    public double retryAfterMinutes;

    @Option(name = "failRetryAfterMinutes", defaultValue = "1")
    public double failRetryAfterMinutes;

    @Option(name = "fileSize", defaultValue = "0")
    public long splitFileSize = 1024 * 1024;

    @Option(name = "filePeriod", defaultValue = "0")
    public long splitRecordPeriod = 60 * 1000;

    @Option(name = "stopAfterOffline", defaultValue = "true")
    public volatile boolean flagStopAfterOffline;
    public volatile boolean flagSplit = false;

    @Option(name = "fileName", defaultValue = "{name}-{shortId} 的{liver}直播{startTime}-{seq}")
    public String fileName;

    @Option(name = "timeFormat", defaultValue = "yyyy-MM-dd HH.mm")
    public String timeFormat;

    @Option(name = "saveFolder", defaultValue = "")
    public String saveFolder;

    @Option(name = "saveFolderAfterCheck", defaultValue = "")
    public String saveFolderAfterCheck;

    public 录制参数(String[] args) {
        // 根据参数初始化值
        if (args != null && args.length >= 1) {
            // 遍历field
            for (Field field : 录制参数.class.getDeclaredFields()) {
                // 只给有注解的赋值
                Option opts = field.getAnnotation(Option.class);
                if (opts != null) {
                    String value = getValue(args[0], opts.name());
                    // 如为null, 再取默认值
                    if (value == null)
                        value = opts.defaultValue();
                    if (!value.isEmpty()) {
                        setValue(field, value);
                    }
                }
            }
            //再遍历一次，将属性为null的，且followField不为空的赋值
            for (Field field : 录制参数.class.getDeclaredFields()) {
                Option opts = field.getAnnotation(Option.class);
                try {
                    if (field.get(this) == null && opts != null && !opts.followField().isEmpty()) {
                        Object value = 录制参数.class.getDeclaredField(opts.followField()).get(this);
                        field.set(this, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 后续补充措施
            String value = getValue(args[0], "proxy"); // http(s)代理 e.g. 127.0.0.1:8888
            if (value != null && !value.isEmpty()) {
                String argus[] = value.split(":");
                System.setProperty("proxyHost", argus[0]);
                System.setProperty("proxyPort", argus[1]);
            }
            value = getValue(args[0], "socksProxy"); // socks代理 e.g. 127.0.0.1:1080
            if (value != null && !value.isEmpty()) {
                String argus[] = value.split(":");
                System.setProperty("socksProxyHost", argus[0]);
                System.setProperty("socksProxyPort", argus[1]);
            }
            value = getValue(args[0], "trustAllCert"); // 信任所有SSL证书
            if (value != null && !value.isEmpty()) {
                if ("true".equals(value)) {
                    try {
                        HttpsURLConnection.setDefaultSSLSocketFactory(TrustAllCertSSLUtil.getFactory());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            value = getValue(args[0], "maxAudioHeaderSize");
            if (value != null)
                TagOptions.maxAudioHeaderSize = Integer.parseInt(value);
            value = getValue(args[0], "maxVideoHeaderSize");
            if (value != null)
                TagOptions.maxVideoHeaderSize = Integer.parseInt(value);
            value = getValue(args[0], "debug");
            if ("true".equals(value))
                Logger.debug = true;
            else
                Logger.debug = false;
        }
    }

    /**
     * @param field
     * @param value
     */
    private void setValue(Field field, String value) {
        try {
            if (field.getType().equals(String.class)) {
                field.set(this, value);
            } else if (field.getType().equals(int.class)) {
                field.set(this, Integer.parseInt(value));
            } else if (field.getType().equals(long.class)) {
                Long obj = (Long) field.get(this);
                field.set(this, obj * Long.parseLong(value));
            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class) ) {
                field.set(this, "true".equals(value));
            } else if (field.getType().equals(double.class)) {
                field.set(this, Double.parseDouble(value));
            } else if (field.getType().equals(String[].class)) {
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                field.set(this, value.split(">"));
            } else {
                System.err.println("未知类型：" + field.getType());
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    JSONObject json = null;
    boolean jsonParsed = false;
    String charset = null;
    /**
     * 从配置文件 + 参数字符串中取出值 "key1=value1&key2=value2 ..."
     *
     * @param param
     * @param key
     */
    public String getValue(String param, String key) {
        // 先获取配置文件
        if (!jsonParsed) {
            jsonParsed = true;
            String fileStr = getValueFromParam(param, "options");
            if (fileStr == null)
                fileStr = "config.json";
            File configFile = new File(fileStr);
            if (configFile.exists()) {
                charset = getValueFromParam(param, "charset");
                if (charset == null)
                    charset = "UTF-8";
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(configFile), charset));
                    StringBuilder sb = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        sb.append(line).append("\r\n");
                        line = reader.readLine();
                    }
                    reader.close();
                    json = new JSONObject(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 先从args中取值
        String value = getValueFromParam(param, key);
        // 如为null, 再从json文件中取值
        if (value == null && json != null)
            value = json.optString(key, null);
        return value;
    }

    private String getValueFromParam(String param, String key) {
        Pattern pattern = Pattern.compile(key + "=([^&]*)");
        Matcher matcher = pattern.matcher(param);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
