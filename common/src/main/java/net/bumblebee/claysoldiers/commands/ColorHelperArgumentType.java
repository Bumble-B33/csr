package net.bumblebee.claysoldiers.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;


public class ColorHelperArgumentType implements ArgumentType<ColorHelper> {
    private static final Collection<String> EXAMPLES = List.of("normal", "#FFFFFF", "#EFD3C4", "black", "aqua");
    public static final String INVALID_COLOR = "argument." + ClaySoldiersCommon.MOD_ID + ".color_type.invalid";
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
            string -> Component.translatableEscape(INVALID_COLOR, string)
    );
    public static final List<String> SUGGESTIONS = buildSuggestions();
    private static final Pattern HEX_PATTERN = Pattern.compile("^#?([A-Fa-f0-9]{6})$");

    public static ColorHelperArgumentType colorArgumentType() {
        return new ColorHelperArgumentType();
    }

    private static String readHexString(StringReader reader) throws CommandSyntaxException {
        StringBuilder input = new StringBuilder();
        boolean isHex = false;
        if (reader.canRead()) {
            char startChar = reader.peek();
            input.append(startChar);
            isHex = startChar == '#';
            reader.skip();
        }
        boolean valid = false;
        while (reader.canRead()) {
            char symbol = reader.peek();
            if (Character.isWhitespace(symbol)) {
                break;
            }
            input.append(symbol);
            valid = isHex && HEX_PATTERN.matcher(input).matches();
            reader.skip();

        }
        if (isHex && !valid) {
            throw ERROR_INVALID_VALUE.create("Not a valid hex color");
        }
        return input.toString();
    }

    @Override
    public ColorHelper parse(StringReader reader) throws CommandSyntaxException {
        String s = readHexString(reader);

        ChatFormatting chatFormatting = ChatFormatting.getByName(s);
        if (chatFormatting != null) {
            return ColorHelper.color(chatFormatting.getColor());
        }

        if (s.equals("normal")) {
            return ColorHelper.EMPTY;
        }
        if (s.equals("jeb_")) {
            return ColorHelper.jeb();
        }
        try {
            return ColorHelper.color(Integer.parseInt(s.substring(1), 16));
        } catch (NumberFormatException e) {
            throw ERROR_INVALID_VALUE.createWithContext(reader, e.getMessage());
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(SUGGESTIONS, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static List<String> buildSuggestions() {
        List<String> list = new ArrayList<>();
        list.add("normal");
        list.addAll(ChatFormatting.getNames(true, false));
        list.remove(ChatFormatting.RESET.getName());
        list.add("jeb_");
        return list;

    }
}
