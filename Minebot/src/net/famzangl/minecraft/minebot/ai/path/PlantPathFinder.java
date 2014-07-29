package net.famzangl.minecraft.minebot.ai.path;

import net.famzangl.minecraft.minebot.Pos;
import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.ItemFilter;
import net.famzangl.minecraft.minebot.ai.task.UseItemOnBlockAtTask;
import net.famzangl.minecraft.minebot.ai.task.place.DestroyBlockTask;
import net.famzangl.minecraft.minebot.ai.task.place.PlaceBlockAtFloorTask;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

public class PlantPathFinder extends MovePathFinder {
	public enum PlantType {
		ANY(Items.wheat_seeds, Items.carrot, Items.potato),
		WHEAT(Items.wheat_seeds),
		CARROT(Items.carrot),
		POTATO(Items.potato),
		NETHERWART(Items.nether_wart);

		private final Item[] items;

		private PlantType(Item... items) {
			this.items = items;
		}

		public boolean canPlantItem(Item item) {
			for (Item i : items) {
				if (item == i) {
					return true;
				}
			}
			return false;
		}
	}

	private final class HoeFilter implements ItemFilter {
		@Override
		public boolean matches(ItemStack itemStack) {
			return itemStack != null && itemStack.getItem() instanceof ItemHoe;
		}
	}

	private final class SeedFilter implements ItemFilter {
		private final PlantType type;

		public SeedFilter(PlantType type) {
			super();
			this.type = type;
		}

		@Override
		public boolean matches(ItemStack itemStack) {
			return itemStack != null && type.canPlantItem(itemStack.getItem());
		}
	}

	private final PlantType type;

	public PlantPathFinder(AIHelper helper, PlantType type) {
		super(helper);
		this.type = type;
	}

	@Override
	protected float rateDestination(int distance, int x, int y, int z) {
		if (isGrown(helper, x, y, z)) {
			return distance + 1;
		} else if (helper.isAirBlock(x, y, z) && hasFarmlandBelow(x, y, z)
				&& helper.canSelectItem(new SeedFilter(type))) {
			return distance + 1;
		} else if (helper.isAirBlock(x, y, z)
				&& AIHelper.blockIsOneOf(helper.getBlock(x, y - 1, z),
						Blocks.dirt, Blocks.grass)
				&& helper.canSelectItem(new SeedFilter(type))
				&& helper.canSelectItem(new HoeFilter())) {
			return distance + 10;
		} else {
			return -1;
		}
	}

	private boolean isGrown(AIHelper helper, int x, int y, int z) {
		final Block block = helper.getBlock(x, y, z);
		if (block instanceof BlockCrops) {
			final int metadata = helper.getMinecraft().theWorld
					.getBlockMetadata(x, y, z);
			return metadata >= 7;
		}
		return false;
	}

	private boolean hasFarmlandBelow(int x, int y, int z) {
		return Block.isEqualTo(helper.getBlock(x, y - 1, z), Blocks.farmland);
	}

	@Override
	protected boolean isForbiddenBlock(Block block) {
		return !(helper.canWalkOn(block) || AIHelper.blockIsOneOf(block,
				Blocks.air));
	}

	@Override
	protected void addTasksForTarget(Pos currentPos) {
		if (helper.isAirBlock(currentPos.x, currentPos.y, currentPos.z)) {
			if (!hasFarmlandBelow(currentPos.x, currentPos.y, currentPos.z)) {
				helper.addTask(new UseItemOnBlockAtTask(new HoeFilter(),
						currentPos.x, currentPos.y - 1, currentPos.z));
			}
			helper.addTask(new PlaceBlockAtFloorTask(currentPos.x,
					currentPos.y, currentPos.z, new SeedFilter(type)));
		} else {
			helper.addTask(new DestroyBlockTask(currentPos.x, currentPos.y,
					currentPos.z));
		}
	}
}
