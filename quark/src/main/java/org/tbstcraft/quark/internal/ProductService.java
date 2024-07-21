package org.tbstcraft.quark.internal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gb2022.commons.math.SHA;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.Comments;
import org.tbstcraft.quark.util.NetworkUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Base64;
import java.util.Objects;


//激活：发送mac地址和访问码，远端服务器返回激活信息，并删除所有旧设备的激活信息。


@Comments("Remove this from service list to crack. You win :D")

@QuarkService(id = "product")
public interface ProductService extends Service {
    Activator ACTIVATOR = new DevActivator();


    /* update-check:
     *  {"version":"3.54.18","version_info":"Update log"}
     */

    @ServiceInject
    static void start() {
        ACTIVATOR.test();
    }

    static String getSystemIdentifier() {
        try {
            for (int i=0;i<8;i++){
                NetworkInterface networkInterfaces = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                if(networkInterfaces==null){
                    continue;
                }
                return SHA.getSHA224(Base64.getEncoder().encodeToString(networkInterfaces.getHardwareAddress()), false);
            }
        } catch (Exception e) {
            return "__error__";
        }
        return "__error__";
    }

    static boolean isActivated() {
        return ACTIVATOR.isActivated();
    }

    enum OperationResult {
        SUCCESS, FAILED, ERROR
    }

    abstract class Activator {
        private boolean activated;

        public abstract OperationResult verify();

        public abstract OperationResult activate(String code);

        public final OperationResult test() {
            OperationResult op = verify();
            if (op == OperationResult.SUCCESS) {
                this.activated = true;
            }
            return op;
        }

        public final OperationResult upgrade(String code) {
            if (this.activated) {
                return OperationResult.ERROR;
            }

            OperationResult op = activate(code);
            if (op == OperationResult.SUCCESS) {
                this.activated = true;
            }
            return op;
        }

        public boolean isActivated() {
            return activated;
        }
    }

    final class OnlineActivator extends Activator {
        public static final String URL_BASE = "https://service.tbstmc.xyz/qapi";
        public static final String ACTIVATE = "/product/activate?sid=%s&code=%s";
        public static final String VERIFY = "/product/verify?sid=%s";

        static OperationResult sendRequest(String path) {
            String url = URL_BASE + path;

            JsonObject obj;
            try {
                String content = NetworkUtil.httpGet(url);
                obj = (JsonObject) JsonParser.parseString(content);
            } catch (Exception e) {
                Quark.LOGGER.severe("request failed for " + url);
                return OperationResult.ERROR;
            }
            if (Objects.equals(obj.get("status").getAsString(), "success")) {
                return OperationResult.SUCCESS;
            }
            return OperationResult.FAILED;
        }

        @Override
        public OperationResult verify() {
            return sendRequest(VERIFY.formatted(getSystemIdentifier()));
        }

        @Override
        public OperationResult activate(String code) {
            return sendRequest(ACTIVATE.formatted(getSystemIdentifier(), code));

        }
    }

    final class DevActivator extends Activator {
        @Override
        public OperationResult verify() {
            return OperationResult.SUCCESS;
        }

        @Override
        public OperationResult activate(String code) {
            return OperationResult.SUCCESS;
        }
    }
}
