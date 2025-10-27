package emu.nebula.server.routes;

import org.jetbrains.annotations.NotNull;

import emu.nebula.server.HttpServer;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;

@Getter
public class CommonConfigHandler implements Handler {
    private HttpServer server;
    private String json;

    public CommonConfigHandler(HttpServer server) {
        this.server = server;
        this.json = "{\"Code\":200,\"Data\":{\"AppConfig\":{\"ACCOUNT_RETRIEVAL\":{\"FIRST_LOGIN_POPUP\":false,\"LOGIN_POPUP\":false,\"PAGE_URL\":\"\"},\"AGREEMENT_POPUP_TYPE\":\"Browser\",\"APPLE_CURRENCY_BLOCK_LIST\":null,\"APPLE_TYPE_KEY\":\"apple_hk\",\"APP_CLIENT_LANG\":[\"en\"],\"APP_DEBUG\":0,\"APP_GL\":\"en\",\"BIND_METHOD\":[\"google\",\"apple_hk\",\"facebook\"],\"CAPTCHA_ENABLED\":false,\"CLIENT_LOG_REPORTING\":{\"ENABLE\":true},\"CREDIT_INVESTIGATION\":\"0.0\",\"DESTROY_USER_DAYS\":15,\"DESTROY_USER_ENABLE\":1,\"DETECTION_ADDRESS\":{\"AUTO\":{\"DNS\":[\"${url}\",\"${url}\",\"${url}/meta/serverlist.html\"],\"HTTP\":[\"${url}\",\"${url}\",\"${url}\"],\"MTR\":[\"${url}\",\"${url}\",\"${url}/meta/serverlist.html\"],\"PING\":[\"${url}\",\"${url}\",\"${url}/meta/serverlist.html\"],\"TCP\":[\"${url}\",\"${url}\",\"${url}/meta/serverlist.html\"]},\"ENABLE\":true,\"ENABLE_MANUAL\":true,\"INTERNET\":\"https://www.google.com\",\"INTERNET_ADDRESS\":\"https://www.google.com\",\"NETWORK_ENDPORINT\":\"\",\"NETWORK_PROJECT\":\"\",\"NETWORK_SECRET_KEY\":\"\"},\"ENABLE_AGREEMENT\":true,\"ENABLE_MULTI_LANG_AGREEMENT\":false,\"ENABLE_TEXT_REVIEW\":true,\"ERROR_CODE\":\"4.4\",\"FILE_DOMAIN\":\"\",\"GEETEST_ENABLE\":false,\"GEETEST_ID\":\"\",\"GOOGLE_ANALYTICS_MEASUREMENT_ID\":\"\",\"MIGRATE_POPUP\":true,\"NICKNAME_REG\":\"^[A-Za-z0-9]{2,20}$\",\"POPUP\":{\"Data\":[{\"Lang\":\"ja\",\"Text\":\"YostarIDを作成\"},{\"Lang\":\"en\",\"Text\":\"CreateaYostaraccount\"},{\"Lang\":\"kr\",\"Text\":\"YOSTAR계정가입하기\"},{\"Lang\":\"fr\",\"Text\":\"CréezvotrecompteYostar\"},{\"Lang\":\"de\",\"Text\":\"EinenYostar-Accounterstellen\"}],\"Enable\":true},\"PRIVACY_AGREEMENT\":\"0.1\",\"RECHARGE_LIMIT\":{\"Enable\":false,\"IsOneLimit\":false,\"Items\":[],\"OneLimitAmount\":0},\"SHARE\":{\"CaptureScreen\":{\"AutoCloseDelay\":0,\"Enabled\":false},\"Facebook\":{\"AppID\":\"\",\"Enabled\":false},\"Instagram\":{\"Enabled\":false},\"Kakao\":{\"AppKey\":\"\",\"Enabled\":false},\"Naver\":{\"Enabled\":false},\"Twitter\":{\"Enabled\":false}},\"SLS\":{\"ACCESS_KEY_ID\":\"7b5d0ffd0943f26704fc547a871c68b1b5d56b5c9caeb354205b81f445d7af59\",\"ACCESS_KEY_SECRET\":\"4a5e9cc8a50819290c9bfa1fedc79da7c50e85189a05eb462a3d28a7688eabb0\",\"ENABLE\":false},\"SURVEY_POPUP_TYPE\":\"Browser\",\"UDATA\":{\"Enable\":false,\"URL\":\"${url}\"},\"USER_AGREEMENT\":\"0.1\",\"YOSTAR_PREFIX\":\"yoyo\"},\"EuropeUnion\":false,\"StoreConfig\":{\"ADJUST_APPID\":\"\",\"ADJUST_CHARGEEVENTTOKEN\":\"\",\"ADJUST_ENABLED\":0,\"ADJUST_EVENTTOKENS\":null,\"ADJUST_ISDEBUG\":0,\"AIRWALLEX_ENABLED\":false,\"AI_HELP\":{\"AihelpAppID\":\"yostar1_platform_2db52a57068b1ee3fe3652c8b53d581b\",\"AihelpAppKey\":\"YOSTAR1_app_bc226f4419a7447c9de95711f8a2d3d9\",\"AihelpDomain\":\"yostar1.aihelp.net\",\"CustomerServiceURL\":\"\",\"CustomerWay\":1,\"DisplayType\":\"Browser\",\"Enable\":1,\"Mode\":\"robot\"},\"APPLEID\":\"\",\"CODA_ENABLED\":false,\"ENABLED_PAY\":{\"AIRWALLEX_ENABLED\":false,\"CODA_ENABLED\":false,\"GMOAlipay\":false,\"GMOAu\":false,\"GMOCreditcard\":false,\"GMOCvs\":false,\"GMODocomo\":false,\"GMOPaypal\":false,\"GMOPaypay\":false,\"GMOSoftbank\":false,\"MYCARD_ENABLED\":false,\"PAYPAL_ENABLED\":true,\"RAZER_ENABLED\":false,\"STEAM_ENABLED\":false,\"STRIPE_ENABLED\":true,\"TOSS_ENABLED\":false,\"WEBMONEY_ENABLED\":false},\"FACEBOOK_APPID\":\"\",\"FACEBOOK_CLIENT_TOKEN\":\"\",\"FACEBOOK_SECRET\":\"\",\"FIREBASE_ENABLED\":0,\"GMO_CC_JS\":\"https://\",\"GMO_CC_KEY\":\"\",\"GMO_CC_SHOPID\":\"\",\"GMO_PAY_CHANNEL\":{\"GMOAlipay\":false,\"GMOAu\":false,\"GMOCreditcard\":false,\"GMOCvs\":false,\"GMODocomo\":false,\"GMOPaypal\":false,\"GMOPaypay\":false,\"GMOSoftbank\":false},\"GMO_PAY_ENABLED\":false,\"GOOGLE_CLIENT_ID\":\"\",\"GOOGLE_CLIENT_SECRET\":\"\",\"GUEST_CREATE_METHOD\":0,\"GUIDE_POPUP\":{\"DATA\":null,\"ENABLE\":0},\"LOGIN\":{\"DEFAULT\":\"yostar\",\"ICON_SIZE\":\"big\",\"SORT\":[\"google\",\"apple\",\"device\"]},\"MYCARD_ENABLED\":false,\"ONE_STORE_LICENSE_KEY\":\"\",\"PAYPAL_ENABLED\":false,\"RAZER_ENABLED\":false,\"REMOTE_CONFIG\":[],\"SAMSUNG_SANDBOX_MODE\":false,\"STEAM_APPID\":\"\",\"STEAM_ENABLED\":false,\"STEAM_PAY_APPID\":\"\",\"STRIPE_ENABLED\":false,\"TOSS_ENABLED\":false,\"TWITTER_KEY\":\"\",\"TWITTER_SECRET\":\"\",\"WEBMONEY_ENABLED\":false}},\"Msg\":\"OK\"}";
        
        String address = server.getServerConfig().getDisplayAddress();
        this.json = this.json.replaceAll("\\$\\{url}", address);
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.result(this.json);
    }

}
