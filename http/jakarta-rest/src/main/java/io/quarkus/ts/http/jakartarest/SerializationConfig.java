package io.quarkus.ts.http.jakartarest;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BaseJsonNode;

@RegisterForReflection(targets = { List.class, ArrayList.class, String.class, JsonMapper.class,
        BaseJsonNode.class, JsonNode.class, ArrayNode.class, JsonGenerator.class, ObjectMapper.class }, serialization = true)
public class SerializationConfig {
}
