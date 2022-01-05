package io.quarkus.ts.security.core;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = { List.class, ArrayList.class, String.class, JsonMapper.class,
        BaseJsonNode.class, JsonNode.class, ArrayNode.class, JsonGenerator.class, ObjectMapper.class }, serialization = true)
public class SerializationConfig {
}
