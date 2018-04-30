package rsstats.inventory.container.rsstats.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
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
        setHardness(2.5F); // Как у верстака, судя по вики
        // TODO: Установить подходящий инструмент, взывоустойчивость, горение и другие свойства
        //BlockStairs
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new UpgradeStationEntity();
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            entityPlayer.openGui(RSStats.instance, RSStats.UPGRADE_UI_FROM_BLOCK_CODE, world, x, y, z);
            //CommonProxy.INSTANCE.sendToServer(new PacketOpenWindow(x, y, z));
            //return false; // TODO: Зачем?
        }
        return super.onBlockActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack p_149689_6_) {
        //This gets the direction the player is facing as an int from 0 to 3
        int dir = MathHelper.floor_double((player.rotationYaw * 4F) / 360F + 0.5D) & 3;
        //You can use the block metadata to save the direction
        world.setBlockMetadataWithNotify(x, y, z, dir, 3);
        //Or you can save it in a tile entity if you are using one
        //createNewTileEntity(world, world.getBlockMetadata(x, y, z));
        super.onBlockPlacedBy(world, x, y, z, player, p_149689_6_);
    }
}