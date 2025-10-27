package emu.nebula.server;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import emu.nebula.Config.HttpServerConfig;
import emu.nebula.Nebula;
import emu.nebula.Nebula.ServerType;
import emu.nebula.server.routes.*;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import lombok.Getter;

@Getter
public class HttpServer {
    private final Javalin app;
    private ServerType type;
    private boolean started;
    
    public HttpServer(ServerType type) {
        this.app = Javalin.create();
        this.type = type;

        this.addRoutes();
    }
    
    public HttpServerConfig getServerConfig() {
        return Nebula.getConfig().getHttpServer();
    }

    private HttpConnectionFactory getHttpFactory() {
        HttpConfiguration httpsConfig = new HttpConfiguration();
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setSniHostCheck(false);
        httpsConfig.addCustomizer(src);
        return new HttpConnectionFactory(httpsConfig);
    }

    private SslContextFactory.Server getSSLContextFactory() {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(Nebula.getConfig().getKeystore().getPath());
        sslContextFactory.setKeyStorePassword(Nebula.getConfig().getKeystore().getPassword());
        sslContextFactory.setSniRequired(false);
        sslContextFactory.setRenegotiationAllowed(false);
        return sslContextFactory;
    }
    
    // Start server
    
    public void start() {
        if (this.started) return;
        this.started = true;

        // Http server
        if (getServerConfig().isUseSSL()) {
            ServerConnector sslConnector = new ServerConnector(getApp().jettyServer().server(), getSSLContextFactory(), getHttpFactory());
            sslConnector.setHost(getServerConfig().getBindAddress());
            sslConnector.setPort(getServerConfig().getBindPort());
            getApp().jettyServer().server().addConnector(sslConnector);

            getApp().start();
        } else {
            getApp().start(getServerConfig().getBindAddress(), getServerConfig().getBindPort());
        }

        // Done
        Nebula.getLogger().info("Http Server started on " + getServerConfig().getBindPort());
    }
    
    // Server endpoints

    private void addRoutes() {
        // Add routes
        if (this.getType().runLogin()) {
            this.addLoginServerRoutes();
        }
        
        if (this.getType().runGame()) {
            this.addGameServerRoutes();
        }
        
        // Exception handler
        getApp().exception(Exception.class, (e, _) -> {
            e.printStackTrace();
        });

        // Fallback handler
        getApp().error(404, this::notFoundHandler);
    }
    
    private void addLoginServerRoutes() {
        // https://en-sdk-api.yostarplat.com/
        getApp().post("/common/config", new CommonConfigHandler(this));
        getApp().post("/common/version", new HttpJsonResponse("{\"Code\":200,\"Data\":{\"Agreement\":[{\"Version\":\"0.1\",\"Type\":\"user_agreement\",\"Title\":\"用户协议\",\"Content\":\"\",\"Lang\":\"en\"},{\"Version\":\"0.1\",\"Type\":\"privacy_agreement\",\"Title\":\"隐私政策\",\"Content\":\"\",\"Lang\":\"en\"}],\"ErrorCode\":\"4.4\"},\"Msg\":\"OK\"}"));
        
        getApp().post("/user/detail", new UserLoginHandler());
        getApp().post("/user/set", new UserSetDataHandler());
        getApp().post("/user/login", new UserLoginHandler());
        getApp().post("/user/quick-login", new UserLoginHandler());
        
        getApp().post("/yostar/get-auth", new GetAuthHandler());
        getApp().post("/yostar/send-code", new HttpJsonResponse("{\"Code\":200,\"Data\":{},\"Msg\":\"OK\"}")); // Dummy handler
        
        // https://nova-static.stellasora.global/
        getApp().get("/meta/serverlist.html", new MetaServerlistHandler(this));
        getApp().get("/meta/win.html", new MetaWinHandler());
    }
    
    private void addGameServerRoutes() {
        getApp().post("/agent-zone-1/", new AgentZoneHandler());
    }
    
    private void notFoundHandler(Context ctx) {
        ctx.status(404);
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.result("{}");
    }
}
