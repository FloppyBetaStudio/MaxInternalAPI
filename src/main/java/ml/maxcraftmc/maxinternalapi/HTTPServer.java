package ml.maxcraftmc.maxinternalapi;

import io.javalin.Javalin;
import io.javalin.http.servlet.JavalinServletContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPServer {
    private String token;
    private final int port;

    public Javalin app;
    public HTTPServer(int Port, String Token) {
        this.token = Token;
        this.port = Port;
        MaxInternalAPI.log.info("API Token:"+this.token);
        this.start();
    }

    public void start() {
        app = Javalin.create().start(port);

        app.before(ctx ->  { // 认证过滤器
            String auth = ctx.header("Authorization");
            if (auth == null){
                ctx.status(401).result("Unauthorized").status();
                MaxInternalAPI.log.info(ctx.ip()+" "+ctx.method()+" "+ctx.url()+" Error 401:Key empty");
                ((JavalinServletContext)ctx).getTasks().clear();
                return;
            }
            if (!auth.equals(token)) {
                ctx.status(401).result("Unauthorized");
                MaxInternalAPI.log.info(ctx.ip()+" "+ctx.method()+" "+ctx.url()+" Error 401:Key wrong");
                ((JavalinServletContext)ctx).getTasks().clear();
                return;
            }
            MaxInternalAPI.log.info(ctx.ip()+" "+ctx.method()+" "+ctx.url());
        });

        // 根路由
        app.get("/", ctx -> ctx.result("Max is OK"));

        // 用户密码检查
        app.post("/auth/check", ctx -> {
            JSONObject jo = new JSONObject(ctx.body());
            String username = jo.getString("username");
            String password = jo.getString("password");
            // 校验用户名密码
            if (MaxInternalAPI.authmeapi.checkPassword(username, password)) {
                ctx.result("true");
            } else {
                ctx.status(403).result("false");
            }
        });

        // 用户密码修改
        app.post("/auth/changePassword", ctx -> {
            JSONObject jo = new JSONObject(ctx.body());
            String username = jo.getString("username");
            String password = jo.getString("password");
            // 校验用户名密码
            if (MaxInternalAPI.authmeapi.isRegistered(username)) {
                MaxInternalAPI.authmeapi.changePassword(username, password);
                ctx.result("true");
            } else {
                ctx.status(404).result("false");
            }
        });

        // 用户强制登录
        app.post("/auth/forceLogin", ctx -> {
            JSONObject jo = new JSONObject(ctx.body());
            String username = jo.getString("username");
            // 校验用户名
            OfflinePlayer thisPlayer = Bukkit.getOfflinePlayer(username);
            if (thisPlayer.isOnline()) {
                MaxInternalAPI.authmeapi.forceLogin(thisPlayer.getPlayer());
                ctx.result("true");
            } else {
                ctx.status(404).result("false");
            }
        });

        // 在线玩家列表
        app.get("/player/list", ctx -> {
            JSONArray onlinePlayers = new JSONArray();
            Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
            for (Player player : players) {
                onlinePlayers.put(player.getName());
            }
            JSONObject resp = new JSONObject();
            resp.put("onlinePlayers", onlinePlayers);
            ctx.result(resp.toString());
        });

        // 获取玩家法币余额
        app.get("/player/balance/{username}", ctx -> {
            ctx.result(String.valueOf(MaxInternalAPI.econ.getBalance(Bukkit.getOfflinePlayer(ctx.pathParam("username")))));


        });

        // 玩家加钱
        app.put("/player/balance/{username}", ctx -> {
            MaxInternalAPI.econ.depositPlayer(Bukkit.getOfflinePlayer(ctx.pathParam("username")), Double.parseDouble(ctx.body()));
        });

        // 玩家扣钱
        app.delete("/player/balance/{username}", ctx -> {
            MaxInternalAPI.econ.withdrawPlayer(Bukkit.getOfflinePlayer(ctx.pathParam("username")), Double.parseDouble(ctx.body()));
        });

        // 玩家是否已注册
        app.get("/player/isRegistered/{username}", ctx -> {
            if(MaxInternalAPI.authmeapi.isRegistered(ctx.pathParam("username"))){
                ctx.result("true");
            }else{
                ctx.status(404).result("false");
            }
        });
    }

    public void reloadApiToken(String TokenNew){
        this.token = TokenNew;
    }
}