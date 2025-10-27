package emu.nebula.game.tower;

import java.util.ArrayList;
import java.util.List;

import dev.morphia.annotations.Entity;
import emu.nebula.data.GameData;
import emu.nebula.data.resources.PotentialDef;
import emu.nebula.data.resources.StarTowerDef;
import emu.nebula.data.resources.StarTowerStageDef;
import emu.nebula.game.formation.Formation;
import emu.nebula.game.player.Player;
import emu.nebula.game.player.PlayerChangeInfo;
import emu.nebula.proto.PublicStarTower.*;
import emu.nebula.proto.StarTowerApply.StarTowerApplyReq;
import emu.nebula.proto.StarTowerInteract.StarTowerInteractReq;
import emu.nebula.proto.StarTowerInteract.StarTowerInteractResp;
import emu.nebula.util.Snowflake;
import emu.nebula.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
    private int stage;
    private int floor;
    private int mapId;
    private int mapTableId;
    private String mapParam;
    private int paramId;
    private int roomType;
    
    // Team
    private int formationId;
    private int buildId;
    private int teamLevel;
    private int teamExp;
    private int charHp;
    private int battleTime;
    private int battleCount;
    private List<StarTowerChar> chars;
    private List<StarTowerDisc> discs;
    private IntSet charIds;
    
    private int lastCaseId = 0;
    private int selectorCaseIndex = -1;
    private List<StarTowerCase> cases;
    
    private Int2IntMap items;
    private IntSet potentials;
    
    private transient StarTowerBuild build;
    
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
        this.stage = 1;
        this.floor = 1;
        this.charHp = -1;
        this.chars = new ArrayList<>();
        this.discs = new ArrayList<>();
        this.charIds = new IntOpenHashSet();

        this.cases = new ArrayList<>();
        this.items = new Int2IntOpenHashMap();
        this.potentials = new IntOpenHashSet();
        
        // Init formation
        for (int i = 0; i < 3; i++) {
            int id = formation.getCharIdAt(i);
            var character = getPlayer().getCharacters().getCharacterById(id);
            
            if (character != null) {
                this.chars.add(character.toStarTowerProto());
                this.charIds.add(id);
            } else {
                this.chars.add(StarTowerChar.newInstance());
            }
        }
        
        for (int i = 0; i < 6; i++) {
            int id = formation.getDiscIdAt(i);
            var disc = getPlayer().getCharacters().getDiscById(id);
            
            if (disc != null) {
                this.discs.add(disc.toStarTowerProto());
            } else {
                this.discs.add(StarTowerDisc.newInstance());
            }
        }
        
        // Add cases
        this.addCase(new StarTowerCase(CaseType.Battle));
        this.addCase(new StarTowerCase(CaseType.SyncHP));
        
        // Debug
        var doorCase = this.addCase(new StarTowerCase(CaseType.OpenDoor));
        doorCase.setFloorId(this.getFloor() + 1);
        
        var nextStage = this.getNextStageData();
        if (nextStage != null) {
            doorCase.setRoomType(nextStage.getRoomType());
        }
    }
    
    public Player getPlayer() {
        return this.manager.getPlayer();
    }
    
    public StarTowerBuild getBuild() {
        if (this.build == null) {
            this.build = new StarTowerBuild(this);
        }
        
        return this.build;
    }
    
    public StarTowerStageDef getStageData(int stage, int floor) {
        var stageId = (this.getId() * 10000) + (stage * 100) + floor;
        return GameData.getStarTowerStageDataTable().get(stageId);
    }
    
    public StarTowerStageDef getNextStageData() {
        int stage = this.stage;
        int floor = this.floor + 1;
        
        if (floor >= this.getData().getMaxFloor(this.getStage())) {
            floor = 1;
            stage++;
        }
        
        return getStageData(stage, floor);
    }
    
    // Cases
    
    public StarTowerCase getSelectorCase() {
        if (this.getSelectorCaseIndex() < 0 || this.getSelectorCaseIndex() >= this.getCases().size()) {
            return null;
        }
        
        return this.getCases().get(this.getSelectorCaseIndex());
    }
    
    public StarTowerCase addCase(StarTowerCase towerCase) {
        return this.addCase(null, towerCase);
    }
    
    public StarTowerCase addCase(StarTowerInteractResp rsp, StarTowerCase towerCase) {
        // Add to cases list
        this.getCases().add(towerCase);
        
        // Increment id
        towerCase.setId(++this.lastCaseId);
        
        // Set proto
        if (rsp != null) {
            rsp.getMutableCases().add(towerCase.toProto());
        }
        
        //
        if (towerCase.getIds() != null) {
            this.selectorCaseIndex = this.getCases().size() - 1;
        }
        
        return towerCase;
    }
    
    // Items

    public int getItemCount(int id) {
        return this.getItems().get(id);
    }
    
    public PlayerChangeInfo addItem(int id, int count, PlayerChangeInfo changes) {
        // Create changes if null
        if (changes == null) {
            changes = new PlayerChangeInfo();
        }
        
        // Get item data
        var itemData = GameData.getItemDataTable().get(id);
        if (itemData == null) {
            return changes;
        }
        
        // Add item
        this.getItems().put(id, this.getItems().get(id) + count);
        
        // Handle changes
        switch (itemData.getItemSubType()) {
            case Potential, SpecificPotential -> {
                // Add potential
                this.getPotentials().add(id);
                
                // Add change
                var change = PotentialInfo.newInstance()
                        .setTid(id)
                        .setLevel(this.getItems().get(id));
                
                changes.add(change);
            }
            default -> {
                // Ignored
            }
        }
        
        // Return changes
        return changes;
    }
    
    // Handlers
    
    public StarTowerInteractResp handleInteract(StarTowerInteractReq req) {
        var rsp = StarTowerInteractResp.newInstance()
                .setId(req.getId());
                
        if (req.hasBattleEndReq()) {
            rsp = this.onBattleEnd(req, rsp);
        } else if (req.hasRecoveryHPReq()) {
            rsp = this.onRecoveryHP(req, rsp);
        } else if (req.hasSelectReq()) {
            rsp = this.onSelect(req, rsp);
        } else if (req.hasEnterReq()) {
            rsp = this.onEnterReq(req, rsp);
        }
        
        // Set data protos
        rsp.getMutableData();
        rsp.getMutableChange();
        
        return rsp;
    }
    
    // Interact events
    
    @SneakyThrows
    public StarTowerInteractResp onBattleEnd(StarTowerInteractReq req, StarTowerInteractResp rsp) {
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

            // Add potential selector
            var potentialCase = new StarTowerCase(CaseType.SelectSpecialPotential);
            potentialCase.setTeamLevel(this.getTeamLevel());
            
            // Get random potentials
            List<PotentialDef> potentials = new ArrayList<>();
            int charId = this.getChars().get(battleCount % this.getCharIds().size()).getId();
            
            for (var potentialData : GameData.getPotentialDataTable()) {
                if (potentialData.getCharId() == charId) {
                    potentials.add(potentialData);
                }
            }
            
            for (int i = 0; i < 3; i++) {
                var potentialData = Utils.randomElement(potentials);
                potentialCase.addId(potentialData.getId());
            }
            
            // Increment battle count
            this.battleCount++;
            
            // Add case
            this.addCase(rsp, potentialCase);
        } else {
            // Handle defeat
            // TODO
        }
        
        return rsp;
    }
    
    public StarTowerInteractResp onSelect(StarTowerInteractReq req, StarTowerInteractResp rsp) {
        var index = req.getMutableSelectReq().getIndex();
        
        var selectorCase = this.getSelectorCase();
        if (selectorCase == null) {
            return rsp;
        }
        
        int id = selectorCase.selectId(index);
        if (id <= 0) {
            return rsp;
        }
        
        // Add item
        var changes = this.addItem(id, 1, null);
        
        rsp.setChange(changes.toProto());
        
        return rsp;
    }
    
    public StarTowerInteractResp onEnterReq(StarTowerInteractReq req, StarTowerInteractResp rsp) {
        // Get proto
        var proto = req.getEnterReq();
        
        // Set
        this.mapId = proto.getMapId();
        this.mapTableId = proto.getMapTableId();
        this.mapParam = proto.getMapParam();
        this.paramId = proto.getParamId();
        
        // Next floor
        this.floor++;
        
        if (this.floor >= this.getData().getMaxFloor(this.getStage())) {
            this.floor = 1;
            this.stage++;
        }
        
        // Calculate stage
        var stageData = this.getStageData(this.getStage(), this.getFloor());
        
        if (stageData != null) {
            this.roomType = stageData.getRoomType();
        } else {
            this.roomType = 0;
        }
        
        // Clear cases
        this.selectorCaseIndex = -1;
        this.lastCaseId = 0;
        this.cases.clear();
        
        // Add cases
        var syncHpCase = new StarTowerCase(CaseType.SyncHP);
        var doorCase = new StarTowerCase(CaseType.OpenDoor);
        doorCase.setFloorId(this.getFloor() + 1);
        
        // Set room type of next room
        var nextStage = this.getNextStageData();
        if (nextStage != null) {
            doorCase.setRoomType(nextStage.getRoomType());
        }
        
        // Room proto
        var room = rsp.getMutableEnterResp().getMutableRoom();
        room.setData(this.toRoomDataProto());
        
        // Handle room type TODO
        if (this.roomType <= StarTowerRoomType.EliteBattleRoom.getValue()) {
            var battleCase = new StarTowerCase(CaseType.Battle);
            battleCase.setSubNoteSkillNum(this.getBattleCount());
            
            this.addCase(battleCase);
            room.addCases(battleCase.toProto());
        }
        
        // Add cases
        this.addCase(syncHpCase);
        this.addCase(doorCase);
        
        // Add cases to room
        room.addCases(syncHpCase.toProto());
        room.addCases(doorCase.toProto());
        
        return rsp;
    }

    public StarTowerInteractResp onRecoveryHP(StarTowerInteractReq req, StarTowerInteractResp rsp) {
        // Add case
        this.addCase(rsp, new StarTowerCase(CaseType.RecoveryHP));
        
        return rsp;
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
        
        proto.getMutableRoom().setData(this.toRoomDataProto());
        
        // Cases
        for (var starTowerCase : this.getCases()) {
            proto.getMutableRoom().addCases(starTowerCase.toProto());
        }
        
        // TODO
        proto.getMutableBag();
        
        return proto;
    }
    
    public StarTowerRoomData toRoomDataProto() {
        var proto = StarTowerRoomData.newInstance()
                .setFloor(this.getFloor())
                .setMapId(this.getMapId())
                .setRoomType(this.getRoomType())
                .setMapTableId(this.getMapTableId());
        
        if (this.getMapParam() != null && !this.getMapParam().isEmpty()) {
            proto.setMapParam(this.getMapParam());
        }
        
        if (this.getParamId() != 0) {
            proto.setParamId(this.getParamId());
        }
        
        return proto;
    }

}
