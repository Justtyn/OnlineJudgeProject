package com.example.onlinejudge.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class IpUtil {
    // 私有保留地址网段前缀
    private static final String[] PRIVATE_PREFIXES = new String[]{
            "10.", "192.168.", "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
            "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.",
            "127.", "0.", "169.254."
    };

    public static String getClientIps(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff;
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty()) {
            return xri;
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "" : remote;
    }

    public static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            List<String> list = splitIps(xff);
            for (String ip : list) {
                if (isPublicIp(ip)) {
                    return ip;
                }
            }
            return list.isEmpty() ? getRemoteIp(request) : list.get(0);
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty()) {
            return xri;
        }
        return getRemoteIp(request);
    }

    public static String getClientIpSrc(HttpServletRequest request) {
        if (request.getHeader("X-Forwarded-For") != null) return "x-forwarded-for";
        if (request.getHeader("X-Real-IP") != null) return "x-real-ip";
        return "remote-addr";
    }

    private static String getRemoteIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        return remote == null ? "" : remote;
    }

    private static List<String> splitIps(String xff) {
        String[] arr = xff.split(",");
        List<String> list = new ArrayList<>();
        for (String s : arr) {
            String ip = s.trim();
            if (!ip.isEmpty()) list.add(ip);
        }
        return list;
    }

    private static boolean isPublicIp(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        for (String p : PRIVATE_PREFIXES) {
            if (ip.startsWith(p)) return false;
        }
        return true;
    }
}


