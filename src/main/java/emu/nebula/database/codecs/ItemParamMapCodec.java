package emu.nebula.database.codecs;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import emu.nebula.game.inventory.ItemParamMap;

/**
 * Copy of Int2IntMapCodec.java
 */
public class ItemParamMapCodec implements Codec<ItemParamMap> {
    
    @Override
    public Class<ItemParamMap> getEncoderClass() {
        return ItemParamMap.class;
    }

    @Override
    public void encode(BsonWriter writer, ItemParamMap collection, EncoderContext encoderContext) {
        writer.writeStartDocument();
        for (var entry : collection.int2IntEntrySet()) {
            writer.writeName(Integer.toString(entry.getIntKey()));
            writer.writeInt32(entry.getIntValue());
        }
        writer.writeEndDocument();
    }
    
    @Override
    public ItemParamMap decode(BsonReader reader, DecoderContext decoderContext) {
        ItemParamMap collection = new ItemParamMap();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            collection.put(Integer.parseInt(reader.readName()), reader.readInt32());
        }
        reader.readEndDocument();
        return collection;
    }
}