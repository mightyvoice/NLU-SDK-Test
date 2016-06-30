package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SocketFactory {

    String _host = null;
    int _port = 0;
    boolean _secure = true;
    boolean _requiresTrustedRoot = true;

    private SocketFactory(String host, int port, boolean secure, boolean requiresTrustedRoot) {
        _host = host;
        _port = port;
        _secure = secure;
        _requiresTrustedRoot = requiresTrustedRoot;
    }

    public static Socket createSocket(String host, int port, boolean secure) {
        return createSocket(host, port, secure, true);
    }

    public static Socket createSocket(String host, int port, boolean secure, boolean requiresTrustedRoot) {
        SocketFactory sf = new SocketFactory(host, port, secure, requiresTrustedRoot);
        Socket s = null;

        try {
            s = sf.createSocket();
            return s;
        }
        catch( Exception e ) {
            e.printStackTrace(System.out);
            return s;
        }
    }

    /**
     * Create a trust manager that validates the certificate chain is from a trusted root CA,
     * and verify the server hostname returned in the TLS handshake
     *
     */
    final TrustManager[] verifyCert = new TrustManager[] { new X509TrustManager() {

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType ) {
            // Not Used
            System.out.println("Checking if client is trusted...");
        }

        @Override
        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException {
            System.out.println("Checking if server is trusted...");

        	/*
        	 * To authenticate the communication channel between client and server we need to ensure
        	 * that both the ssl certificate is from a trusted root CA and the hostname in the certificate
        	 * matches the one we are expecting.
        	 *
        	 * Nuance NCS Services SSL certificate hostname is *.nuancemobility.net
        	 *
        	 */

            boolean containsTrustedRootCA = false;
            boolean containsValidHostname = false;
            //String validHostname = "*.nuancemobility.net";

            for(int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];

                // Check to ensure certificate hasn't expired...
                try {
                    cert.checkValidity();
                } catch (CertificateExpiredException e) {
                    System.out.println( "Certificate Expiration Error: " + e.getMessage() );
                    throw e;
                } catch (CertificateNotYetValidException e) {
                    System.out.println( "Certificate Expiration Error: " + e.getMessage() );
                    throw e;
                }

                // Ensure the chain contains a certificate from a trusted root CA
                if ( _requiresTrustedRoot && cert.getBasicConstraints() != -1 ) // Chain contains a trusted root CA
                    containsTrustedRootCA = true;


                // Ensure the chain contains a certificate with a valid hostname
                String dn = cert.getSubjectDN().getName();

                int cnStart = dn.indexOf("CN=") + 3;
                int cnEnd = dn.indexOf(',', cnStart);
                String cn = dn.substring(cnStart, cnEnd);
                System.out.println("Verifying hostname against CN: " + cn);

                if( cn.startsWith("*") && _host.toLowerCase().endsWith(cn.substring(1).toLowerCase()) ) {
                    containsValidHostname = true;
                }
                else if( cn.equalsIgnoreCase(_host) )
                    containsValidHostname = true;
            }

            // Throw any exceptions...
            if( !containsValidHostname )
                throw new java.security.cert.CertificateException("Invalid Certificate Hostname!");

            if( _requiresTrustedRoot && !containsTrustedRootCA )
                throw new java.security.cert.CertificateException("Certificate is not from Trusted CA!");

            // All good!
            return;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // Not Used
            System.out.println("Checking accepted issuers...");
            return null;
        }
    }};

    /**
     * Create either a secure or insecure socket based on the value of class member _useSSL
     *
     * @return Socket
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws UnknownHostException
     * @throws IOException
     */
    protected Socket createSocket() throws NoSuchAlgorithmException, KeyManagementException, UnknownHostException, IOException {

        /** Initialize a secure, encrypted connection to our services... */
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, verifyCert, null);

        Socket s;

        if (_secure) {
            s = sslContext.getSocketFactory().createSocket(_host, _port);
        }
        else {
            // We only support secure connections to our NCS data centers. This is for non-Production development environments only.
            s = javax.net.SocketFactory.getDefault().createSocket(_host, _port);
        }

        s.setSoTimeout(30000);

        return s;
    }


}

