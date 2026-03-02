package com.example;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Random;

public class WeChatPush {
    // ==================== 【完全保留你的常量】 ====================
    private static final String GIRLFRIEND_NAME = "刘雨嫣";
    private static final int NANJING_CITY_ID = 1806260;
    private static final LocalDate LOVE_START_DATE = LocalDate.of(2025, 11, 6);
    private static final LocalDate GIRL_BIRTHDAY = LocalDate.of(2026, 9, 20); // 【新增】她的生日，记得改月/日
    // ==============================================================

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
    // 【修改】模板消息接口URL
    private static final String SEND_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + NANJING_CITY_ID + "&units=metric&lang=zh_cn&appid=";
    private static final Gson GSON = new Gson();
    private static final Random RANDOM = new Random();

    // 【完全保留你的每日寄语库】
    private static final String[][] QUOTES = {
        {"It's not the travelling. It's the arriving that matters.", "不管路途如何，重要的是抵达目的地。"},
        {"The best thing to hold onto in life is each other.", "生命里最值得抱紧的，是彼此。"},
        {"Every love story is beautiful, but ours is my favorite.", "每段爱情都很美，但我最爱我们的。"}
    };

    public static void main(String[] args) {
        System.out.println("=== 微信推送程序启动 ===");

        // 读取环境变量
        String appId = System.getenv("WECHAT_APPID");
        String appSecret = System.getenv("WECHAT_APPSECRET");
        String openId = System.getenv("WECHAT_OPENID");
        String weatherKey = System.getenv("OPENWEATHER_API_KEY");
        String templateId = System.getenv("WECHAT_TEMPLATE_ID"); // 【新增】读取模板ID

        // 微信核心配置判空
        if (appId == null || appId.trim().isEmpty()
                || appSecret == null || appSecret.trim().isEmpty()
                || openId == null || openId.trim().isEmpty()
                || templateId == null || templateId.trim().isEmpty()) {
            System.err.println("错误：微信核心配置或模板ID缺失，程序终止");
            System.exit(1);
        }

        try {
            // 【完全保留你的日期计算】
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE", Locale.CHINA);
            String todayStr = today.format(dateFormatter);
            long loveDays = ChronoUnit.DAYS.between(LOVE_START_DATE, today);
            
            // 【新增】计算距离她生日的天数
            LocalDate nextBirthday = GIRL_BIRTHDAY.withYear(today.getYear());
            if (nextBirthday.isBefore(today)) {
                nextBirthday = nextBirthday.plusYears(1);
            }
            long daysToBirthday = ChronoUnit.DAYS.between(today, nextBirthday);

            System.out.println("📅 今天日期：" + todayStr);
            System.out.println("⏳ 在一起：" + loveDays + "天");
            System.out.println("🎂 距离生日：" + daysToBirthday + "天");
            
            // 【完全保留】获取微信access_token
            String accessToken = getAccessToken(appId, appSecret);
            System.out.println("✅ 获取access_token成功");

            // 【完全保留】获取南京天气
            String weatherDesc = "未知";
            String temp = "未知";
            String humidity = "未知";
            String windSpeed = "未知";
            String suggest = "今天也要开心哦";

            if (weatherKey != null && !weatherKey.trim().isEmpty()) {
                try {
                    JsonObject weatherData = getWeatherData(weatherKey);
                    JsonObject main = weatherData.getAsJsonObject("main");
                    JsonObject weather = weatherData.getAsJsonArray("weather").get(0).getAsJsonObject();
                    JsonObject wind = weatherData.getAsJsonObject("wind");

                    weatherDesc = weather.get("description").getAsString();
                    temp = main.get("temp").getAsString() + "℃（体感" + main.get("feels_like").getAsString() + "℃）";
                    humidity = main.get("humidity").getAsString() + "%";
                    windSpeed = wind.get("speed").getAsString() + "m/s";
                    suggest = getClothingAdvice(main.get("temp").getAsDouble());
                    
                    System.out.println("✅ 天气获取成功：" + weatherDesc + " " + temp);
                } catch (Exception e) {
                    System.err.println("⚠️  天气获取失败：" + e.getMessage());
                }
            }

            // 【完全保留】随机获取每日一句
            String[] quote = QUOTES[RANDOM.nextInt(QUOTES.length)];
            String enQuote = quote[0];
            String cnQuote = quote[1];

            // 【仅修改这里】发送模板消息，带彩色文字
            String result = sendTemplateMessage(accessToken, openId, templateId, 
                    todayStr, "南京", weatherDesc, temp, humidity, windSpeed, 
                    String.valueOf(loveDays), String.valueOf(daysToBirthday), 
                    suggest, enQuote, cnQuote);
            
            System.out.println("✅ 微信接口响应：" + result);
            System.out.println("=== 推送执行完成 ===");

        } catch (Exception e) {
            System.err.println("❌ 程序执行失败：");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 【完全保留】天气数据获取
     */
    private static JsonObject getWeatherData(String weatherKey) throws Exception {
        String url = WEATHER_URL + weatherKey;
        System.out.println("🌤️  正在请求OpenWeather API...");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("User-Agent", "Mozilla/5.0");

            try (CloseableHttpResponse response = client.execute(get)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    String errorBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    throw new RuntimeException("API状态码异常：" + statusCode);
                }
                String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                return GSON.fromJson(body, JsonObject.class);
            }
        }
    }

