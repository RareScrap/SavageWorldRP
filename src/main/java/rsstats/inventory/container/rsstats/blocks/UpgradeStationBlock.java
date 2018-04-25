package rsstats.inventory.container.rsstats.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import rsstats.common.RSStats;

public class UpgradeStationBlock extends BlockContainer {
    public static final String name = "upgrade_station";

    public UpgradeStationBlock() {
        super(Material.wood);
        setBlockTextureName(RSStats.MODID + ":" + name);
        //setBlockBounds(0.25F, 0, 0.25F, 0.75F, 0.5F, 0.75F);
        setBlockName(name);
        setCreativeTab(RSStats.CREATIVE_TAB);

        //GameRegistry.registerBlock(this, name);
    }
    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2)
    {
        return new UpgradeStationEntity();
    }
}