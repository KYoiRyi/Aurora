package emu.nebula.server.routes;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

import emu.nebula.game.account.Account;
import emu.nebula.game.account.AccountHelper;
import emu.nebula.server.routes.UserLoginEntity.LoginYostarJson;
import emu.nebula.server.routes.UserLoginEntity.UserDetailJson;
import emu.nebula.server.routes.UserLoginEntity.UserInfoJson;
import emu.nebula.server.routes.UserLoginEntity.UserKeyJson;
import emu.nebula.util.JsonUtils;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;

@Getter
public class UserLoginHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        // Get account from header first
        Account account = this.getAccountFromHeader(ctx);
        
        // Check req body for account details
        if (account == null) {
            account = this.getAccountFromBody(ctx);
        }
        
        // Check
        if (account == null) {
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result("{\"Code\":100403,\"Data\":{},\"Msg\":\"Error\"}"); // TOKEN_AUTH_FAILED
            return;
        }
        
        // Create response
        var response = new UserLoginEntity();
        
        response.Code = 200;
        response.Msg = "OK";
        response.Data = new UserDetailJson();
        response.Data.Keys = new ArrayList<>();
        response.Data.UserInfo = new UserInfoJson();
        response.Data.Yostar = new LoginYostarJson();
        
        response.Data.UserInfo.ID = account.getUid();
        response.Data.UserInfo.UID2 = 0;
        response.Data.UserInfo.PID = "NEBULA";
        response.Data.UserInfo.Token = account.getLoginToken();
        response.Data.UserInfo.Birthday = "";
        response.Data.UserInfo.RegChannel = "pc";
        response.Data.UserInfo.TransCode = "";
        response.Data.UserInfo.State = 1;
        response.Data.UserInfo.DeviceID = "";
        response.Data.UserInfo.CreatedAt = account.getCreatedAt();
        
        response.Data.Yostar.ID = account.getUid();
        response.Data.Yostar.Country = "US";
        response.Data.Yostar.Nickname = account.getNickname();
        response.Data.Yostar.Picture = account.getPicture();
        response.Data.Yostar.State = 1;
        response.Data.Yostar.AgreeAd = 0;
        response.Data.Yostar.CreatedAt = account.getCreatedAt();
        
        var key = new UserKeyJson();
        key.ID = account.getUid();
        key.Type = "yostar";
        key.Key = account.getEmail();
        key.NickName = account.getEmail();
        key.CreatedAt = account.getCreatedAt();
        
        response.Data.Keys.add(key);
        
        // Result
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.result(JsonUtils.encode(response, true));
    }
    
    protected Account getAccountFromBody(Context ctx) {
        // Parse request
        var req = JsonUtils.decode(ctx.body(), UserLoginRequestJson.class);
        
        if (req == null || req.OpenID == null || req.Token == null) {
            return null;
        }
        
        // Get account
        return AccountHelper.getAccountByLoginToken(req.Token); 
    }
    
    protected Account getAccountFromHeader(Context ctx) {
        // Parse request
        var req = JsonUtils.decode(ctx.header("Authorization"), UserAuthDataJson.class);
        
        if (req == null || req.Head == null || req.Head.Token == null) {
            return null;
        }
        
        // Get account
        return AccountHelper.getAccountByLoginToken(req.Head.Token); 
    }
    
    @SuppressWarnings("unused")
    private static class UserLoginRequestJson {
        public String OpenID;
        public String Token;
        public String Type;
        public String UserName;
        public String Secret;
        public int CheckAccount;
    }
    
    @SuppressWarnings("unused")
    private static class UserAuthDataJson {
        public UserAuthHeadJson Head;
        public String Sign;
        
        protected static class UserAuthHeadJson {
            public String UID;
            public String Token;
        }
    }
    
}