    /**
     * 【新增】穿衣建议
     */
    private static String getClothingAdvice(double temp) {
        if (temp < 6) return "天气很冷，一定要穿厚外套，注意保暖！";
        else if (temp < 14) return "有点凉，穿风衣/卫衣加外套正合适。";
        else if (temp < 22) return "温度舒适，穿长袖刚刚好。";
        else return "天气暖和，可以穿得清爽一点，记得防晒哦！";
    }

    /**
     * 【完全保留】获取Token
     */
    private static String getAccessToken(String appId, String secret) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse res = client.execute(new HttpGet(TOKEN_URL + "&appid=" + appId + "&secret=" + secret));
        String body = EntityUtils.toString(res.getEntity());
        return GSON.fromJson(body, JsonObject.class).get("access_token").getAsString();
    }

    /**
     * 【核心修改】发送模板消息，带彩色文字设置
     */
    private static String sendTemplateMessage(String token, String openId, String templateId,
            String date, String city, String weather, String temp, String humidity, String wind,
            String loveDays, String birthdayDays, String suggest, String enQuote, String cnQuote) throws Exception {
        String url = SEND_TEMPLATE_URL + token;
        
        JsonObject msg = new JsonObject();
        msg.addProperty("touser", openId);
        msg.addProperty("template_id", templateId);

        // 设置模板参数和颜色（颜色参考示例图）
        JsonObject data = new JsonObject();
        data.add("date", createParam(date, "#FFD700")); // 金色
        data.add("city", createParam(city, "#1E90FF")); // 蓝色
        data.add("weather", createParam(weather, "#FF6347")); // 红色
        data.add("temp", createParam(temp, "#FF69B4")); // 粉色
        data.add("humidity", createParam(humidity, "#32CD32")); // 绿色
        data.add("wind", createParam(wind, "#9370DB")); // 紫色
        data.add("love_days", createParam(loveDays, "#32CD32")); // 绿色
        data.add("birthday_days", createParam(birthdayDays, "#32CD32")); // 绿色
        data.add("suggest", createParam(suggest, "#1E90FF")); // 蓝色
        data.add("en_quote", createParam(enQuote, "#808080")); // 灰色
        data.add("cn_quote", createParam(cnQuote, "#4169E1")); // 宝蓝色

        msg.add("data", data);

        // 发送请求
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            post.setEntity(new StringEntity(GSON.toJson(msg), "UTF-8"));
            try (CloseableHttpResponse response = client.execute(post)) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        }
    }

    /**
     * 辅助方法：创建带颜色的模板参数
     */
    private static JsonObject createParam(String value, String color) {
        JsonObject param = new JsonObject();
        param.addProperty("value", value);
        param.addProperty("color", color);
        return param;
    }
}
