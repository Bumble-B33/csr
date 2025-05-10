package net.bumblebee.claysoldiers.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class DefaultedResourceLocationArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = List.of("foo:bar", "zee");

    public static final String INVALID_RESOURCE_LOCATION = "argument." + ClaySoldiersCommon.MOD_ID + ".resource_location.invalid";
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
            string -> Component.translatableEscape(INVALID_RESOURCE_LOCATION, string)
    );

    public static AllClayMobTeam all(CommandBuildContext context) {
        return new AllClayMobTeam(context);
    }

    public static SoldierItemType itemType(CommandBuildContext context) {
        return new SoldierItemType(context);
    }


    public static ResourceLocation key(String key, CommandContext<CommandSourceStack> context) {
        return context.getArgument(key, ResourceLocation.class);
    }

    protected abstract boolean isValid(ResourceLocation key);

    @Override
    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        var res =read(reader);
        if (!isValid(res)) {
            throw ERROR_INVALID_VALUE.create(res.toString());
        }
        return res;
    }

    private static ResourceLocation read(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        String s = readGreedy(reader);

        try {
            return CodecUtils.parse(s);
        } catch (ResourceLocationException resourcelocationexception) {
            reader.setCursor(i);
            throw ResourceLocation.ERROR_INVALID.createWithContext(reader);
        }
    }

    private static String readGreedy(StringReader reader) {
        int i = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(i, reader.getCursor());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public abstract <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder);

    public static class AllClayMobTeam extends DefaultedResourceLocationArgument {
        private final HolderLookup<ClayMobTeam> registryLookup;

        public AllClayMobTeam(CommandBuildContext context) {
            this.registryLookup = context.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS);
        }

        @Override
        protected boolean isValid(ResourceLocation key) {
            return registryLookup.get(ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, key)).isPresent();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return SharedSuggestionProvider.suggestResource(this.registryLookup.listElementIds().map(ResourceKey::location), builder);

        }
    }

    public static class SoldierItemType extends DefaultedResourceLocationArgument {
        private final CommandBuildContext context;

        public SoldierItemType(CommandBuildContext context) {
            this.context = context;
        }

        @Override
        protected boolean isValid(ResourceLocation key) {
            var registryLookup = context.lookup(ModRegistries.SOLDIER_ITEM_TYPES);
            if (registryLookup.isEmpty()) {
                return true;
            }
            return registryLookup.orElseThrow().get(ResourceKey.create(ModRegistries.SOLDIER_ITEM_TYPES, key)).isPresent();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return context.getSource() instanceof SharedSuggestionProvider sharedsuggestionprovider
                    ? sharedsuggestionprovider.suggestRegistryElements(ModRegistries.SOLDIER_ITEM_TYPES, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, builder, context)
                    : builder.buildFuture();
        }
    }
}
