/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.revelc.code.formatter.xml;

import java.util.Map;

import net.revelc.code.formatter.AbstractCacheableFormatter;
import net.revelc.code.formatter.ConfigurationSource;
import net.revelc.code.formatter.Formatter;
import net.revelc.code.formatter.LineEnding;
import net.revelc.code.formatter.xml.lib.FormattingPreferences;
import net.revelc.code.formatter.xml.lib.XmlDocumentFormatter;

/**
 * The Class XMLFormatter.
 */
public class XMLFormatter extends AbstractCacheableFormatter implements Formatter {

    /** The formatter. */
    private XmlDocumentFormatter formatter;

    @Override
    public void init(final Map<String, String> options, final ConfigurationSource cfg) {
        super.initCfg(cfg);

        final var prefs = new FormattingPreferences();
        final var maxLineLength = options.get("maxLineLength");
        final var wrapLongLines = options.get("wrapLongLines");
        final var tabInsteadOfSpaces = options.get("tabInsteadOfSpaces");
        final var tabWidth = options.get("tabWidth");
        final var splitMultiAttrs = options.get("splitMultiAttrs");
        final var wellFormedValidation = options.get("wellFormedValidation");

        prefs.setMaxLineLength(maxLineLength != null ? Integer.valueOf(maxLineLength) : null);
        prefs.setWrapLongLines(wrapLongLines != null ? Boolean.valueOf(wrapLongLines) : null);
        prefs.setTabInsteadOfSpaces(tabInsteadOfSpaces != null ? Boolean.valueOf(tabInsteadOfSpaces) : null);
        prefs.setTabWidth(tabWidth != null ? Integer.valueOf(tabWidth) : null);
        prefs.setSplitMultiAttrs(splitMultiAttrs != null ? Boolean.valueOf(splitMultiAttrs) : null);

        if (wellFormedValidation != null) {
            prefs.setWellFormedValidation(wellFormedValidation);
        }

        this.formatter = new XmlDocumentFormatter(options.getOrDefault("lineending", System.lineSeparator()), prefs);
    }

    @Override
    protected String doFormat(final String code, final LineEnding ending) {
        final var formattedCode = this.formatter.format(code);

        if (code.equals(formattedCode)) {
            return null;
        }

        return formattedCode;
    }

    @Override
    public boolean isInitialized() {
        return this.formatter != null;
    }

}
