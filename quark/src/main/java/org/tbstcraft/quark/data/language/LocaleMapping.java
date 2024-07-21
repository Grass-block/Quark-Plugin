package org.tbstcraft.quark.data.language;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public interface LocaleMapping {
    Map<String, String[]> LANGUAGE_ROOT_MAP = new HashMap<>();
    String[] MINECRAFT_KNOWN_LANGUAGES = {"af_za", "ar_sa", "ast_es", "az_az", "ba_ru", "bar", "be_by", "bg_bg", "br_fr", "brb", "bs_ba", "ca_es", "cs_cz", "cy_gb", "da_dk", "de_at", "de_ch", "de_de", "el_gr", "en_au", "en_ca", "en_gb", "en_nz", "en_pt", "en_ud", "en_us", "eo_uy", "es_ar", "es_cl", "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "esan", "et_ee", "eu_es", "fa_ir", "fi_fi", "fil_ph", "fo_fo", "fr_ca", "fr_fr", "fra_de", "fy_nl", "ga_ie", "gd_gb", "gl_es", "haw_us", "he_il", "hi_in", "hr_hr", "hu_hu", "hy_am", "id_id", "ig_ng", "io_en", "is_is", "isv", "it_it", "ja_jp", "jbo_en", "jv_id", "ka_ge", "kk_kz", "kn_in", "ko_kr", "ksh", "kw_gb", "la_la", "lb_lu", "li_li", "lmo", "lol_us", "lt_lt", "lv_lv", "lzh", "mk_mk", "mn_mn", "ms_my", "mt_mt", "nds_de", "nl_be", "nl_nl", "nn_no", "no_no", "nuk", "oc_fr", "ovd", "pl_pl", "pt_br", "pt_pt", "qya_aa", "ro_ro", "rpr", "ru_ru", "se_no", "sk_sk", "sl_si", "so_so", "sq_al", "sr_sp", "sv_se", "swg", "sxu", "szl", "ta_in", "th_th", "tl_ph", "tlh_aa", "tok", "tr_tr", "tt_ru", "uk_ua", "val_es", "vec_it", "vi_vn", "yi_de", "yo_ng", "zh_cn", "zh_hk", "zh_tw", "zlm_arab"};

    static Locale locale(String minecraft) {
        String[] parts = minecraft.split("_");
        if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length == 1) {
            return new Locale(parts[0]);
        } else {
            throw new IllegalArgumentException("Invalid Minecraft language ID: " + minecraft);
        }
    }

    static String minecraft(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();

        if (!country.isEmpty()) {
            return language + "_" + country.toLowerCase();
        } else {
            return language;
        }
    }

    static String remap(String input, Function<String, Boolean> checker) {
        if (checker.apply(input)) {
            return input;
        }

        if (LANGUAGE_ROOT_MAP.isEmpty()) {
            initLocaleMap();
        }

        String root = input.contains("_") ? input.split("_")[0] : input;

        if (!LANGUAGE_ROOT_MAP.containsKey(root)) {
            return remap("en_us", checker);
        }

        for (String lang : LANGUAGE_ROOT_MAP.get(root)) {
            if (checker.apply(lang)) {
                return lang;
            }
        }

        if (input.equals("en_us")) {
            return "zh_cn";
        }

        return remap("en_us", checker);
    }

    static void initLocaleMap() {
        LANGUAGE_ROOT_MAP.put("en", new String[]{"en_gb", "en_us", "en_au", "en_ca", "en_nz"});
        LANGUAGE_ROOT_MAP.put("zh", new String[]{"zh_cn", "zh_tw", "zh_hk"});
        LANGUAGE_ROOT_MAP.put("es", new String[]{"es_es", "es_mx", "es_ar", "es_cl", "es_ec", "es_uy", "es_ve"});
        LANGUAGE_ROOT_MAP.put("fr", new String[]{"fr_fr", "fr_ca"});
        LANGUAGE_ROOT_MAP.put("de", new String[]{"de_de", "de_at", "de_ch"});
        LANGUAGE_ROOT_MAP.put("pt", new String[]{"pt_pt", "pt_br"});
        LANGUAGE_ROOT_MAP.put("ru", new String[]{"ru_ru"});
        LANGUAGE_ROOT_MAP.put("ja", new String[]{"ja_jp"});
        LANGUAGE_ROOT_MAP.put("ko", new String[]{"ko_kr"});
        LANGUAGE_ROOT_MAP.put("ar", new String[]{"ar_sa", "ar_ae", "ar_eg", "ar_lb"});
        LANGUAGE_ROOT_MAP.put("it", new String[]{"it_it"});
        LANGUAGE_ROOT_MAP.put("nl", new String[]{"nl_nl", "nl_be"});
        LANGUAGE_ROOT_MAP.put("hi", new String[]{"hi_in"});
        LANGUAGE_ROOT_MAP.put("sv", new String[]{"sv_se"});
        LANGUAGE_ROOT_MAP.put("fi", new String[]{"fi_fi"});
        LANGUAGE_ROOT_MAP.put("da", new String[]{"da_dk"});
        LANGUAGE_ROOT_MAP.put("no", new String[]{"no_no"});
        LANGUAGE_ROOT_MAP.put("pl", new String[]{"pl_pl"});
        LANGUAGE_ROOT_MAP.put("uk", new String[]{"uk_ua"});
        LANGUAGE_ROOT_MAP.put("cs", new String[]{"cs_cz"});
        LANGUAGE_ROOT_MAP.put("sk", new String[]{"sk_sk"});
        LANGUAGE_ROOT_MAP.put("el", new String[]{"el_gr"});
        LANGUAGE_ROOT_MAP.put("ro", new String[]{"ro_ro"});
        LANGUAGE_ROOT_MAP.put("bg", new String[]{"bg_bg"});
        LANGUAGE_ROOT_MAP.put("hr", new String[]{"hr_hr"});
        LANGUAGE_ROOT_MAP.put("sr", new String[]{"sr_sp"});
    }
}
