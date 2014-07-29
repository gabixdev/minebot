package net.famzangl.minecraft.minebot.ai.strategy;

import net.famzangl.minecraft.minebot.Pos;
import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.AIStrategy;
import net.famzangl.minecraft.minebot.ai.AIStrategyFactory;
import net.famzangl.minecraft.minebot.ai.path.LayRailPathFinder;
import net.minecraftforge.common.util.ForgeDirection;

public class LayRailStrategy implements AIStrategyFactory {

	@Override
	public AIStrategy produceStrategy(AIHelper helper) {
		final Pos p = helper.getPlayerPosition();
		ForgeDirection horizontalLook = helper.getLookDirection();

		return new PathFinderStrategy(new LayRailPathFinder(helper, horizontalLook.offsetX, horizontalLook.offsetZ,
				p.x, p.y, p.z), "Building a railway");
	}
}
