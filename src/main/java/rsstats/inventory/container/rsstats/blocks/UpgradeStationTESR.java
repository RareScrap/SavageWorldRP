package rsstats.inventory.container.rsstats.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
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

    // TODO: Найти сслку на тутор, по которому я это делал
    /**
     * Рендерер блока {@link UpgradeStationBlock} в инвентаре и в руке
     */
    public static class Renderer implements IItemRenderer {
        TileEntitySpecialRenderer render;
        private TileEntity te; // Хз зачем, но в туторе нужно

        public Renderer(TileEntitySpecialRenderer render, TileEntity te) {
            this.render = render;
            this.te = te;
        }

        @Override
        public boolean handleRenderType(ItemStack item, ItemRenderType type) {
            return true;
        }

        @Override
        public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
            return true;
        }

        // Прикол в том, что Renderer не может использоать UpgradeStationTESR#model. Хз почему. Даже если она - статик. Так что я создаю model еще раз
        //IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(RSStats.MODID, "obj/upgrade_station.obj"));

        //ResourceLocation texture = new ResourceLocation(RSStats.MODID, "textures/t.png");

        @Override
        public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
            // Было в туторе, но смещает центр, когда итем выбрасывается игроком, из-за чего он вертится не вокруг своего логического центра
            /*if (type == IItemRenderer.ItemRenderType.ENTITY) {
                GL11.glTranslatef(-0.5F, 0.0F, -0.5F);
            }*/

            if (type == ItemRenderType.EQUIPPED) {
                GL11.glTranslatef(0.5F, 0.0F, 0.5F); // Делаем так, чтобы рука игрока держалась за центр блока
                GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F); // Поворачиваем тисками в другую сторону, чтобы сами тиски были видны. А то некрасиво
            }

            if (type == ItemRenderType.INVENTORY) {
                GL11.glTranslatef(0.0F, -0.5F, 0.0F); // Чутка опустим предмет вниз, чтобы он нормально отображался в слоте инвентаря
                //GL11.glScalef(1.3F, 1.3F, 1.3F);
            } else {
                // Делаем его чуть-больше, чтобы лучше смотрелся
                //GL11.glScalef(1.5F, 1.5F, 1.5F);
            }

            if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                GL11.glTranslatef(0.0F, 0.35F, 0.5F); // Смещаем центр предмета в руке, чтобы он рисовался там же, где и все остальные блоки, когда их держит юзер
                //GL11.glScalef(1.3F, 1.3F, 1.3F);
            }

            // О БЛЯ! ТОЖЕ РАБОТАЕТ!
            Minecraft.getMinecraft().renderEngine.bindTexture(((UpgradeStationTESR) render).texture);
            ((UpgradeStationTESR) render).model.renderAll();

            // Не работает. жалуется на нехватку текстур или что-то в этом духе. Хотя по тутору - должно.
            //this.render.renderTileEntityAt(this.te, 0.0D, 0.0D, 0.0D, 0.0F);

            // Рабочий бинд текстуры
            //Minecraft.getMinecraft().renderEngine.bindTexture(texture);

            // К моему удивлению - это работает. Но крайне криво
            //model.renderAll();

            // Дебажная вещь
            switch (type) {
                case ENTITY: {
                    System.out.println("ENTITY");
                    break;
                }
                case EQUIPPED: {
                    System.out.println("EQUIPPED");
                    break;
                }
                case EQUIPPED_FIRST_PERSON: {
                    System.out.println("EQUIPPED_FIRST_PERSON");
                    break;
                }case FIRST_PERSON_MAP: {
                    System.out.println("FIRST_PERSON_MAP");
                    break;
                }case INVENTORY: {
                    System.out.println("INVENTORY");
                    break;
                }
            }
        }
    }
}