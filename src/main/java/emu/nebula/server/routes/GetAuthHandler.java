package emu.nebula.server.routes;

import org.jetbrains.annotations.NotNull;

import emu.nebula.Nebula;
import emu.nebula.game.account.Account;
import emu.nebula.game.account.AccountHelper;
import emu.nebula.util.JsonUtils;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;

@Getter
public class GetAuthHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        // Parse request
        var req = JsonUtils.decode(ctx.body(), GetAuthRequestJson.class);
        
        if (req == null || req.Account == null || req.Account.isEmpty() || req.Code == null) {
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result("{\"Code\":100600,\"Data\":{},\"Msg\":\"Error\"}"); // PARAM_IS_EMPTY
            return;
        }
        
        // Get account
        Account account = AccountHelper.getAccountByEmail(req.Account);
        
        if (account == null) {
            // Create an account if were allowed to
            if (Nebula.getConfig().getServerOptions().isAutoCreateAccount()) {
                account = AccountHelper.createAccount(req.Account, null, 0);
            }
        } else {
            // Check passcode sent by email
            if (!account.verifyCode(req.Code)) {
                account = null;
            }
        }
        
        // Sanity
        if (account == null) {
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result("{\"Code\":100403,\"Data\":{},\"Msg\":\"Error\"}"); // TOKEN_AUTH_FAILED
            return;
        }
        
        // Build request
        var response = new GetAuthResponseJson();
        
        response.Code = 200;
        response.Msg = "OK";
        response.Data = new GetAuthResponseJson.GetAuthDataJson();
        response.Data.UID = account.getEmail();
        response.Data.Token = account.generateLoginToken();
        response.Data.Account = account.getEmail();
        
        // Result
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.result(JsonUtils.encode(response));
    }
    
    private static class GetAuthRequestJson {
        public String Account;
        public String Code;
    }
    
    @SuppressWarnings("unused")
    private static class GetAuthResponseJson {
        public int Code;
        public GetAuthDataJson Data;
        public String Msg;
        
        private static class GetAuthDataJson {
            public String UID;
            public String Token;
            public String Account;
        }
    }
    
}
