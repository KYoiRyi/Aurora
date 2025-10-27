package emu.nebula.game.mail;

import java.util.concurrent.TimeUnit;

import dev.morphia.annotations.Entity;
import emu.nebula.Nebula;
import emu.nebula.game.inventory.ItemParamMap;
import emu.nebula.proto.Public.Mail;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity(useDiscriminator = false)
public class GameMail {
    private int id;
    
    private String author;
    private String subject;
    private String desc;
    
    private ItemParamMap attachments;
    
    @Setter private boolean read;
    @Setter private boolean recv;
    @Setter private boolean pin;
    
    private long flag;
    private long time;
    private long expiry;
    
    @Deprecated // Morphia only
    public GameMail() {
        
    }
    
    public GameMail(String author, String subject, String desc) {
        this.author = author;
        this.subject = subject;
        this.desc = desc;
        this.time = Nebula.getCurrentTime();
        this.expiry = this.time + TimeUnit.DAYS.toSeconds(30);
    }
    
    protected void setId(int id) {
        if (this.id == 0) {
            this.id = id;
        }
    }

    public boolean canRemove() {
        return (this.isRead() || this.isRecv()) && !this.isPin() && (this.hasAttachments() && this.isRecv());
    }

    public boolean hasAttachments() {
        return this.attachments != null;
    }
    
    public void addAttachment(int itemId, int count) {
        if (this.attachments == null) {
            this.attachments = new ItemParamMap();
        }
        
        this.attachments.add(itemId, count);
    }
    
    public Mail toProto() {
        var proto = Mail.newInstance()
                .setId(this.getId())
                .setAuthor(this.getAuthor())
                .setSubject(this.getSubject())
                .setDesc(this.getDesc())
                .setTime(this.getTime())
                .setRead(this.isRead())
                .setRecv(this.isRecv())
                .setPin(this.isPin())
                .setFlag(this.getFlag())
                .setDeadline(this.getExpiry());
        
        if (this.getAttachments() != null) {
            this.getAttachments().itemTemplateStream()
                .forEach(proto::addAttachments);
        }
        
        return proto;
    }
}
