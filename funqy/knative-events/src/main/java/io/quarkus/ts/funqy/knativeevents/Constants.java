package io.quarkus.ts.funqy.knativeevents;

public interface Constants {

    String CUSTOM_EVENT_ATTR_VALUE = "customEventAttrValue";
    String CUSTOM_EVENT_ATTR_NAME = "customEventAttrName";
    String PING_TRIGGER = "ping";
    String PING_RESPONSE_SOURCE = "ping-response-source";
    String PING_RESPONSE_TYPE = "ping-response-type";
    String PONG_EXPECTED_VALUE = PING_RESPONSE_SOURCE + "," + PING_RESPONSE_TYPE;
    byte[] FALLBACK_EXPECTED_VALUE = new byte[] { 0x24, 0x68, (byte) 0xAC, (byte) 0xf0 };
    String ENV_VAR_NAME = "env-var-name";
    String ENV_VAR_VALUE = "env-var-value";
    String PUNG_EXPECTED_VALUE = "Answer to the Ultimate Question of Life, the Universe and Everything is 42.";
    String PENG_EXPECTED_VALUE = "id: one-two-three, type: peng";
    String ULTIMATE_QUESTION = "Answer to the Ultimate Question of Life, the Universe and Everything";
    String NOT_MATCHED_NAME = "notMatchedName";
    String NOT_MATCHED_VALUE = "notMatchedValue";

}
