package io.quarkus.ts.security.webauthn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Random;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import io.quarkus.test.bootstrap.Protocol;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.Codec;

public class MyWebAuthnHardware {
    private KeyPair keyPair;
    private String id;
    private byte[] credID;

    public MyWebAuthnHardware() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            this.keyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        this.credID = new byte[32];
        new Random().nextBytes(this.credID);
        this.id = Base64.getUrlEncoder().withoutPadding().encodeToString(this.credID);
    }

    public JsonObject makeRegistrationJson(String challenge) {
        JsonObject clientData = (new JsonObject()).put("type", "webauthn.create").put("challenge", challenge)
                .put("origin", MySqlWebAuthnIT.app.getURI(Protocol.HTTP).getHost()).put("crossOrigin", false);
        String clientDataEncoded = Base64.getUrlEncoder().encodeToString(clientData.encode().getBytes(StandardCharsets.UTF_8));
        byte[] authBytes = this.makeAuthBytes();
        CBORFactory cborFactory = new CBORFactory();
        ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();

        try {
            JsonGenerator generator = cborFactory.createGenerator(byteWriter);
            generator.writeStartObject();
            generator.writeStringField("fmt", "none");
            generator.writeObjectFieldStart("attStmt");
            generator.writeEndObject();
            generator.writeBinaryField("authData", authBytes);
            generator.writeEndObject();
            generator.close();
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        }

        String attestationObjectEncoded = Base64.getUrlEncoder().encodeToString(byteWriter.toByteArray());
        return (new JsonObject())
                .put("id", this.id).put("rawId", this.id).put("response", (new JsonObject())
                        .put("attestationObject", attestationObjectEncoded).put("clientDataJSON", clientDataEncoded))
                .put("type", "public-key");
    }

    private byte[] makeAuthBytes() {
        int counter = 1;
        Buffer buffer = Buffer.buffer();
        String rpDomain = MySqlWebAuthnIT.app.getURI(Protocol.HTTP).getHost();

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException var19) {
            throw new RuntimeException(var19);
        }

        byte[] rpIdHash = md.digest(rpDomain.getBytes(StandardCharsets.UTF_8));
        buffer.appendBytes(rpIdHash);
        byte flags = 65;
        buffer.appendByte(flags);
        long signCounter = counter++;
        buffer.appendUnsignedInt(signCounter);
        String aaguidString = "00000000-0000-0000-0000-000000000000";
        String aaguidStringShort = aaguidString.replace("-", "");
        byte[] aaguid = Codec.base16Decode(aaguidStringShort);
        buffer.appendBytes(aaguid);
        buffer.appendUnsignedShort(this.credID.length);
        buffer.appendBytes(this.credID);
        ECPublicKey publicKey = (ECPublicKey) this.keyPair.getPublic();
        Base64.Encoder urlEncoder = Base64.getUrlEncoder();
        String x = urlEncoder.encodeToString(publicKey.getW().getAffineX().toByteArray());
        String y = urlEncoder.encodeToString(publicKey.getW().getAffineY().toByteArray());
        CBORFactory cborFactory = new CBORFactory();
        ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();

        try {
            JsonGenerator generator = cborFactory.createGenerator(byteWriter);
            generator.writeStartObject();
            generator.writeNumberField("1", 2);
            generator.writeNumberField("3", -7);
            generator.writeNumberField("-1", 1);
            generator.writeStringField("-2", x);
            generator.writeStringField("-3", y);
            generator.writeEndObject();
            generator.close();
        } catch (IOException var18) {
            throw new RuntimeException(var18);
        }
        buffer.appendBytes(byteWriter.toByteArray());
        return buffer.getBytes();
    }

}
