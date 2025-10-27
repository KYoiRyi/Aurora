package emu.nebula.game.account;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import emu.nebula.Nebula;
import emu.nebula.database.AccountDatabaseOnly;
import emu.nebula.util.Snowflake;
import lombok.Getter;

@Getter
@AccountDatabaseOnly
@Entity(value = "accounts", useDiscriminator = false)
public class Account {
    @Id private String uid;
    
    @Indexed
    private String email;
    private String code;
    
    private String nickname;
    private String picture;
    
    @Indexed private String loginToken;
    @Indexed private String gameToken;
    
    private Set<String> permissions;
    
    private int reservedPlayerUid;
    private long createdAt;
    
    @Deprecated
    public Account() {
        // Morphia only
    }
    
    public Account(String email, String password, int reservedUid) {
        this.uid = Long.toString(Snowflake.newUid());
        this.email = email;
        this.nickname = "";
        this.picture = "";
        this.permissions = new HashSet<>();
        this.reservedPlayerUid = reservedUid;
        this.createdAt = System.currentTimeMillis() / 1000;
    }
    
    public boolean verifyCode(String code) {
        // TODO
        return true;
    }
    
    public void setNickname(String value) {
        this.nickname = value;
    }
    
    // Tokens

    public String generateLoginToken() {
        this.loginToken = AccountHelper.createSessionKey(this.getUid());
        this.save();
        return this.loginToken;
    }

    public String generateGameToken() {
        this.gameToken = AccountHelper.createSessionKey(this.getUid());
        this.save();
        return this.gameToken;
    }
    
    // Permissions
    
    public Set<String> getPermissions() {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
            this.save();
        }
        return this.permissions;
    }
    
    public boolean addPermission(String permission) {
        if (this.getPermissions().contains(permission)) {
            return false;
        }
        this.getPermissions().add(permission); 
        this.save();
        return true;
    }

    public static boolean permissionMatchesWildcard(String wildcard, String[] permissionParts) {
        String[] wildcardParts = wildcard.split("\\.");
        if (permissionParts.length < wildcardParts.length) {  // A longer wildcard can never match a shorter permission
            return false;
        }
        
        for (int i = 0; i < wildcardParts.length; i++) {
            switch (wildcardParts[i]) {
                case "**":  // Recursing match
                    return true;
                case "*":  // Match only one layer
                    if (i >= (permissionParts.length-1)) {
                        return true;
                    }
                    break;
                default:  // This layer isn't a wildcard, it needs to match exactly
                    if (!wildcardParts[i].equals(permissionParts[i])) {
                        return false;
                    }
            }
        }
        // At this point the wildcard will have matched every layer, but if it is shorter then the permission then this is not a match at this point (no **).
        return wildcardParts.length == permissionParts.length;
    }

    public boolean hasPermission(String permission) {
        // Skip if permission isnt required
        if (permission.isEmpty()) {
            return true;
        }
        
        // Default permissions
        var defaultPermissions = Nebula.getConfig().getServerOptions().getDefaultPermissions();
        
        if (defaultPermissions.contains("*")) {
            return true;
        }

        // Add default permissions if it doesn't exist
        List<String> permissions = Stream.of(this.getPermissions(), defaultPermissions)
                .flatMap(Collection::stream)
                .distinct().toList();

        if (permissions.contains(permission)) {
            return true;
        }

        String[] permissionParts = permission.split("\\.");
        for (String p : permissions) {
            if (p.startsWith("-") && permissionMatchesWildcard(p.substring(1), permissionParts)) return false;
            if (permissionMatchesWildcard(p, permissionParts)) return true;
        }

        return permissions.contains("*");
    }

    public boolean removePermission(String permission) {
        boolean res = this.getPermissions().remove(permission);
        if (res) this.save();
        return res;
    }

    public void clearPermission() {
        this.getPermissions().clear();
        this.save();
    }
    
    // Database

    public void save() {
        Nebula.getAccountDatabase().save(this);
    }
}
