package emu.nebula.game.tower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.morphia.annotations.Entity;
import emu.nebula.data.resources.StarTowerDef;
import emu.nebula.game.formation.Formation;
import emu.nebula.game.player.Player;
import emu.nebula.proto.PublicStarTower.StarTowerChar;
import emu.nebula.proto.PublicStarTower.StarTowerDisc;
import emu.nebula.proto.PublicStarTower.StarTowerInfo;
import emu.nebula.proto.PublicStarTower.StarTowerRoomCase;
import emu.nebula.proto.StarTowerApply.StarTowerApplyReq;
import emu.nebula.proto.StarTowerInteract.StarTowerInteractReq;
import emu.nebula.proto.StarTowerInteract.StarTowerInteractResp;
import emu.nebula.util.Snowflake;
import emu.nebula.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
@Entity(useDiscriminator = false)
public class StarTowerInstance {
    private transient StarTowerManager manager;
    private transient StarTowerDef data;
    
    // Tower id
    private int id;
    
    // Room
    private int floor;
    private int mapId;
    private int mapTableId;
    private String mapParam;
    private int paramId;
    
    // Team
    private int formationId;
    private int buildId;
    private int teamLevel;
    private int teamExp;
    private int charHp;
    private int battleTime;
    private List<StarTowerChar> chars;
    private List<StarTowerDisc> discs;
    
    private int lastCaseId = 0;
    private List<StarTowerCase> cases;
    
    private Int2IntMap items;
    
    @Deprecated // Morphia only
    public StarTowerInstance() {
        
    }
    
    public StarTowerInstance(StarTowerManager manager, StarTowerDef data, Formation formation, StarTowerApplyReq req) {
        this.manager = manager;
        this.data = data;
        
        this.id = req.getId();
        
        this.mapId = req.getMapId();
        this.mapTableId = req.getMapTableId();
        this.mapParam = req.getMapParam();
        this.paramId = req.getParamId();
        
        this.formationId = req.getFormationId();
        this.buildId = Snowflake.newUid();
        this.teamLevel = 1;
        this.floor = 1;
        this.charHp = -1;
        this.chars = new ArrayList<>();
        this.discs = new ArrayList<>();

        this.cases = new ArrayList<>();
        this.items = new Int2IntOpenHashMap();
        
        // Init formation
        for (int i = 0; i < 3; i++) {
            int id = formation.getCharIdAt(i);
            var character = getPlayer().getCharacters().getCharacterById(id);
            
            if (character != null) {
                chars.add(character.toStarTowerProto());
            } else {
                chars.add(StarTowerChar.newInstance());
            }
        }
        
        for (int i = 0; i < 6; i++) {
            int id = formation.getDiscIdAt(i);
            var disc = getPlayer().getCharacters().getDiscById(id);
            
            if (disc != null) {
                discs.add(disc.toStarTowerProto());
            } else {
                discs.add(StarTowerDisc.newInstance());
            }
        }
        
        // Add cases
        this.addCase(new StarTowerCase(CaseType.Battle));
        this.addCase(new StarTowerCase(CaseType.SyncHP));
        
        
        var doorCase = this.addCase(new StarTowerCase(CaseType.OpenDoor));
        doorCase.setFloorId(this.getFloor() + 1);
        
    }
    
    public Player getPlayer() {
        return this.manager.getPlayer();
    }
    
    public StarTowerCase addCase(StarTowerCase towerCase) {
        return this.addCase(null, towerCase);
    }
    
    public StarTowerCase addCase(StarTowerInteractResp rsp, StarTowerCase towerCase) {
        // Add to cases list
        this.cases.add(towerCase);
        
        // Increment id
        towerCase.setId(++this.lastCaseId);
        
        // Set proto
        if (rsp != null) {
            rsp.getMutableCases().add(towerCase.toProto());
        }
        
        return towerCase;
    }
    
    public StarTowerInteractResp handleInteract(StarTowerInteractReq req) {
        var rsp = StarTowerInteractResp.newInstance()
                .setId(req.getId());
                
        if (req.hasBattleEndReq()) {
            this.onBattleEnd(req, rsp);
        } else if (req.hasRecoveryHPReq()) {
            var proto = req.getRecoveryHPReq();
        } else if (req.hasSelectReq()) {
            
        } else if (req.hasEnterReq()) {
            this.onEnterReq(req, rsp);
        }
        
        // Set data protos
        rsp.getMutableData();
        rsp.getMutableChange();
        //rsp.getMutableNextPackage();
        
        return rsp;
    }
    
    // Interact events
    
    @SneakyThrows
    public void onBattleEnd(StarTowerInteractReq req, StarTowerInteractResp rsp) {
        var proto = req.getBattleEndReq();
        
        if (proto.hasVictory()) {
            // Add team level
            this.teamLevel++;
            
            // Add clear time
            this.battleTime += proto.getVictory().getTime();
            
            // Handle victory
            rsp.getMutableBattleEndResp()
                .getMutableVictory()
                .setLv(this.getTeamLevel())
                .setBattleTime(this.getBattleTime());
            
            // Add potential selector TODO
        } else {
            // Handle defeat
        }
    }
    
    public void onSelect(StarTowerInteractReq req, StarTowerInteractResp rsp) {
        
    }
    
    public void onEnterReq(StarTowerInteractReq req, StarTowerInteractResp rsp) {
        var proto = req.getEnterReq();
        
        // Set
        this.floor = this.floor++;
        this.mapId = proto.getMapId();
        this.mapTableId = proto.getMapTableId();
        
        // Clear cases TODO
        this.lastCaseId = 0;
        this.cases.clear();
        
        // Add cases
        var syncHpCase = this.addCase(new StarTowerCase(CaseType.SyncHP));
        var doorCase = this.addCase(new StarTowerCase(CaseType.OpenDoor));
        doorCase.setFloorId(this.getFloor() + 1);
        
        // Proto
        var room = rsp.getMutableEnterResp().getMutableRoom();
        
        room.getMutableData()
            .setMapId(this.getMapId())
            .setMapTableId(this.getMapTableId())
            .setFloor(this.getFloor());
        
        room.addAllCases(syncHpCase.toProto(), doorCase.toProto());
    }

    public void onRecoveryHP(StarTowerInteractReq req, StarTowerInteractResp rsp) {
        // Add case
        this.addCase(rsp, new StarTowerCase(CaseType.RecoveryHP));
    }
    
    // Proto
    
    public StarTowerInfo toProto() {
        var proto = StarTowerInfo.newInstance();
        
        proto.getMutableMeta()
            .setId(this.getId())
            .setCharHp(this.getCharHp())
            .setTeamLevel(this.getTeamLevel())
            .setNPCInteractions(1)
            .setBuildId(this.getBuildId());
        
        this.getChars().forEach(proto.getMutableMeta()::addChars);
        this.getDiscs().forEach(proto.getMutableMeta()::addDiscs);
        
        proto.getMutableRoom().getMutableData()
            .setFloor(this.getFloor())
            .setMapId(this.getMapId())
            .setMapTableId(this.getMapTableId())
            .setMapParam(this.getMapParam())
            .setParamId(this.getParamId());
        
        // Cases
        for (var starTowerCase : this.getCases()) {
            proto.getMutableRoom().addCases(starTowerCase.toProto());
        }
        
        // TODO
        proto.getMutableBag();
        
        return proto;
    }
}
