package org.atcraftmc.starlight.util.dependency;

public enum MavenRepo {
    CENTRAL("https://repo1.maven.org/maven2/"),
    ALICLOUD("https://maven.aliyun.com/repository/central/"),
    HUAWEI("https://repo.huaweicloud.com/repository/maven/"),
    TENCENT("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/"),
    TSINGHUA("https://mirrors.tuna.tsinghua.edu.cn/nexus/content/repositories/central/");

    private final String url;

    MavenRepo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}