package emu.nebula.server.routes;

import org.jetbrains.annotations.NotNull;

import emu.nebula.proto.Pb.ClientDiff;
import emu.nebula.util.AeadHelper;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PRIVATE)
public class MetaWinHandler implements Handler {
    private ClientDiff list;
    private byte[] proto;

    public MetaWinHandler() {
        // Create client diff
        this.list = ClientDiff.newInstance();
        
        // TODO load from json or something
        
        // Cache proto
        this.proto = list.toByteArray();
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        // Result
        ctx.contentType(ContentType.APPLICATION_OCTET_STREAM);
        ctx.result(AeadHelper.encryptCBC(this.getProto()));
    }

}
