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
package net.revelc.code.formatter.jsoup;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;

import net.revelc.code.formatter.AbstractCacheableFormatter;
import net.revelc.code.formatter.ConfigurationSource;
import net.revelc.code.formatter.Formatter;
import net.revelc.code.formatter.LineEnding;

/**
 * The Class JsoupBasedFormatter.
 */
public abstract class JsoupBasedFormatter extends AbstractCacheableFormatter implements Formatter {

    /** The Constant REMOVE_TRAILING_PATTERN. */
    private static final Pattern REMOVE_TRAILING_PATTERN = Pattern.compile("\\p{Blank}+$", Pattern.MULTILINE);

    /** The Constant RESET_LEADING_SPACES_PATTERN. */
    private static final Pattern RESET_LEADING_SPACES_PATTERN = Pattern.compile("^\\s+");

    /** The formatter. */
    private OutputSettings formatter;

    @Override
    public void init(final Map<String, String> options, final ConfigurationSource cfg) {
        super.initCfg(cfg);

        this.formatter = new OutputSettings();
        this.formatter.charset(Charset.forName(options.getOrDefault("charset", StandardCharsets.UTF_8.name())));
        this.formatter.escapeMode(EscapeMode.valueOf(options.getOrDefault("escapeMode", EscapeMode.xhtml.name())));
        this.formatter.indentAmount(Integer.parseInt(options.getOrDefault("indentAmount", "4")));
        this.formatter.maxPaddingWidth(Integer.parseInt(options.getOrDefault("maxPaddingWidth", "-1")));
        this.formatter.outline(Boolean.parseBoolean(options.getOrDefault("outlineMode", Boolean.TRUE.toString())));
        this.formatter.prettyPrint(Boolean.parseBoolean(options.getOrDefault("pretty", Boolean.TRUE.toString())));
        this.formatter.syntax(Syntax.valueOf(options.getOrDefault("syntax", Syntax.html.name())));
    }

    @Override
    public String doFormat(String code, final LineEnding ending) {
        Document document;
        if (this.formatter.syntax() != Syntax.html) {
            throw new IllegalArgumentException(this.formatter.syntax() + " is not allowed as syntax");
        }
        document = Jsoup.parse(code, "", Parser.htmlParser());
        document.outputSettings(this.formatter);

        var formattedCode = document.outerHtml();

        // TODO: Fixing trailing space issue caused by jsoup. We do fix this during a proper run
        // but our tests fail to do so thus we are duplicating this until jsoup fixes bug.
        formattedCode = REMOVE_TRAILING_PATTERN.matcher(formattedCode).replaceAll("");

        // TODO: Fixing jsoup counting issue which occurs when more than one indentation (jsoup can have mixed line
        // ending content)
        if (this.formatter.indentAmount() > 1) {
            int lineSize;
            int newLineSize;
            int remainder;
            String[] lines = formattedCode.split("\\r?\\n");
            List<String> newLines = new ArrayList<>(lines.length);
            for (String line : lines) {
                lineSize = line.length();
                if (lineSize != 0) {
                    newLineSize = RESET_LEADING_SPACES_PATTERN.matcher(line).replaceAll("").length();
                    if (lineSize != newLineSize) {
                        remainder = (lineSize - newLineSize) % this.formatter.indentAmount();
                        if (remainder > 0) {
                            line = line.substring(remainder);
                        }
                    }
                }
                newLines.add(line);
            }
            formattedCode = String.join(ending.getChars(), newLines);
        }

        // TODO: Fixing jsoup tagging issue where line break doesn't occur (seen in headers)
        formattedCode = formattedCode.replace("--><!", "-->" + ending.getChars() + "<!");

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
