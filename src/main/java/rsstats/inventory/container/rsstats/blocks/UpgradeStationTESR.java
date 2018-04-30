package rsstats.inventory.container.rsstats.blocks;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import rsstats.common.RSStats;

public class UpgradeStationTESR extends TileEntitySpecialRenderer
{
    IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(RSStats.MODID, "obj/upgrade_station.obj"));
    ResourceLocation texture = new ResourceLocation(RSStats.MODID, "textures/t.png");

    @Override
    public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float p_147500_8_) {

        bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.0F, (float) z + 0.5F);
        //GL11.glScalef(0.09375F, 0.09375F, 0.09375F);

        // Поворачиваем блок в нужном направлении (направление хранится в метадате блока, которая выставляется когда игрок ставит блок)
        int dir = entity.blockMetadata;
        if (dir == 0)
        {
            GL11.glRotatef(-180F, 0.0F, 1.0F, 0.0F);
        }

        if (dir % 2 != 0)
        {
            GL11.glRotatef(dir * (/*-*/90F), 0.0F, 1.0F, 0.0F);
        }

        if (dir % 2 == 0)
        {
            GL11.glRotatef(dir * (-180F), 0.0F, 1.0F, 0.0F);
        }

        // Рендерим блок
        model.renderAll();
        GL11.glPopMatrix();
    }
}