package io.quarkus.ts.infinispan.client.serialized;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(includeClasses = { ShopItem.class }, schemaPackageName = "quarkus_qe")
interface ShopItemSchema extends GeneratedSchema {
}
