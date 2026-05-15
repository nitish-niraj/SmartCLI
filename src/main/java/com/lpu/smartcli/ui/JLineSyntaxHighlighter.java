package com.lpu.smartcli.ui;

import com.lpu.smartcli.smart.SyntaxHighlighter;
import com.lpu.smartcli.smart.SyntaxHighlighter.Token;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Bridges {@link SyntaxHighlighter} tokens to JLine {@link AttributedString} styles for the prompt line.
 */
public final class JLineSyntaxHighlighter {

    private JLineSyntaxHighlighter() {
    }

    public static AttributedString highlight(String buffer) {
        if (buffer == null || buffer.isBlank()) {
            return new AttributedString("");
        }

        AttributedStringBuilder builder = new AttributedStringBuilder();
        for (Token token : SyntaxHighlighter.highlight(buffer)) {
            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.style(styleFor(token.getType())).append(token.getText());
        }

        return builder.toAttributedString();
    }

    private static AttributedStyle styleFor(Token.TokenType type) {
        return switch (type) {
            case COMMAND -> AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN);
            case FLAG -> AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);
            case FILENAME -> AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
            case ARGUMENT -> AttributedStyle.DEFAULT;
        };
    }
}
