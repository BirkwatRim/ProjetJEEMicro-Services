package org.sid.secservice.sec;

public class JWTUtil {
    public static final String SECRET="mySecret1234";
    public static final String AUTH_HEDER="Authorization";
    public static final String PREFIX="Bearer ";
    public static final long EXPIRE_ACCESS_TOKEN=2*60*1000;
    public static final long EXPIRE_REFRECH_TOKEN=15*60*1000;

}
