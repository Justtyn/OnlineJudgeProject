package com.example.onlinejudge.service;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class IpGeoService {
    private Searcher searcher;

    @PostConstruct
    public void init() {
        try {
            // 需要将 ip2region.xdb 放置在 resources 根目录
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("ip2region.xdb");
            if (is == null) {
                // 可执行 JAR 场景兜底：尝试从文件系统读取（resources 同级）
                try {
                    File file = ResourceUtils.getFile("classpath:ip2region.xdb");
                    if (file.exists()) {
                        is = new FileInputStream(file);
                    }
                } catch (Exception ignore) { }
                if (is == null) {
                    System.out.println("[IpGeoService] ip2region.xdb 未找到，跳过归属地解析");
                    return; // 未提供库则跳过
                }
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            byte[] cBuff = baos.toByteArray();
            searcher = Searcher.newWithBuffer(cBuff);
            System.out.println("[IpGeoService] ip2region.xdb 加载成功，size=" + cBuff.length);
        } catch (Exception ignore) { }
    }

    @PreDestroy
    public void destroy() {
        if (searcher != null) try { searcher.close(); } catch (Exception ignore) {}
    }

    public Map<String, String> resolve(String ip) {
        Map<String, String> map = new HashMap<>();
        if (ip == null || ip.isEmpty() || searcher == null) return map;
        try {
            // 处理 IPv6 映射到 IPv4 的形式 ::ffff:x.x.x.x
            if (ip.contains(":") && ip.contains(".")) {
                int idx = ip.lastIndexOf(":");
                if (idx != -1) {
                    String maybeV4 = ip.substring(idx + 1);
                    if (maybeV4.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                        ip = maybeV4;
                    }
                }
            }
            if (ip.contains(":")) {
                return map; // 纯 IPv6 暂不解析
            }
            String result = searcher.search(ip); // 格式：国家|区域|省份|城市|ISP
            System.out.println("[IpGeoService] resolve ip=" + ip + ", result=" + result);
            if (result != null) {
                String[] parts = result.split("\\|");
                // 兼容不同格式：
                // 常见：国家|区域|省份|城市|ISP  (len>=5)
                // 也可能：国家|省份|城市|ISP   (len==4)
                // 或：国家|城市|ISP         (len==3)
                if (parts.length >= 5) {
                    map.put("country", parts[0]);
                    map.put("region", parts[2]);
                    map.put("city", parts[3]);
                    map.put("isp", parts[4]);
                } else if (parts.length == 4) {
                    map.put("country", parts[0]);
                    map.put("region", parts[1]);
                    map.put("city", parts[2]);
                    map.put("isp", parts[3]);
                } else if (parts.length == 3) {
                    map.put("country", parts[0]);
                    map.put("city", parts[1]);
                    map.put("isp", parts[2]);
                }
            }
        } catch (Exception ignore) { }
        return map;
    }
}


