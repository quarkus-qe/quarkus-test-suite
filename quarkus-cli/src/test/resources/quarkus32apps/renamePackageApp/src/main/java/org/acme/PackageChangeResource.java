package org.acme;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.hibernate.search.mapper.orm.coordination.outboxpolling.OutboxPollingExtension;

import javax.security.cert.Certificate;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.CertificateException;
import javax.security.cert.CertificateExpiredException;
import javax.security.cert.CertificateNotYetValidException;
import javax.security.cert.X509Certificate;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.Set;


/**
 * It is used just for "quarkus update" command, to check if it renames packages correctly
 */
@Path("/rename")
public class PackageChangeResource {
    OutboxPollingExtension extension = OutboxPollingExtension.get();
    Certificate certificate;

    @GET
    @Path("/cert")
    public String cert(){
        certificate = createCertificate();
        return certificate.toString();
    }

    @GET
    @Path("/extension")
    public String extension(){
        return extension.getClass().getSimpleName();
    }


    private Certificate createCertificate(){
        return new X509Certificate() {
            public boolean hasUnsupportedCriticalExtension() {
                return false;
            }

            public Set<String> getCriticalExtensionOIDs() {
                return null;
            }

            public Set<String> getNonCriticalExtensionOIDs() {
                return null;
            }

            public byte[] getExtensionValue(String s) {
                return new byte[0];
            }

            @Override
            public byte[] getEncoded() throws CertificateEncodingException {
                return new byte[0];
            }

            @Override
            public void verify(PublicKey publicKey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {

            }

            @Override
            public void verify(PublicKey publicKey, String s) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {

            }

            @Override
            public String toString() {
                return "Hello cert";
            }

            @Override
            public PublicKey getPublicKey() {
                return null;
            }

            @Override
            public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {

            }

            @Override
            public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {

            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public BigInteger getSerialNumber() {
                return null;
            }

            @Override
            public Principal getIssuerDN() {
                return null;
            }

            @Override
            public Principal getSubjectDN() {
                return null;
            }

            @Override
            public Date getNotBefore() {
                return null;
            }

            @Override
            public Date getNotAfter() {
                return null;
            }

            public byte[] getTBSCertificate() throws java.security.cert.CertificateEncodingException {
                return new byte[0];
            }

            public byte[] getSignature() {
                return new byte[0];
            }

            @Override
            public String getSigAlgName() {
                return null;
            }

            @Override
            public String getSigAlgOID() {
                return null;
            }

            @Override
            public byte[] getSigAlgParams() {
                return new byte[0];
            }

            public boolean[] getIssuerUniqueID() {
                return new boolean[0];
            }

            public boolean[] getSubjectUniqueID() {
                return new boolean[0];
            }

            public boolean[] getKeyUsage() {
                return new boolean[0];
            }

            public int getBasicConstraints() {
                return 0;
            }
        };
    }
}
