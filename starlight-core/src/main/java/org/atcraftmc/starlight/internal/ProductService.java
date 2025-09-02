package org.atcraftmc.starlight.internal;

import me.gb2022.commons.math.SHA;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceInject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Base64;


//激活：发送mac地址和访问码，远端服务器返回激活信息，并删除所有旧设备的激活信息。
@SLService(id = "product")
public interface ProductService extends Service {
    Activator ACTIVATOR = new DevActivator();

    @ServiceInject
    static void start() {
        ACTIVATOR.test();
    }

    static String getSystemIdentifier() {
        try {
            for (int i = 0; i < 8; i++) {
                NetworkInterface networkInterfaces = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                if (networkInterfaces == null) {
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
