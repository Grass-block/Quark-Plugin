package org.tbstcraft.quark.framework.module;

import org.tbstcraft.quark.FeatureAvailability;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.atcraftmc.qlib.language.LanguageItem;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.framework.FunctionalComponentStatus;
import org.tbstcraft.quark.framework.packages.IPackage;

import java.util.Locale;

public interface ModuleMeta {
    static ModuleMeta dummy(String id) {
        return new DummyModuleMeta(id);
    }

    static ModuleMeta wrap(AbstractModule m) {
        var lang = LanguageContainer.getInstance().access(m.getParent().getId());
        var display = lang.item("-module-name", m.getId());
        var namespace = m.getFullId().split(":")[0];
        var id = m.getFullId().split(":")[1];

        var meta = new AnnotationBasedMeta((Class<AbstractModule>) m.getClass(), m.getParent(), namespace, id, display);
        meta.handle(m);

        return meta;
    }

    static ModuleMeta create(Class<AbstractModule> reference, IPackage parent, String id) {
        var lang = LanguageContainer.getInstance().access(parent.getId());
        var display = lang.item("-module-name", id);

        return new AnnotationBasedMeta(reference, parent, parent.getId(), id, display);
    }


    //----[FIXED]----
    Class<? extends AbstractModule> reference();

    String id();

    String fullId();

    String displayName(Locale locale);

    String version();

    boolean defaultEnable();

    boolean beta();

    boolean internal();

    APIProfile[] compatBlackList();

    FeatureAvailability available();

    String description();

    IPackage parent();


    //----[META]----
    FunctionalComponentStatus status();

    String additional();

    <I extends AbstractModule> I get(Class<I> type);

    void status(FunctionalComponentStatus functionalComponentStatus);

    void additional(String info);

    AbstractModule handle();

    void handle(AbstractModule handle);

    default boolean unknown() {
        return this instanceof DummyModuleMeta;
    }


    record DummyModuleMeta(String id) implements ModuleMeta {

        @Override
        public String id() {
            return this.id + "[unknown]";
        }

        @Override
        public String fullId() {
            return "unknown:" + this.id;
        }

        @Override
        public String displayName(Locale locale) {
            return "&7Unknown Module";
        }

        @Override
        public String version() {
            return "[unknown]";
        }

        @Override
        public String description() {
            return "no description";
        }

        @Override
        public FunctionalComponentStatus status() {
            return FunctionalComponentStatus.UNKNOWN;
        }

        @Override
        public String additional() {
            return "[unknown or unregistered module]";
        }

        @Override
        public <I extends AbstractModule> I get(Class<I> type) {
            return null;
        }

        @Override
        public AbstractModule handle() {
            return null;
        }

        @Override
        public void status(FunctionalComponentStatus status) {
            throw new UnsupportedOperationException("unknown module!");
        }

        @Override
        public void additional(String info) {
            throw new UnsupportedOperationException("unknown module!");
        }

        @Override
        public void handle(AbstractModule handle) {
            throw new UnsupportedOperationException("unknown module!");
        }

        @Override
        public Class<? extends AbstractModule> reference() {
            return null;
        }

        @Override
        public boolean defaultEnable() {
            return false;
        }

        @Override
        public boolean beta() {
            return false;
        }

        @Override
        public boolean internal() {
            return false;
        }

        @Override
        public APIProfile[] compatBlackList() {
            return new APIProfile[0];
        }

        @Override
        public FeatureAvailability available() {
            return FeatureAvailability.DEMO_AVAILABLE;
        }

        @Override
        public IPackage parent() {
            return null;
        }
    }

    abstract class AbstractMeta implements ModuleMeta {
        private final IPackage parent;
        private FunctionalComponentStatus status = FunctionalComponentStatus.UNKNOWN;
        private String additional = "";
        private AbstractModule handle;

        protected AbstractMeta(IPackage parent) {
            this.parent = parent;
        }

        @Override
        public void status(FunctionalComponentStatus status) {
            this.status = status;
        }

        @Override
        public FunctionalComponentStatus status() {
            return this.status;
        }

        @Override
        public void additional(String info) {
            this.additional = info;
        }

        @Override
        public String additional() {
            return this.additional;
        }

        @Override
        public AbstractModule handle() {
            return this.handle;
        }

        @Override
        public <I extends AbstractModule> I get(Class<I> type) {
            return type.cast(this.handle);
        }

        @Override
        public void handle(AbstractModule handle) {
            this.handle = handle;
        }

        @Override
        public IPackage parent() {
            return this.parent;
        }
    }

    final class AnnotationBasedMeta extends AbstractMeta {
        private final Class<AbstractModule> reference;
        private final String id;
        private final String namespace;
        private final LanguageItem displayName;
        private final QuarkModule descriptor;

        public AnnotationBasedMeta(Class<AbstractModule> reference, IPackage parent, String namespace, String id, LanguageItem displayName) {
            super(parent);

            this.reference = reference;
            this.id = id;
            this.namespace = namespace;
            this.displayName = displayName;

            this.descriptor = reference.getAnnotation(QuarkModule.class);
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public String fullId() {
            return this.namespace + ":" + this.id;
        }

        @Override
        public String displayName(Locale locale) {
            var parts = this.id.split("-"); // 按 '-' 分割
            var displayId = new StringBuilder();

            for (var part : parts) {
                if (!part.isEmpty()) {
                    displayId.append(Character.toUpperCase(part.charAt(0))); // 首字母大写
                    displayId.append(part.substring(1).toLowerCase()); // 剩余部分小写
                }
            }

            if (this.displayName != null && this.displayName.getParent().hasKey("-module-name", this.id)) {
                return "%s&7(&f%s&7)".formatted(displayId.toString(), this.displayName.getMessage(locale));
            }

            return displayId.toString();
        }


        @Override
        public String version() {
            return this.descriptor.version();
        }

        @Override
        public String description() {
            return this.descriptor.description();
        }

        @Override
        public Class<? extends AbstractModule> reference() {
            return this.reference;
        }

        @Override
        public boolean defaultEnable() {
            return this.descriptor.defaultEnable();
        }

        @Override
        public boolean beta() {
            return this.descriptor.beta();
        }

        @Override
        public boolean internal() {
            return this.descriptor.internal();
        }

        @Override
        public APIProfile[] compatBlackList() {
            return this.descriptor.compatBlackList();
        }

        @Override
        public FeatureAvailability available() {
            var val = this.descriptor.available();

            if (val == FeatureAvailability.INHERIT) {
                return parent().getAvailability();
            }

            return val;
        }
    }
}
