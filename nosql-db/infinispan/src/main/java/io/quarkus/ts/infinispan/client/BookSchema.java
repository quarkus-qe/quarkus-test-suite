package io.quarkus.ts.infinispan.client;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(includeClasses = { Book.class }, schemaPackageName = "quarkus_qe")
public interface BookSchema extends GeneratedSchema {
}
