package web;

import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.web.HTTPService;
import org.tbstcraft.quark.service.web.HttpRequest;

@QuarkModule
public class HttpServerQuery extends PackageModule {
    @Override
    public void enable() {
        HTTPService.registerHandler(this);
    }

    @HttpRequest("/query/server")
    public void queryServerInfo(){

    }
}
