package us.maxpowa.apc;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Mod("apotheoticcreation")
public class ApotheoticCreation {
    public static final ResourceLocation RARITY_ID = ResourceLocation.fromNamespaceAndPath("apotheoticcreation", "rarity");
    public static final ResourceLocation AFFIX_ID = ResourceLocation.fromNamespaceAndPath("apotheoticcreation", "affix");

    public ApotheoticCreation(IEventBus modEventBus) {
        modEventBus.addListener(this::registerAttributes);
    }

    private void registerAttributes(RegisterEvent event) {
        ResourceKey<Registry<ItemAttributeType>> attributeRegistryKey =
                ResourceKey.createRegistryKey(CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.key().location());

        if (event.getRegistryKey().equals(attributeRegistryKey)) {
            event.register(attributeRegistryKey, RARITY_ID, () -> new RarityAttribute.Type());
            event.register(attributeRegistryKey, AFFIX_ID, () -> new AffixAttribute.Type());
        }
    }

    public static class RarityAttribute implements ItemAttribute {
        public static final MapCodec<RarityAttribute> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        ResourceLocation.CODEC.optionalFieldOf("rarity_id").forGetter(attr -> {
                            if (attr.rarity == null) return Optional.empty();
                            return RarityRegistry.INSTANCE.getKeys().stream()
                                    .filter(key -> RarityRegistry.INSTANCE.holder(key).get() == attr.rarity)
                                    .findFirst();
                        })
                ).apply(instance, id -> {
                    if (id.isPresent()) {
                        DynamicHolder<LootRarity> holder = RarityRegistry.INSTANCE.holder(id.get());
                        return new RarityAttribute(holder.isBound() ? holder.get() : null);
                    }
                    return new RarityAttribute(null);
                })
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, RarityAttribute> STREAM_CODEC = StreamCodec.of(
                (buf, attr) -> {
                    Optional<ResourceLocation> id = Optional.empty();
                    if (attr.rarity != null) {
                        id = RarityRegistry.INSTANCE.getKeys().stream()
                                .filter(key -> RarityRegistry.INSTANCE.holder(key).get() == attr.rarity)
                                .findFirst();
                    }
                    buf.writeBoolean(id.isPresent());
                    id.ifPresent(buf::writeResourceLocation);
                },
                buf -> {
                    if (buf.readBoolean()) {
                        DynamicHolder<LootRarity> holder = RarityRegistry.INSTANCE.holder(buf.readResourceLocation());
                        return new RarityAttribute(holder.isBound() ? holder.get() : null);
                    }
                    return new RarityAttribute(null);
                }
        );

        private LootRarity rarity;

        public RarityAttribute() {}

        public RarityAttribute(LootRarity rarity) {
            this.rarity = rarity;
        }

        @Override
        public boolean appliesTo(ItemStack stack, Level level) {
            if (rarity == null) return false;

            DynamicHolder<LootRarity> itemRarity = AffixHelper.getRarity(stack);
            return itemRarity.isBound() && itemRarity.get() == rarity;
        }

        @Override
        public String getTranslationKey() {
            return "item_rarity";
        }

        @Override
        public Object[] getTranslationParameters() {
            return rarity != null ?
                    new Object[]{ rarity.toComponent() } :
                    new Object[0];
        }

        @Override
        public ItemAttributeType getType() {
            return CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.get(RARITY_ID);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof RarityAttribute other)) return false;
            return Objects.equals(rarity, other.rarity);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(rarity);
        }

        public static class Type implements ItemAttributeType {
            @Override
            public @NotNull ItemAttribute createAttribute() {
                return new RarityAttribute();
            }

            @Override
            public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
                List<ItemAttribute> attributes = new ArrayList<>();
                DynamicHolder<LootRarity> itemRarity = AffixHelper.getRarity(stack);

                if (itemRarity.isBound()) {
                    attributes.add(new RarityAttribute(itemRarity.get()));
                }

                return attributes;
            }

            @Override
            public MapCodec<? extends ItemAttribute> codec() {
                return CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
                return STREAM_CODEC;
            }
        }
    }

    public static class AffixAttribute implements ItemAttribute {
        public static final MapCodec<AffixAttribute> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        ResourceLocation.CODEC.optionalFieldOf("affix_id").forGetter(attr ->
                                Optional.ofNullable(attr.affix != null && attr.affix.isBound() ? attr.affix.getId() : null)
                        )
                ).apply(instance, id -> new AffixAttribute(id.isPresent() ? AffixRegistry.INSTANCE.holder(id.get()) : null))
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, AffixAttribute> STREAM_CODEC = StreamCodec.of(
                (buf, attr) -> {
                    boolean hasAffix = attr.affix != null && attr.affix.isBound();
                    buf.writeBoolean(hasAffix);
                    if (hasAffix) {
                        buf.writeResourceLocation(attr.affix.getId());
                    }
                },
                buf -> {
                    if (buf.readBoolean()) {
                        return new AffixAttribute(AffixRegistry.INSTANCE.holder(buf.readResourceLocation()));
                    }
                    return new AffixAttribute(null);
                }
        );

        private static final Set<String> HIDDEN_AFFIXES = Set.of("socket", "durable");
        private DynamicHolder<? extends Affix> affix;

        public AffixAttribute() {}

        public AffixAttribute(DynamicHolder<? extends Affix> affix) {
            this.affix = affix;
        }

        @Override
        public boolean appliesTo(ItemStack stack, Level level) {
            if (affix == null || !affix.isBound()) return false;

            Map<DynamicHolder<Affix>, AffixInstance> affixes =
                    AffixHelper.getAffixes(stack);

            return affixes.containsKey(affix);
        }

        @Override
        public String getTranslationKey() {
            return "item_affix";
        }

        @Override
        public Object[] getTranslationParameters() {
            return affix != null && affix.isBound() ?
                    new Object[]{ affix.get().getName(true) } :
                    new Object[0];
        }

        @Override
        public ItemAttributeType getType() {
            return CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.get(AFFIX_ID);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof AffixAttribute other)) return false;
            return Objects.equals(affix, other.affix);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(affix);
        }

        public static class Type implements ItemAttributeType {
            @Override
            public @NotNull ItemAttribute createAttribute() {
                return new AffixAttribute();
            }

            @Override
            public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
                return AffixHelper.getAffixes(stack).keySet().stream()
                        .filter(affix ->
                                affix.isBound() &&
                                        !HIDDEN_AFFIXES.contains(affix.getId().getPath())
                        )
                        .map(AffixAttribute::new)
                        .collect(Collectors.toList());
            }

            @Override
            public MapCodec<? extends ItemAttribute> codec() {
                return CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
                return STREAM_CODEC;
            }
        }
    }
}