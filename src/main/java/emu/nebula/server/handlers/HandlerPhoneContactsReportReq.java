package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.PhoneContactsReport.PhoneContactsReportReq;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.phone_contacts_report_req)
public class HandlerPhoneContactsReportReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = PhoneContactsReportReq.parseFrom(message);
        
        // Get chat data
        var data = GameData.getChatDataTable().get(req.getChatId());
        if (data == null) {
            return session.encodeMsg(NetMsgId.phone_contacts_report_failed_ack);
        }
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(data.getAddressBookId());
        if (character == null) {
            return session.encodeMsg(NetMsgId.phone_contacts_report_failed_ack);
        }

        // Handle report
        var change = character.getContact().report(data, req.getProcess(), req.getOptions(), req.getEnd());
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.phone_contacts_report_failed_ack);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.phone_contacts_report_succeed_ack, change.toProto());
    }

}
