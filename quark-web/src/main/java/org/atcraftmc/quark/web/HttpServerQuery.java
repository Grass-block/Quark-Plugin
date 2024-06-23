package org.atcraftmc.quark.web;


import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.HttpService;
import org.tbstcraft.quark.internal.http.HttpRequest;

@QuarkModule
public class HttpServerQuery extends PackageModule {
    @Override
    public void enable() {
        HttpService.registerHandler(this);
    }

    @HttpRequest("/query/server")
    public void queryServerInfo(){

    }
}
