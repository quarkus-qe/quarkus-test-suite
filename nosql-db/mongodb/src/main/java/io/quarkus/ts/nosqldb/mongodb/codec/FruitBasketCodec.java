package io.quarkus.ts.nosqldb.mongodb.codec;

import java.util.UUID;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import com.mongodb.MongoClientSettings;

import io.quarkus.ts.nosqldb.mongodb.FruitBasket;

public class FruitBasketCodec implements CollectibleCodec<FruitBasket> {

    private final Codec<Document> documentCodec;

    public FruitBasketCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public void encode(BsonWriter writer, FruitBasket fruitBasket, EncoderContext encoderContext) {
        documentCodec.encode(writer, fruitBasket.toDocument(), encoderContext);
    }

    @Override
    public Class<FruitBasket> getEncoderClass() {
        return FruitBasket.class;
    }

    @Override
    public FruitBasket generateIdIfAbsentFromDocument(FruitBasket document) {
        if (!documentHasId(document)) {
            document.setId(UUID.randomUUID().toString());
        }
        return document;
    }

    @Override
    public boolean documentHasId(FruitBasket document) {
        return document.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(FruitBasket document) {
        return new BsonString(document.getId());
    }

    @Override
    public FruitBasket decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        FruitBasket fruitBasket = FruitBasket.fromDocument(document);
        if (document.getString("id") != null) {
            fruitBasket.setId(document.getString("id"));
        }
        return fruitBasket;
    }
}
