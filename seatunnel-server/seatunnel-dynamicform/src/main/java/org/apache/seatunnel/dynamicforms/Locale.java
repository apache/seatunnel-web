package org.apache.seatunnel.dynamicforms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Multi language support
 */
@Data
public class Locale {
    @JsonIgnore
    public static final String I18N_PREFIX = "i18n.";

    @JsonProperty("zh_CN")
    private Map<String, String> zhCN = new HashMap<>();

    @JsonProperty("en_US")
    private Map<String, String> enUS = new HashMap<>();

    public Locale addZhCN(@NonNull String key, @NonNull String value) {
        zhCN.put(key, value);
        return this;
    }

    public Locale addEnUS(@NonNull String key, @NonNull String value) {
        enUS.put(key, value);
        return this;
    }
}
