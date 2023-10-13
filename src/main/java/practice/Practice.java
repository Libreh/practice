package practice;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import practice.mixin.accessors.DisplayEntityAccessor;
import practice.util.MoveWorld;

import java.io.IOException;

public class Practice implements ModInitializer {
	public static final String MOD_ID = "practice";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier PRACTICE_WORLD_ID = new Identifier(MOD_ID, "world");
	public static RegistryKey<World> practiceRegistryKey = RegistryKey.of(RegistryKeys.WORLD, PRACTICE_WORLD_ID);

	@Override
	public void onInitialize() {

		try {
			MoveWorld.invoke();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			server.getOverworld().getChunkManager().removePersistentTickets();

			server.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(0, server);

			try {
				spawnStructure(server);
			} catch (IOException e) {
				LOGGER.info("Failed to spawn Practice mod world");
			}

			var world = server.getWorld(practiceRegistryKey);

			spawnInteractionText(world, -1.5, 65.0, -0.99, "Movement", true, Items.FEATHER);
			spawnInteractionText(world, 0.5, 65.0, -0.99, "Clutching", true, Items.WATER_BUCKET);
			spawnInteractionText(world, 2.5, 65.0, -0.99, "Completion", true, Items.ENDER_EYE);

			spawnInteractionText(world, -1.5, 71.0, -0.99, "Freerun", true, Items.IRON_BOOTS);
			spawnInteractionText(world, 0.5, 71.0, -0.99, "Bridging", true, Items.IRON_DOOR);
			spawnInteractionText(world, 2.5, 71.0, -0.99, "Course", true, Items.RAW_IRON);
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (player.getWorld() != player.getServer().getWorld(practiceRegistryKey)) {
					player.teleport(player.getServer().getWorld(practiceRegistryKey), 0.5, 63, 1, 180, 0);
					player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1f, 1f);
					player.changeGameMode(GameMode.ADVENTURE);
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
				}
			}
		});
	}

	private void spawnStructure(MinecraftServer server) throws IOException {
		var practiceSpawnNbt = NbtIo.readCompressed(getClass().getResourceAsStream("/practice/world/spawn.nbt"));

		var lobbyWorld = server.getWorld(practiceRegistryKey);

		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 0), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, 0), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, -15), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, -15), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 16), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 0), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 16), 16, Unit.INSTANCE);
		placeStructure(lobbyWorld, new BlockPos(-12, 62, -7), practiceSpawnNbt);
	}

	private void placeStructure(ServerWorld world, BlockPos pos, NbtCompound nbt) {
		StructureTemplate template = world.getStructureTemplateManager().createTemplate(nbt);

		template.place(
				world,
				pos,
				pos,
				new StructurePlacementData(),
				StructureBlockBlockEntity.createRandom(world.getSeed()),
				2
		);
	}

	private void spawnInteractionText(ServerWorld world, double x, double y, double z, String name, boolean itemDisplay, Item item) {
		DisplayEntity.TextDisplayEntity textDisplayEntity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
		NbtCompound displayNbt = new NbtCompound();
		NbtList displayPosList = new NbtList();
		displayPosList.add(NbtDouble.of(x));
		displayPosList.add(NbtDouble.of(y));
		displayPosList.add(NbtDouble.of(z));
		displayNbt.put("Pos", displayPosList);
		displayNbt.putFloat("width", 1f);
		displayNbt.putFloat("height", 0.25f);
		displayNbt.putString("text", Text.Serializer.toJson(Text.empty().append(Text.literal(name))));
		displayNbt.putString("billboard", "fixed");
		displayNbt.putString("alignment", "center");
		textDisplayEntity.readNbt(displayNbt);
		InteractionEntity interactionEntity = new InteractionEntity(EntityType.INTERACTION, world);
		NbtList interactPosList = new NbtList();
		interactPosList.add(NbtDouble.of(x));
		interactPosList.add(NbtDouble.of(y-1.2));
		interactPosList.add(NbtDouble.of(z-0.51));
		NbtCompound interactNbt = new NbtCompound();
		interactNbt.put("Pos", interactPosList);
		interactNbt.putFloat("width", 1.2f);
		interactNbt.putFloat("height", 1.5f);
		interactionEntity.readNbt(interactNbt);
		interactionEntity.setCustomName(Text.literal(name));
		world.spawnEntity(textDisplayEntity);
		world.spawnEntity(interactionEntity);
		if (itemDisplay) {
			DisplayEntity.ItemDisplayEntity itemDisplayEntity = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
			NbtList itemPosList = new NbtList();
			itemPosList.add(NbtDouble.of(x));
			itemPosList.add(NbtDouble.of(y-0.5));
			itemPosList.add(NbtDouble.of(z+0.01));
			NbtCompound itemNbt = new NbtCompound();
			itemNbt.put("Pos", itemPosList);
			itemDisplayEntity.readNbt(itemNbt);
			itemDisplayEntity.getStackReference(0).set(new ItemStack(item));
			itemDisplayEntity.getDataTracker().set(DisplayEntityAccessor.getSCALE(), new Vector3f(0.5f, 0.5f, 0.5f));
			world.spawnEntity(itemDisplayEntity);
		}
	}
}